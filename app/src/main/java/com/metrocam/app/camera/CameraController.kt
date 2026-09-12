package com.metrocam.app.camera

import android.content.Context
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.util.Log
import android.util.Range
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.metrocam.app.processing.toBitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Wraps CameraX for preview + capture, and exposes Camera2-level manual controls
 * (ISO, shutter speed, focus distance, white balance) via [Camera2CameraControl], which
 * lets those settings be updated live without rebinding the use cases.
 */
@OptIn(ExperimentalCamera2Interop::class)
class CameraController(private val context: Context) {

    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    var sensorInfo: SensorInfo? = null
        private set

    suspend fun bindToLifecycle(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val provider = ProcessCameraProvider.getInstance(context).await(context)

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
        imageCapture = capture

        val selector = CameraSelector.DEFAULT_BACK_CAMERA

        provider.unbindAll()
        camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
        sensorInfo = readSensorInfo()
    }

    private fun readSensorInfo(): SensorInfo? {
        val cam = camera ?: return null
        return try {
            val camera2Info = Camera2CameraInfo.from(cam.cameraInfo)
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val characteristics = cameraManager.getCameraCharacteristics(camera2Info.cameraId)
            val isoRange: Range<Int>? = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
            val exposureRange: Range<Long>? =
                characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
            val minFocusDistance: Float? =
                characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
            SensorInfo(
                isoRange = isoRange ?: Range(100, 800),
                exposureTimeRangeNs = exposureRange ?: Range(1_000_000L, 250_000_000L),
                minFocusDistanceDiopters = minFocusDistance ?: 0f
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not read sensor characteristics", e)
            null
        }
    }

    /** Pushes [state] onto the live Camera2 capture request. No use-case rebind required. */
    fun applyManualControls(state: ManualControlState) {
        val cam = camera ?: return
        val camera2Control = Camera2CameraControl.from(cam.cameraControl)
        val builder = CaptureRequestOptions.Builder()
        if (state.enabled) {
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
            builder.setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, state.iso)
            builder.setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, state.exposureTimeNs)
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
            builder.setCaptureRequestOption(CaptureRequest.LENS_FOCUS_DISTANCE, state.focusDistanceDiopters)
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, state.whiteBalanceMode)
        } else {
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            builder.setCaptureRequestOption(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            builder.setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
        }
        camera2Control.captureRequestOptions = builder.build()
    }

    fun setExposureCompensationIndex(index: Int) {
        camera?.cameraControl?.setExposureCompensationIndex(index)
    }

    /** Suggests a [-2EV, 0, +2EV] bracket (clamped to what this device's AE range supports). */
    fun suggestedHdrBracket(): List<Int> {
        val exposureState = camera?.cameraInfo?.exposureState ?: return listOf(0)
        if (!exposureState.isExposureCompensationSupported) return listOf(0)
        val range = exposureState.exposureCompensationRange
        val step = exposureState.exposureCompensationStep.toFloat()
        if (step <= 0f) return listOf(0)
        val twoEvSteps = kotlin.math.round(2f / step).toInt().coerceAtLeast(1)
        val under = (-twoEvSteps).coerceIn(range.lower, range.upper)
        val over = twoEvSteps.coerceIn(range.lower, range.upper)
        return listOf(under, 0, over).distinct().sorted()
    }

    suspend fun captureOneFrame(): Bitmap {
        val capture = imageCapture ?: error("Camera not bound yet")
        return suspendCancellableCoroutine { cont ->
            capture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        cont.resume(image.toBitmap())
                    } catch (e: Exception) {
                        cont.resumeWithException(e)
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    cont.resumeWithException(exception)
                }
            })
        }
    }

    /** Captures one frame per exposure-compensation index in [evSteps], then restores AE to 0. */
    suspend fun captureBracket(evSteps: List<Int>): List<Bitmap> {
        val results = mutableListOf<Bitmap>()
        try {
            for (ev in evSteps) {
                setExposureCompensationIndex(ev)
                delay(200) // let auto-exposure settle at the new compensation
                results.add(captureOneFrame())
            }
        } finally {
            setExposureCompensationIndex(0)
        }
        return results
    }

    suspend fun captureBurst(count: Int, delayMs: Long = 80L): List<Bitmap> {
        val results = mutableListOf<Bitmap>()
        repeat(count) {
            results.add(captureOneFrame())
            delay(delayMs)
        }
        return results
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraController"
    }
}

data class SensorInfo(
    val isoRange: Range<Int>,
    val exposureTimeRangeNs: Range<Long>,
    val minFocusDistanceDiopters: Float
)

private suspend fun <T> ListenableFuture<T>.await(context: Context): T =
    suspendCancellableCoroutine { cont ->
        addListener(
            {
                try {
                    cont.resume(get())
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

package com.metrocam.app.ui

import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.metrocam.app.camera.CameraController
import com.metrocam.app.camera.CaptureMode
import com.metrocam.app.camera.ManualControlState
import com.metrocam.app.processing.HdrProcessor
import com.metrocam.app.processing.NightModeProcessor
import com.metrocam.app.util.MediaStoreSaver
import kotlinx.coroutines.launch

private const val NIGHT_MODE_FRAME_COUNT = 8

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val cameraController = remember { CameraController(context) }
    var mode by remember { mutableStateOf(CaptureMode.PHOTO) }
    var manualState by remember { mutableStateOf(ManualControlState()) }
    var isProcessing by remember { mutableStateOf(false) }
    var showManualControls by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { cameraController.shutdown() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    scope.launch {
                        cameraController.bindToLifecycle(lifecycleOwner, previewView)
                        cameraController.applyManualControls(manualState)
                    }
                }
            }
        )

        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 32.dp)
        ) {
            ModeSelector(selected = mode, onSelect = { mode = it })
        }

        if (showManualControls) {
            ManualControlsPanel(
                sensorInfo = cameraController.sensorInfo,
                state = manualState,
                onStateChange = {
                    manualState = it
                    cameraController.applyManualControls(it)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 140.dp)
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { showManualControls = !showManualControls }) {
                Text(if (showManualControls) "Auto" else "Manual", color = Color.White)
            }

            ShutterButton(enabled = !isProcessing) {
                scope.launch {
                    isProcessing = true
                    try {
                        val result = when (mode) {
                            CaptureMode.PHOTO -> cameraController.captureOneFrame()
                            CaptureMode.HDR -> {
                                val evSteps = cameraController.suggestedHdrBracket()
                                val frames = cameraController.captureBracket(evSteps)
                                HdrProcessor.fuseExposures(frames)
                            }
                            CaptureMode.NIGHT -> {
                                val frames = cameraController.captureBurst(NIGHT_MODE_FRAME_COUNT)
                                NightModeProcessor.stackFrames(frames)
                            }
                        }
                        MediaStoreSaver.saveJpeg(context, result)
                        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Capture failed: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isProcessing = false
                    }
                }
            }

            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}

@Composable
private fun ShutterButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(if (enabled) Color.White else Color.Gray, CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
    )
}

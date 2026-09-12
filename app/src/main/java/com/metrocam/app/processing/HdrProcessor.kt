package com.metrocam.app.processing

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.opencv.photo.Photo

/**
 * HDR exposure-fusion merge, built on OpenCV's `photo` module:
 *  1. [org.opencv.photo.AlignMTB] aligns the handheld bracket (median-threshold-bitmap
 *     registration, robust to small motion between shots).
 *  2. [org.opencv.photo.MergeMertens] blends the aligned exposures by local contrast,
 *     saturation and well-exposedness - Mertens' method - producing a natural-looking
 *     LDR result directly, with no camera-response calibration or exposure metadata needed.
 */
object HdrProcessor {

    fun fuseExposures(frames: List<Bitmap>): Bitmap {
        require(frames.isNotEmpty()) { "Need at least one frame to fuse" }
        if (frames.size == 1) return frames[0]

        val sourceMats = frames.map { it.toBgrMat() }

        val aligned = ArrayList<Mat>(sourceMats.size)
        Photo.createAlignMTB().process(sourceMats, aligned)
        sourceMats.forEach { it.release() }

        val fused = Mat()
        Photo.createMergeMertens().process(aligned, fused)
        aligned.forEach { it.release() }

        // Mertens output is float32 in [0,1]; scale back to 8-bit before converting to a Bitmap.
        val fused8u = Mat()
        fused.convertTo(fused8u, CvType.CV_8UC3, 255.0)
        fused.release()

        return fused8u.toBitmapAndRelease()
    }
}

internal fun Bitmap.toBgrMat(): Mat {
    val rgba = Mat()
    Utils.bitmapToMat(this, rgba)
    val bgr = Mat()
    Imgproc.cvtColor(rgba, bgr, Imgproc.COLOR_RGBA2BGR)
    rgba.release()
    return bgr
}

internal fun Mat.toBitmapAndRelease(): Bitmap {
    val rgba = Mat()
    Imgproc.cvtColor(this, rgba, Imgproc.COLOR_BGR2RGBA)
    release()
    val bitmap = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(rgba, bitmap)
    rgba.release()
    return bitmap
}

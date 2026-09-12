package com.metrocam.app.processing

import android.graphics.Bitmap
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.photo.Photo

/**
 * Night-mode multi-frame merge: a handheld burst at a fixed exposure is aligned with
 * OpenCV's [org.opencv.photo.AlignMTB] (the same registration Google's own HDR+ pipeline
 * lineage is built on), averaged to reduce shot noise by roughly sqrt(N), then finished
 * with a non-local-means denoise pass to clean up the residual noise averaging alone
 * can't remove from deep shadows.
 */
object NightModeProcessor {

    fun stackFrames(frames: List<Bitmap>): Bitmap {
        require(frames.isNotEmpty()) { "Need at least one frame to stack" }
        if (frames.size == 1) return frames[0]

        val sourceMats = frames.map { it.toBgrMat() }

        val aligned = ArrayList<Mat>(sourceMats.size)
        Photo.createAlignMTB().process(sourceMats, aligned)
        sourceMats.forEach { it.release() }

        val accumulator = Mat.zeros(aligned[0].size(), CvType.CV_32FC3)
        for (mat in aligned) {
            val floatMat = Mat()
            mat.convertTo(floatMat, CvType.CV_32FC3)
            Core.add(accumulator, floatMat, accumulator)
            floatMat.release()
            mat.release()
        }

        val averaged8u = Mat()
        accumulator.convertTo(averaged8u, CvType.CV_8UC3, 1.0 / aligned.size)
        accumulator.release()

        val denoised = Mat()
        Photo.fastNlMeansDenoisingColored(averaged8u, denoised, 6f, 6f, 7, 21)
        averaged8u.release()

        return denoised.toBitmapAndRelease()
    }
}

package com.metrocam.app.processing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/**
 * Decodes an [ImageProxy] captured by CameraX's ImageCapture (JPEG format by default for
 * the in-memory OnImageCapturedCallback path) into a correctly-rotated [Bitmap].
 */
fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: error("Failed to decode captured frame")

    val rotation = imageInfo.rotationDegrees
    if (rotation == 0) return decoded

    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
    val rotated = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    if (rotated !== decoded) decoded.recycle()
    return rotated
}

fun Bitmap.toJpegBytes(quality: Int = 95): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, quality, stream)
    return stream.toByteArray()
}

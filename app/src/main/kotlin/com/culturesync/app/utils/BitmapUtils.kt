package com.culturesync.app.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import java.io.ByteArrayOutputStream

/**
 * Utility class for image format conversions required by the camera pipeline.
 * Converts YUV_420_888 ImageProxy / Image to Bitmap for MediaPipe inference.
 */
object BitmapUtils {

    /**
     * Convert a YUV_420_888 [Image] to a [Bitmap].
     * Used when CameraX provides YUV format instead of RGBA_8888.
     */
    fun yuvToBitmap(image: Image): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, out)
        val bytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    /**
     * Scale a bitmap to target dimensions while maintaining aspect ratio.
     */
    fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratio = minOf(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )
        val newW = (bitmap.width * ratio).toInt()
        val newH = (bitmap.height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }
}

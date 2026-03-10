package com.newglobe.gemmaocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private const val MAX_IMAGE_LONG_EDGE = 1280

suspend fun decodeBitmapFromUri(
    context: Context,
    uri: Uri
): Result<Bitmap> = withContext(Dispatchers.IO) {
    runCatching {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.isMutableRequired = false
            decoder.setMemorySizePolicy(ImageDecoder.MEMORY_POLICY_LOW_RAM)

            val width = info.size.width
            val height = info.size.height
            val longEdge = maxOf(width, height)
            if (longEdge > MAX_IMAGE_LONG_EDGE) {
                val scale = MAX_IMAGE_LONG_EDGE.toFloat() / longEdge.toFloat()
                val targetWidth = (width * scale).roundToInt().coerceAtLeast(1)
                val targetHeight = (height * scale).roundToInt().coerceAtLeast(1)
                decoder.setTargetSize(targetWidth, targetHeight)
            }
        }
        ensureArgb8888(decoded)
    }
}

fun ensureArgb8888(bitmap: Bitmap): Bitmap {
    return if (bitmap.config == Bitmap.Config.ARGB_8888) {
        bitmap
    } else {
        bitmap.copy(Bitmap.Config.ARGB_8888, false)
            ?: throw IllegalStateException("Failed to convert bitmap to ARGB_8888.")
    }
}

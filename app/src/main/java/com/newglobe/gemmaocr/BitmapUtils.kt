package com.newglobe.gemmaocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.MediaStore
import com.newglobe.gemmaocr.ui.NormalizedRect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun decodeBitmapFromUri(
    context: Context,
    uri: Uri
): Result<Bitmap> = withContext(Dispatchers.IO) {
    runCatching {
        val decoded = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = false
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        ensureArgb8888(decoded)
    }
}

fun cropBitmap(bitmap: Bitmap, rect: NormalizedRect): Bitmap {
    val source = ensureArgb8888(bitmap)
    val n = rect.normalized()
    val left = (n.left * source.width).toInt().coerceIn(0, source.width - 1)
    val top = (n.top * source.height).toInt().coerceIn(0, source.height - 1)
    val right = (n.right * source.width).toInt().coerceIn(left + 1, source.width)
    val bottom = (n.bottom * source.height).toInt().coerceIn(top + 1, source.height)
    val width = (right - left).coerceAtLeast(1)
    val height = (bottom - top).coerceAtLeast(1)
    return ensureArgb8888(Bitmap.createBitmap(source, left, top, width, height))
}

fun ensureArgb8888(bitmap: Bitmap): Bitmap {
    return if (bitmap.config == Bitmap.Config.ARGB_8888) {
        bitmap
    } else {
        bitmap.copy(Bitmap.Config.ARGB_8888, false)
            ?: throw IllegalStateException("Failed to convert bitmap to ARGB_8888.")
    }
}

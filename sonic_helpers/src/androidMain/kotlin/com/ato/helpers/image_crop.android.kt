package com.ato.helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.min
import kotlin.math.roundToInt

actual fun imagePixelSize(bytes: ByteArray): ImagePixelSize? {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

    if (options.outWidth <= 0 || options.outHeight <= 0) return null
    return ImagePixelSize(width = options.outWidth, height = options.outHeight)
}

actual fun cropImageToSquare(
    bytes: ByteArray,
    rect: NormalizedCropRect,
    outputSizePx: Int,
): ByteArray? {
    val source = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

    return try {
        val left = (rect.left * source.width).roundToInt().coerceIn(0, source.width - 1)
        val top = (rect.top * source.height).roundToInt().coerceIn(0, source.height - 1)

        // Сторону берём по меньшему из двух: округление долей вверх по обеим
        // осям вылезло бы за край, а `createBitmap` на это отвечает
        // исключением, а не обрезкой.
        val side = min(
            (rect.right * source.width).roundToInt() - left,
            (rect.bottom * source.height).roundToInt() - top,
        )
            .coerceAtLeast(1)
            .coerceAtMost(min(source.width - left, source.height - top))

        val cropped = Bitmap.createBitmap(source, left, top, side, side)
        val scaled = if (side == outputSizePx) {
            cropped
        } else {
            Bitmap.createScaledBitmap(cropped, outputSizePx, outputSizePx, true)
        }

        ByteArrayOutputStream().use { output ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            output.toByteArray()
        }.also {
            if (scaled !== cropped) scaled.recycle()
            if (cropped !== source) cropped.recycle()
        }
    } catch (error: IllegalArgumentException) {
        null
    } finally {
        source.recycle()
    }
}

private const val JPEG_QUALITY = 90

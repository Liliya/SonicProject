package com.ato.helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Обе функции работают в **тех же координатах, в которых фотографию видит
 * человек**, а не в тех, в которых она лежит в файле.
 *
 * Разница неочевидная и дорогая. Снимок с камеры часто хранится повёрнутым, а
 * как его показывать — написано отдельным полем EXIF, которое `BitmapFactory`
 * игнорирует. Поэтому поворот применяется здесь, в одном месте, и одинаково для
 * превью и для обрезки: [decodeImage] и [cropImageToSquare] обязаны смотреть на
 * одни и те же пиксели, иначе кадр разъедется.
 */
actual fun decodeImage(bytes: ByteArray, maxSide: Int): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxSide)
    }
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return null

    return decoded.upright(exifOrientation(bytes)).asImageBitmap()
}

actual fun cropImageToSquare(
    bytes: ByteArray,
    rect: NormalizedCropRect,
    outputSizePx: Int,
): ByteArray? {
    // Резать по возможности из большего, но не из сотни мегабайт: вдвое больше
    // итоговой стороны хватает, чтобы уменьшение не портило картинку.
    val options = BitmapFactory.Options().apply {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, outputSizePx * 4)
    }
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return null
    val source = decoded.upright(exifOrientation(bytes))

    return try {
        val crop = rect.toPixelCrop(width = source.width, height = source.height)
        if (crop.side <= 0) return null

        val cropped = Bitmap.createBitmap(source, crop.left, crop.top, crop.side, crop.side)
        val scaled = if (crop.side == outputSizePx) {
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

private fun sampleSizeFor(width: Int, height: Int, maxSide: Int): Int {
    if (maxSide <= 0 || width <= 0 || height <= 0) return 1

    var sample = 1
    while (max(width, height) / sample > maxSide) sample *= 2
    return sample
}

/**
 * Поворот из EXIF, или «никакого» если его нет и если файл не читается.
 *
 * `android.media.ExifInterface`, а не androidx: та же информация, но без новой
 * зависимости в общем каталоге версий. Устарела в пользу androidx-версии —
 * если каталог всё равно будут трогать, это первый кандидат на замену.
 */
@Suppress("DEPRECATION")
private fun exifOrientation(bytes: ByteArray): Int = try {
    ByteArrayInputStream(bytes).use { stream ->
        ExifInterface(stream).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL,
        )
    }
} catch (error: Exception) {
    ExifInterface.ORIENTATION_NORMAL
}

/** Разворачивает пиксели так, как их показывают. */
@Suppress("DEPRECATION")
private fun Bitmap.upright(orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(270f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }

        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.setRotate(270f)
            matrix.postScale(-1f, 1f)
        }

        else -> return this
    }

    val upright = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    if (upright !== this) recycle()
    return upright
}

private const val JPEG_QUALITY = 90

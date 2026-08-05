package com.ato.helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Обе функции работают в **тех же координатах, в которых фотографию видит
 * человек**, а не в тех, в которых она лежит в файле.
 *
 * Разница неочевидная и дорогая. Снимок с камеры часто хранится повёрнутым, а
 * как его показывать — написано отдельным полем EXIF. `BitmapFactory` это поле
 * игнорирует, а загрузчик картинок (Coil) — применяет. Пока об этом не знали,
 * кружок кадрирования показывал фотографию правильно, геометрия считала кадр по
 * неповёрнутым пикселям, и на аватарке оказывалось не то, что человек выбрал
 * пальцем: у повёрнутого снимка ширина с высотой меняются местами, то есть
 * системы координат расходятся целиком, а не на пару пикселей.
 *
 * Поэтому [imagePixelSize] отдаёт размеры уже с учётом поворота, а
 * [cropImageToSquare] сначала доворачивает картинку, и только потом режет.
 * Снимок без EXIF (а таким становится любой, который peekaboo пережал под свой
 * порог) проходит оба места без изменений.
 */
actual fun imagePixelSize(bytes: ByteArray): ImagePixelSize? {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

    if (options.outWidth <= 0 || options.outHeight <= 0) return null

    return if (exifOrientation(bytes).swapsSides()) {
        ImagePixelSize(width = options.outHeight, height = options.outWidth)
    } else {
        ImagePixelSize(width = options.outWidth, height = options.outHeight)
    }
}

actual fun cropImageToSquare(
    bytes: ByteArray,
    rect: NormalizedCropRect,
    outputSizePx: Int,
): ByteArray? {
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
    val source = decoded.upright(exifOrientation(bytes))

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

/** Меняет ли этот поворот ширину с высотой. */
@Suppress("DEPRECATION")
private fun Int.swapsSides(): Boolean = when (this) {
    ExifInterface.ORIENTATION_ROTATE_90,
    ExifInterface.ORIENTATION_ROTATE_270,
    ExifInterface.ORIENTATION_TRANSPOSE,
    ExifInterface.ORIENTATION_TRANSVERSE -> true

    else -> false
}

/** Разворачивает пиксели так, как их показывает загрузчик картинок. */
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

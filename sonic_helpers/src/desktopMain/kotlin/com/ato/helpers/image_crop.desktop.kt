package com.ato.helpers

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * JVM-реализация на `ImageIO`, а не на Skia.
 *
 * Skia на десктопе тоже есть — она приезжает вместе с Compose, — но `ImageIO`
 * входит в JDK, и это единственная из трёх реализаций, которую CI действительно
 * выполняет (`:sonic_helpers:desktopTest`). Обрезка проверяется там же, где
 * считается геометрия, а не только глазами на телефоне.
 *
 * Поворот из EXIF здесь **не применяется**, в отличие от Android-реализации:
 * `ImageIO` его игнорирует, десктопная цель приложения сейчас не собирается
 * вообще, и подгонять поведение под несуществующий экран смысла нет. Если она
 * оживёт, повёрнутый снимок обрежется не по тому кадру — начинать надо будет
 * отсюда.
 */
actual fun decodeImage(bytes: ByteArray, maxSide: Int): ImageBitmap? {
    val source = readImage(bytes) ?: return null

    return source.limitedTo(maxSide).toComposeImageBitmap()
}

actual fun cropImageToSquare(
    bytes: ByteArray,
    rect: NormalizedCropRect,
    outputSizePx: Int,
): ByteArray? {
    val source = readImage(bytes) ?: return null

    val crop = rect.toPixelCrop(width = source.width, height = source.height)
    if (crop.side <= 0) return null

    // TYPE_INT_RGB, а не ARGB: JPEG прозрачность не умеет, и картинка с
    // альфа-каналом записалась бы с перевранными цветами.
    val output = BufferedImage(outputSizePx, outputSizePx, BufferedImage.TYPE_INT_RGB)
    val graphics = output.createGraphics()
    try {
        graphics.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR,
        )
        graphics.drawImage(
            source.getSubimage(crop.left, crop.top, crop.side, crop.side),
            0, 0, outputSizePx, outputSizePx,
            null,
        )
    } finally {
        graphics.dispose()
    }

    return ByteArrayOutputStream().use { stream ->
        if (!ImageIO.write(output, "jpg", stream)) return null
        stream.toByteArray()
    }
}

private fun readImage(bytes: ByteArray): BufferedImage? = try {
    ImageIO.read(ByteArrayInputStream(bytes))
} catch (error: Exception) {
    null
}

/**
 * Уменьшение до [maxSide] по большей стороне, с сохранением пропорций.
 *
 * `internal`, чтобы это проверялось тестом: сам [decodeImage] на JVM в unit-тесте
 * не выполнить — `toComposeImageBitmap` тянет нативную библиотеку skiko, которой
 * в тестовом classpath нет и которую незачем туда тащить ради одной функции.
 */
internal fun BufferedImage.limitedTo(maxSide: Int): BufferedImage {
    if (maxSide <= 0 || max(width, height) <= maxSide) return this

    val factor = maxSide.toFloat() / max(width, height)
    val target = BufferedImage(
        (width * factor).roundToInt().coerceAtLeast(1),
        (height * factor).roundToInt().coerceAtLeast(1),
        BufferedImage.TYPE_INT_RGB,
    )
    val graphics = target.createGraphics()
    try {
        graphics.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR,
        )
        graphics.drawImage(this, 0, 0, target.width, target.height, null)
    } finally {
        graphics.dispose()
    }
    return target
}

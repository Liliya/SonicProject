package com.ato.helpers

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * JVM-реализация на `ImageIO`, а не на Skia.
 *
 * Skia на десктопе тоже есть — она приезжает вместе с Compose, — но `ImageIO`
 * входит в JDK, и это единственная из трёх реализаций, которую CI действительно
 * выполняет (`:sonic_helpers:desktopTest`). Обрезка проверяется там же, где
 * считается геометрия, а не только глазами на телефоне.
 */
actual fun imagePixelSize(bytes: ByteArray): ImagePixelSize? {
    // `createImageInputStream` объявлен в Java и вернуть null может — Kotlin про
    // это не знает и молча пропустил бы `.use` до NPE.
    val stream = ImageIO.createImageInputStream(ByteArrayInputStream(bytes)) ?: return null

    stream.use {
        val reader = ImageIO.getImageReaders(stream).asSequence().firstOrNull() ?: return null
        return try {
            reader.input = stream
            ImagePixelSize(width = reader.getWidth(0), height = reader.getHeight(0))
        } catch (error: Exception) {
            null
        } finally {
            reader.dispose()
        }
    }
}

actual fun cropImageToSquare(
    bytes: ByteArray,
    rect: NormalizedCropRect,
    outputSizePx: Int,
): ByteArray? {
    val source = try {
        ImageIO.read(ByteArrayInputStream(bytes))
    } catch (error: Exception) {
        null
    } ?: return null

    val left = (rect.left * source.width).roundToInt().coerceIn(0, source.width - 1)
    val top = (rect.top * source.height).roundToInt().coerceIn(0, source.height - 1)
    val side = min(
        (rect.right * source.width).roundToInt() - left,
        (rect.bottom * source.height).roundToInt() - top,
    )
        .coerceAtLeast(1)
        .coerceAtMost(min(source.width - left, source.height - top))

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
            source.getSubimage(left, top, side, side),
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

package com.ato.helpers

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy
import kotlin.math.roundToInt

/**
 * iOS-реализация через UIKit.
 *
 * Рисование `UIImage` в контекст выбрано намеренно вместо вырезания из
 * `CGImage`: снимок с камеры приезжает лежащим на боку, а поворот описан
 * отдельным полем `imageOrientation`. `CGImage` про это поле не знает и отдал
 * бы кадр из повёрнутой картинки — то есть не тот, который человек только что
 * выбрал пальцем. `drawInRect` ориентацию применяет сам.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun imagePixelSize(bytes: ByteArray): ImagePixelSize? {
    val image = UIImage.imageWithData(bytes.toNSData()) ?: return null

    return image.size.useContents {
        val scale = image.scale
        val pixelWidth = (width * scale).roundToInt()
        val pixelHeight = (height * scale).roundToInt()

        if (pixelWidth <= 0 || pixelHeight <= 0) {
            null
        } else {
            ImagePixelSize(width = pixelWidth, height = pixelHeight)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun cropImageToSquare(
    bytes: ByteArray,
    rect: NormalizedCropRect,
    outputSizePx: Int,
): ByteArray? {
    val image = UIImage.imageWithData(bytes.toNSData()) ?: return null

    // Размер в точках: доли из [NormalizedCropRect] от единиц не зависят,
    // поэтому переводить в пиксели тут незачем.
    val sizeInPoints = image.size.useContents { width to height }
    val (widthPoints, heightPoints) = sizeInPoints
    if (widthPoints <= 0.0 || heightPoints <= 0.0) return null

    val cropWidth = (rect.right - rect.left) * widthPoints
    if (cropWidth <= 0.0) return null

    val output = outputSizePx.toDouble()
    val scale = output / cropWidth

    UIGraphicsBeginImageContextWithOptions(CGSizeMake(output, output), true, 1.0)
    image.drawInRect(
        CGRectMake(
            x = -rect.left * widthPoints * scale,
            y = -rect.top * heightPoints * scale,
            width = widthPoints * scale,
            height = heightPoints * scale,
        )
    )
    val cropped = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    return cropped
        ?.let { UIImageJPEGRepresentation(it, JPEG_QUALITY) }
        ?.toByteArray()
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(bytes = allocArrayOf(this@toNSData), length = this@toNSData.size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size <= 0) return ByteArray(0)

    return ByteArray(size).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
        }
    }
}

private const val JPEG_QUALITY = 0.9

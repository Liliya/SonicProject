package com.ato.helpers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * [toPixelCrop] — единственное место, где доли превращаются в пиксели, и им
 * пользуются оба: и кружок кадрирования, и платформенная обрезка.
 *
 * Раньше каждый считал сам, и расхождение между «что видно» и «что вырезано»
 * стоило трёх заходов.
 */
class PixelCropTest {

    @Test
    fun theWholeSquareOfASquareImage() {
        val crop = NormalizedCropRect.WHOLE.toPixelCrop(width = 500, height = 500)

        assertEquals(PixelCrop(left = 0, top = 0, side = 500), crop)
    }

    @Test
    fun theMiddleOfAWideImage() {
        val rect = NormalizedCropRect(left = 1000f / 6000f, top = 0f, right = 5000f / 6000f, bottom = 1f)

        val crop = rect.toPixelCrop(width = 6000, height = 4000)

        assertEquals(PixelCrop(left = 1000, top = 0, side = 4000), crop)
    }

    @Test
    fun theResultIsAlwaysSquare() {
        // Доли приходят из жеста и не обязаны давать одинаковые стороны после
        // округления. Обрезка отдаёт квадрат, значит квадрат надо выбрать здесь.
        val rect = NormalizedCropRect(left = 0.13f, top = 0.27f, right = 0.61f, bottom = 0.78f)

        val crop = rect.toPixelCrop(width = 1333, height = 999)

        assertTrue(crop.side > 0)
        assertTrue(crop.left + crop.side <= 1333, "вылезло по ширине: $crop")
        assertTrue(crop.top + crop.side <= 999, "вылезло по высоте: $crop")
    }

    @Test
    fun nothingEverPointsOutsideTheImage() {
        val rects = listOf(
            NormalizedCropRect.WHOLE,
            NormalizedCropRect(0f, 0f, 0f, 0f),
            NormalizedCropRect(1f, 1f, 1f, 1f),
            NormalizedCropRect(0.999f, 0.999f, 1f, 1f),
            NormalizedCropRect(-0.5f, -0.5f, 1.5f, 1.5f),
        )
        val sizes = listOf(1 to 1, 7 to 3, 1200 to 800, 800 to 1200)

        rects.forEach { rect ->
            sizes.forEach { (width, height) ->
                val crop = rect.toPixelCrop(width, height)

                assertTrue(crop.left >= 0, "$rect at $width×$height → $crop")
                assertTrue(crop.top >= 0, "$rect at $width×$height → $crop")
                assertTrue(crop.side >= 1, "$rect at $width×$height → $crop")
                assertTrue(crop.left + crop.side <= width, "$rect at $width×$height → $crop")
                assertTrue(crop.top + crop.side <= height, "$rect at $width×$height → $crop")
            }
        }
    }

    @Test
    fun anEmptyImageAsksForNothing() {
        assertEquals(PixelCrop(0, 0, 0), NormalizedCropRect.WHOLE.toPixelCrop(0, 0))
        assertEquals(PixelCrop(0, 0, 0), NormalizedCropRect.WHOLE.toPixelCrop(100, 0))
    }

    @Test
    fun theSameRectOnASmallerCopyOfTheImageGivesTheSameFrame() {
        // Превью рисуется по уменьшенной копии, обрезка — по большой. Кадр
        // обязан совпасть, иначе всё это опять разъедется.
        val rect = NormalizedCropRect(left = 0.2f, top = 0.1f, right = 0.7f, bottom = 0.85f)

        val big = rect.toPixelCrop(width = 4000, height = 3000)
        val small = rect.toPixelCrop(width = 1000, height = 750)

        assertEquals(big.left / 4000f, small.left / 1000f, 0.002f)
        assertEquals(big.top / 3000f, small.top / 750f, 0.002f)
        assertEquals(big.side / 4000f, small.side / 1000f, 0.002f)
    }
}

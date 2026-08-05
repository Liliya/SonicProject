package com.ato.helpers

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CropGeometryTest {

    private val viewport = 300f

    private fun assertClose(expected: Float, actual: Float, name: String) {
        assertTrue(
            abs(expected - actual) < 0.001f,
            "$name: ожидали $expected, получили $actual"
        )
    }

    private fun assertRect(
        rect: NormalizedCropRect,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        assertClose(left, rect.left, "left")
        assertClose(top, rect.top, "top")
        assertClose(right, rect.right, "right")
        assertClose(bottom, rect.bottom, "bottom")
    }

    @Test
    fun squarePhotoAtRestGivesTheWholeImage() {
        val rect = CropGeometry.cropRect(
            imageWidth = 1000,
            imageHeight = 1000,
            viewportSide = viewport,
            zoom = CropGeometry.MIN_ZOOM,
            offset = CropGeometry.centeredOffset(1000, 1000, viewport, CropGeometry.MIN_ZOOM),
        )

        assertRect(rect, 0f, 0f, 1f, 1f)
    }

    @Test
    fun widePhotoAtRestKeepsTheMiddleColumn() {
        // 2000x1000: в квадрат влезает половина ширины, по краям срезано поровну.
        val rect = CropGeometry.cropRect(
            imageWidth = 2000,
            imageHeight = 1000,
            viewportSide = viewport,
            zoom = CropGeometry.MIN_ZOOM,
            offset = CropGeometry.centeredOffset(2000, 1000, viewport, CropGeometry.MIN_ZOOM),
        )

        assertRect(rect, left = 0.25f, top = 0f, right = 0.75f, bottom = 1f)
    }

    @Test
    fun tallPhotoAtRestKeepsTheMiddleRow() {
        val rect = CropGeometry.cropRect(
            imageWidth = 1000,
            imageHeight = 4000,
            viewportSide = viewport,
            zoom = CropGeometry.MIN_ZOOM,
            offset = CropGeometry.centeredOffset(1000, 4000, viewport, CropGeometry.MIN_ZOOM),
        )

        assertRect(rect, left = 0f, top = 0.375f, right = 1f, bottom = 0.625f)
    }

    @Test
    fun zoomingInTakesLess() {
        val zoom = 2f
        val rect = CropGeometry.cropRect(
            imageWidth = 1000,
            imageHeight = 1000,
            viewportSide = viewport,
            zoom = zoom,
            offset = CropGeometry.centeredOffset(1000, 1000, viewport, zoom),
        )

        // Вдвое ближе — вчетверо меньше площади, то есть половина стороны.
        assertRect(rect, left = 0.25f, top = 0.25f, right = 0.75f, bottom = 0.75f)
    }

    @Test
    fun draggingMovesTheCropAndNotTheOtherWay() {
        // Картинка уехала влево — значит в кадр попала её правая часть.
        val zoom = 2f
        val centered = CropGeometry.centeredOffset(1000, 1000, viewport, zoom)
        val dragged = CropOffset(centered.x - 150f, centered.y)

        val rect = CropGeometry.cropRect(1000, 1000, viewport, zoom, dragged)

        assertRect(rect, left = 0.5f, top = 0.25f, right = 1f, bottom = 0.75f)
    }

    @Test
    fun theViewportIsNeverLeftUncovered() {
        // Сколько ни тащи — за край картинки кадр не выходит.
        val zoom = 1.5f
        val farTooFar = CropOffset(x = 10_000f, y = -10_000f)

        val rect = CropGeometry.cropRect(1200, 800, viewport, zoom, farTooFar)

        assertTrue(rect.left >= 0f, "left ${rect.left}")
        assertTrue(rect.top >= 0f, "top ${rect.top}")
        assertTrue(rect.right <= 1f, "right ${rect.right}")
        assertTrue(rect.bottom <= 1f, "bottom ${rect.bottom}")
    }

    @Test
    fun clampingKeepsTheImageAgainstTheEdges() {
        val zoom = 1f
        val clamped = CropGeometry.clampOffset(
            imageWidth = 2000,
            imageHeight = 1000,
            viewportSide = viewport,
            zoom = zoom,
            offset = CropOffset(x = 500f, y = 500f),
        )

        // Положительный сдвиг открыл бы пустое поле слева — его срезает в ноль.
        assertClose(0f, clamped.x, "x")
        assertClose(0f, clamped.y, "y")
    }

    @Test
    fun theCropIsAlwaysSquareInImagePixels() {
        val zoom = 1.7f
        val width = 1600
        val height = 900
        val rect = CropGeometry.cropRect(
            imageWidth = width,
            imageHeight = height,
            viewportSide = viewport,
            zoom = zoom,
            offset = CropGeometry.centeredOffset(width, height, viewport, zoom),
        )

        val sideX = (rect.right - rect.left) * width
        val sideY = (rect.bottom - rect.top) * height

        assertClose(sideX, sideY, "сторона в пикселях")
    }

    @Test
    fun zoomIsKeptWithinItsLimits() {
        assertEquals(CropGeometry.MIN_ZOOM, CropGeometry.clampZoom(0.2f))
        assertEquals(CropGeometry.MAX_ZOOM, CropGeometry.clampZoom(50f))
        assertEquals(2.5f, CropGeometry.clampZoom(2.5f))
    }

    @Test
    fun brokenImageSizesDoNotBlowUp() {
        assertRect(CropGeometry.cropRect(0, 0, viewport, 1f, CropOffset(0f, 0f)), 0f, 0f, 1f, 1f)
        assertRect(CropGeometry.cropRect(100, 100, 0f, 1f, CropOffset(0f, 0f)), 0f, 0f, 1f, 1f)
    }
}

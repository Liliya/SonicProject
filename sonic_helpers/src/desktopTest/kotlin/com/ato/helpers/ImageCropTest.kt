package com.ato.helpers

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Единственная из трёх реализаций обрезки, которую можно выполнить в CI.
 *
 * Android и iOS работают через свои графические API и проверяются на
 * устройстве, но сама договорённость — «доли на входе, квадратный JPEG на
 * выходе, вырезано то, что просили» — общая, и ломается она обычно в ней, а не
 * в платформенном вызове.
 */
class ImageCropTest {

    /** Картинка из четырёх цветных четвертей: по цвету видно, что именно вырезали. */
    private fun quarters(width: Int, height: Int): ByteArray {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        graphics.color = Color.RED
        graphics.fillRect(0, 0, width / 2, height / 2)
        graphics.color = Color.GREEN
        graphics.fillRect(width / 2, 0, width - width / 2, height / 2)
        graphics.color = Color.BLUE
        graphics.fillRect(0, height / 2, width / 2, height - height / 2)
        graphics.color = Color.WHITE
        graphics.fillRect(width / 2, height / 2, width - width / 2, height - height / 2)
        graphics.dispose()

        return ByteArrayOutputStream().use { stream ->
            ImageIO.write(image, "png", stream)
            stream.toByteArray()
        }
    }

    private fun ByteArray.decoded(): BufferedImage = ImageIO.read(inputStream())

    @Test
    fun sizeIsReadWithoutDecodingTheWholeImage() {
        assertEquals(ImagePixelSize(640, 480), imagePixelSize(quarters(640, 480)))
    }

    @Test
    fun garbageIsNotAnImage() {
        assertNull(imagePixelSize(byteArrayOf(1, 2, 3, 4)))
        assertNull(cropImageToSquare(byteArrayOf(1, 2, 3, 4), NormalizedCropRect.WHOLE, 64))
    }

    @Test
    fun outputIsASquareJpegOfTheRequestedSize() {
        val cropped = cropImageToSquare(quarters(800, 600), NormalizedCropRect.WHOLE, outputSizePx = 128)

        assertNotNull(cropped)
        // FF D8 — сигнатура JPEG, по ней `data_storage` выбирает расширение файла.
        assertEquals(0xFF.toByte(), cropped[0])
        assertEquals(0xD8.toByte(), cropped[1])

        val image = cropped.decoded()
        assertEquals(128, image.width)
        assertEquals(128, image.height)
    }

    @Test
    fun theRequestedCornerIsWhatComesOut() {
        // Правая нижняя четверть картинки — белая.
        val bottomRight = NormalizedCropRect(left = 0.5f, top = 0.5f, right = 1f, bottom = 1f)

        val cropped = cropImageToSquare(quarters(400, 400), bottomRight, outputSizePx = 64)

        assertNotNull(cropped)
        val middle = Color(cropped.decoded().getRGB(32, 32))
        assertTrue(middle.red > 200 && middle.green > 200 && middle.blue > 200, "получили $middle")
    }

    @Test
    fun anotherCornerToProveItIsNotAlwaysTheSameOne() {
        val topLeft = NormalizedCropRect(left = 0f, top = 0f, right = 0.5f, bottom = 0.5f)

        val cropped = cropImageToSquare(quarters(400, 400), topLeft, outputSizePx = 64)

        assertNotNull(cropped)
        val middle = Color(cropped.decoded().getRGB(32, 32))
        assertTrue(middle.red > 200 && middle.green < 60 && middle.blue < 60, "получили $middle")
    }

    @Test
    fun theGeometryAndTheCropAgree() {
        // То, что насчитал [CropGeometry], должно проходить через обрезку без
        // подгонки на месте — эти двое общаются только через доли.
        val width = 1200
        val height = 800
        val viewport = 320f
        val zoom = 1.4f

        val rect = CropGeometry.cropRect(
            imageWidth = width,
            imageHeight = height,
            viewportSide = viewport,
            zoom = zoom,
            offset = CropGeometry.centeredOffset(width, height, viewport, zoom),
        )

        val cropped = cropImageToSquare(quarters(width, height), rect, outputSizePx = 200)

        assertNotNull(cropped)
        assertEquals(200, cropped.decoded().width)
        assertEquals(200, cropped.decoded().height)
    }

    /**
     * Что кружок показывал, то и должно вырезаться.
     *
     * Тесты выше проверяют размеры, а разъехаться может кадр: на телефоне
     * дважды выходило, что подвинутое в кружке не совпадало с аватаркой. Здесь
     * цепочка «геометрия → доли → пиксели» проверяется по содержимому — по
     * тому, где оказалась граница цветных четвертей.
     *
     * Картинка нарочно горизонтальная: у неё есть свобода по горизонтали и нет
     * по вертикали, так что перепутанные оси видно сразу.
     */
    @Test
    fun theCropShowsTheSamePartOfTheImageTheViewportDid() {
        val width = 1200
        val height = 800
        val viewport = 300f

        // Ничего не двигали: в квадрат попадает середина по ширине, вся высота.
        val rect = CropGeometry.cropRect(
            imageWidth = width,
            imageHeight = height,
            viewportSide = viewport,
            zoom = CropGeometry.MIN_ZOOM,
            offset = CropGeometry.centeredOffset(width, height, viewport, CropGeometry.MIN_ZOOM),
        )

        val image = cropImageToSquare(quarters(width, height), rect, outputSizePx = 200)
        assertNotNull(image)
        val cropped = image.decoded()

        // Середина горизонтальной картинки — это по-прежнему стык всех четырёх
        // четвертей ровно в центре кадра.
        assertQuarter(cropped, 50, 50, "red")
        assertQuarter(cropped, 150, 50, "green")
        assertQuarter(cropped, 50, 150, "blue")
        assertQuarter(cropped, 150, 150, "white")
    }

    @Test
    fun draggingRightBringsTheLeftOfTheImageIntoTheCrop() {
        val width = 1200
        val height = 800
        val viewport = 300f
        val centered = CropGeometry.centeredOffset(width, height, viewport, CropGeometry.MIN_ZOOM)

        // Картинку потащили вправо — значит в кадр приезжает её левая часть, и
        // граница четвертей уходит правее. Знак здесь и есть то, что ломается
        // молча: тест поймает и перепутанное направление, и перепутанную ось.
        val dragged = CropOffset(centered.x + 60f, centered.y)
        val rect = CropGeometry.cropRect(width, height, viewport, CropGeometry.MIN_ZOOM, dragged)

        val image = cropImageToSquare(quarters(width, height), rect, outputSizePx = 200)
        assertNotNull(image)
        val cropped = image.decoded()

        // Без сдвига граница стоит на x = 100, со сдвигом — на 140.
        assertQuarter(cropped, 120, 50, "red")
        assertQuarter(cropped, 160, 50, "green")
    }

    private fun assertQuarter(image: BufferedImage, x: Int, y: Int, expected: String) {
        val pixel = Color(image.getRGB(x, y))
        val bright = 170
        val dim = 90
        val actual = when {
            pixel.red > bright && pixel.green > bright && pixel.blue > bright -> "white"
            pixel.red > bright && pixel.green < dim && pixel.blue < dim -> "red"
            pixel.green > bright && pixel.red < dim && pixel.blue < dim -> "green"
            pixel.blue > bright && pixel.red < dim && pixel.green < dim -> "blue"
            else -> "$pixel"
        }

        assertEquals(expected, actual, "в точке ($x, $y)")
    }

    @Test
    fun aCropSmallerThanOnePixelStillProducesAnImage() {
        // Доли приходят из жеста, и нулевую площадь легко получить руками.
        val degenerate = NormalizedCropRect(left = 0.5f, top = 0.5f, right = 0.5f, bottom = 0.5f)

        val cropped = cropImageToSquare(quarters(400, 400), degenerate, outputSizePx = 32)

        assertNotNull(cropped)
        assertEquals(32, cropped.decoded().width)
    }
}

package com.ato.helpers

import kotlin.math.max
import kotlin.math.min

/** Размер картинки в пикселях, как её видит платформа. */
data class ImagePixelSize(val width: Int, val height: Int)

/**
 * Какую часть картинки оставить, долями от её ширины и высоты.
 *
 * Доли, а не пиксели, потому что считает это UI, который знает про картинку
 * только пропорции: настоящий размер вылезает уже внутри платформенной
 * реализации, при декодировании.
 */
data class NormalizedCropRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    companion object {
        /**
         * Вся картинка целиком.
         *
         * Осторожно: обрезка отдаёт **квадрат**, поэтому на неквадратной
         * картинке это не «всё как было», а квадрат от левого верхнего угла.
         * Заглушкой на случай «не знаем размеров» этот прямоугольник быть не
         * может — там нужен файл без обрезки, иначе кадр разойдётся с тем, что
         * человек видел в кружке.
         */
        val WHOLE = NormalizedCropRect(0f, 0f, 1f, 1f)
    }
}

/** Смещение картинки внутри окошка кадрирования, в пикселях раскладки. */
data class CropOffset(val x: Float, val y: Float)

/**
 * Арифметика круглого кадрирования: где сейчас лежит картинка и что из неё
 * вырежется.
 *
 * Вынесено из composable отдельно и без единого типа из Compose — иначе это не
 * проверить ничем, кроме глаз, а ошибка тут стоит криво обрезанного лица.
 *
 * Модель простая. Окошко — квадрат со стороной `viewportSide`. Картинка
 * вписана в него «по меньшей стороне», так что при [MIN_ZOOM] окошко закрыто
 * целиком и пустых полей не бывает никогда. [CropOffset] — сдвиг левого
 * верхнего угла картинки относительно левого верхнего угла окошка; он всегда
 * отрицательный или нулевой, потому что картинка не меньше окошка.
 */
object CropGeometry {

    /** Меньше единицы — это уже поля по краям круга, а не кадрирование. */
    const val MIN_ZOOM: Float = 1f

    /** Дальше растягивается уже не картинка, а её пиксели. */
    const val MAX_ZOOM: Float = 5f

    /**
     * Во сколько раз картинку надо увеличить, чтобы она закрыла окошко при
     * [MIN_ZOOM].
     */
    fun coverScale(imageWidth: Int, imageHeight: Int, viewportSide: Float): Float {
        val smallestSide = min(imageWidth, imageHeight)
        if (smallestSide <= 0) return 1f
        return viewportSide / smallestSide
    }

    fun displayWidth(imageWidth: Int, imageHeight: Int, viewportSide: Float, zoom: Float): Float =
        imageWidth * coverScale(imageWidth, imageHeight, viewportSide) * zoom

    fun displayHeight(imageWidth: Int, imageHeight: Int, viewportSide: Float, zoom: Float): Float =
        imageHeight * coverScale(imageWidth, imageHeight, viewportSide) * zoom

    fun clampZoom(zoom: Float): Float = zoom.coerceIn(MIN_ZOOM, MAX_ZOOM)

    /**
     * Ближайшее допустимое смещение: такое, при котором окошко всё ещё закрыто
     * картинкой.
     *
     * Без этого жест утаскивает картинку за край и в кадр попадает пустота.
     */
    fun clampOffset(
        imageWidth: Int,
        imageHeight: Int,
        viewportSide: Float,
        zoom: Float,
        offset: CropOffset,
    ): CropOffset {
        val width = displayWidth(imageWidth, imageHeight, viewportSide, zoom)
        val height = displayHeight(imageWidth, imageHeight, viewportSide, zoom)

        // Нижняя граница отрицательная, верхняя — ноль. `max`, а не `coerceIn`,
        // потому что из-за округления ширина может оказаться на волос меньше
        // окошка, и тогда границы поменялись бы местами.
        val minX = min(viewportSide - width, 0f)
        val minY = min(viewportSide - height, 0f)

        return CropOffset(
            x = offset.x.coerceIn(minX, 0f),
            y = offset.y.coerceIn(minY, 0f),
        )
    }

    /** Смещение, при котором в окошке середина картинки. */
    fun centeredOffset(
        imageWidth: Int,
        imageHeight: Int,
        viewportSide: Float,
        zoom: Float,
    ): CropOffset {
        val width = displayWidth(imageWidth, imageHeight, viewportSide, zoom)
        val height = displayHeight(imageWidth, imageHeight, viewportSide, zoom)

        return CropOffset(
            x = min((viewportSide - width) / 2f, 0f),
            y = min((viewportSide - height) / 2f, 0f),
        )
    }

    /**
     * Что из картинки попало в окошко.
     *
     * Результат подрезан по краям картинки: без этого пара пикселей округления
     * превращается в запрос выйти за границу изображения, а платформенные
     * функции обрезки на такое отвечают исключением.
     */
    fun cropRect(
        imageWidth: Int,
        imageHeight: Int,
        viewportSide: Float,
        zoom: Float,
        offset: CropOffset,
    ): NormalizedCropRect {
        if (imageWidth <= 0 || imageHeight <= 0 || viewportSide <= 0f) {
            return NormalizedCropRect.WHOLE
        }

        val scale = coverScale(imageWidth, imageHeight, viewportSide) * zoom
        val clamped = clampOffset(imageWidth, imageHeight, viewportSide, zoom, offset)

        val sideInImagePixels = viewportSide / scale
        val left = -clamped.x / scale
        val top = -clamped.y / scale

        return NormalizedCropRect(
            left = max(left / imageWidth, 0f),
            top = max(top / imageHeight, 0f),
            right = min((left + sideInImagePixels) / imageWidth, 1f),
            bottom = min((top + sideInImagePixels) / imageHeight, 1f),
        )
    }
}

/**
 * Размер картинки в пикселях, или `null` если это вообще не картинка.
 *
 * Декодировать целиком ради двух чисел не надо: на Android для этого есть
 * `inJustDecodeBounds`, на JVM — заголовок через `ImageIO`.
 */
expect fun imagePixelSize(bytes: ByteArray): ImagePixelSize?

/**
 * Вырезает из [bytes] квадрат [rect] и приводит его к стороне [outputSizePx].
 *
 * Отдаёт JPEG, поэтому загруженный файл получает расширение `.jpg` —
 * `getFileExtensionBySignature` в `data_storage` определяет его по сигнатуре, а
 * не по имени.
 *
 * Возвращает `null`, если картинку не удалось раскодировать: вызывающий на
 * этом показывает ошибку, а не падает.
 */
expect fun cropImageToSquare(
    bytes: ByteArray,
    rect: NormalizedCropRect,
    outputSizePx: Int = AVATAR_OUTPUT_SIZE_PX,
): ByteArray?

/**
 * Сторона сохраняемой аватарки.
 *
 * Самое крупное место — кружок 256dp на экране профиля: на плотности 3x это
 * 768 физических пикселей. 720 чуть меньше, и разница в четыре процента не
 * видна, зато файл выходит ~60–90 КБ вместо мегабайтов, которые сейчас летят в
 * Storage как есть — а платит за них каждый, кто открыл список людей.
 */
const val AVATAR_OUTPUT_SIZE_PX: Int = 720

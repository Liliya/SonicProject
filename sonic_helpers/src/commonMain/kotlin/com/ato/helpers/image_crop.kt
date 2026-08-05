package com.ato.helpers

import androidx.compose.ui.graphics.ImageBitmap
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Какую часть картинки оставить, долями от её ширины и высоты.
 *
 * Доли, а не пиксели, потому что считает это UI, который знает про картинку
 * только пропорции: настоящий размер вылезает уже при декодировании.
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
         * Заглушкой на случай «не смогли раскодировать» этот прямоугольник быть
         * не может — там нужен файл без обрезки, иначе кадр разойдётся с тем,
         * что человек видел в кружке.
         */
        val WHOLE = NormalizedCropRect(0f, 0f, 1f, 1f)
    }
}

/** Квадрат в пикселях картинки: то, что реально вырезается. */
data class PixelCrop(val left: Int, val top: Int, val side: Int)

/**
 * Переводит доли в квадрат пикселей.
 *
 * **Одна функция на всех.** Ею считает и превью в кружке, и платформенная
 * обрезка. Пока каждый считал сам, «что видно в кружке» и «что вырезалось»
 * разъезжались, причём молча и по-разному на разных пропорциях.
 *
 * Сторона берётся по меньшему из двух измерений: округление долей вверх по обеим
 * осям вылезло бы за край, а платформенные функции обрезки отвечают на это
 * исключением, а не обрезкой.
 */
fun NormalizedCropRect.toPixelCrop(width: Int, height: Int): PixelCrop {
    if (width <= 0 || height <= 0) return PixelCrop(left = 0, top = 0, side = 0)

    val x = (left * width).roundToInt().coerceIn(0, width - 1)
    val y = (top * height).roundToInt().coerceIn(0, height - 1)
    val side = min(
        (right * width).roundToInt() - x,
        (bottom * height).roundToInt() - y,
    )
        .coerceAtLeast(1)
        .coerceAtMost(min(width - x, height - y))

    return PixelCrop(left = x, top = y, side = side)
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

        // Нижняя граница отрицательная, верхняя — ноль. `min`, а не `coerceIn`,
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
     * превращается в запрос выйти за границу изображения.
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
 * Раскодированная картинка для показа в кадрировании, или `null` если это вообще
 * не картинка.
 *
 * Нужна именно битмапа, а не ссылка для загрузчика картинок: кружок рисует её
 * сам, указывая исходный прямоугольник — тот же самый, который уйдёт в
 * [cropImageToSquare]. Пока кадр задавался размером и сдвигом composable-а,
 * раскладка жила по своим правилам, и «что в кружке» с «что вырезано»
 * расходились.
 *
 * Размер уменьшается до [maxSide] по большей стороне: снимок на 24 мегапикселя
 * в виде битмапы — это около сотни мегабайт, а для кружка на 320dp это мусор.
 * На кадр это не влияет, потому что кадр считается в долях.
 *
 * Поворот из EXIF применяется здесь же, ровно как в [cropImageToSquare] — иначе
 * превью и результат опять смотрели бы на разные пиксели.
 */
expect fun decodeImage(bytes: ByteArray, maxSide: Int = DECODE_MAX_SIDE): ImageBitmap?

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
 * До какого размера уменьшать картинку для показа в кадрировании.
 *
 * Кружок редко больше 320dp, то есть меньше 1000 физических пикселей даже на
 * плотных экранах; 2048 оставляет запас на приближение щипком и при этом держит
 * битмапу в пределах ~16 МБ.
 */
const val DECODE_MAX_SIDE: Int = 2048

/**
 * Сторона сохраняемой аватарки.
 *
 * Самое крупное место — кружок 256dp на экране профиля: на плотности 3x это
 * 768 физических пикселей. 720 чуть меньше, и разница в четыре процента не
 * видна, зато файл выходит ~60–90 КБ вместо мегабайтов, которые сейчас летят в
 * Storage как есть — а платит за них каждый, кто открыл список людей.
 */
const val AVATAR_OUTPUT_SIZE_PX: Int = 720

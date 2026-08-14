package com.ato.sonic_ui.base.image

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.ato.helpers.getAsyncImageLoader
import com.ato.helpers.getPlatformContext
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage

/**
 * Картинка желания — или тихая заглушка на её месте.
 *
 * Желание без картинки раньше не рисовало ничего, и строка списка становилась
 * на 60dp ниже соседней: ряд из трёх желаний, где картинка есть у одного, ехал
 * ступеньками. Ту же беду у людей давно решают пресеты аватарок, а у досок —
 * [BoardPresets]; третьего способа заводить не стали, но и рисовать желанию
 * цветную картинку из ниоткуда — плохо: она соревновалась бы за внимание с
 * настоящими фотографиями соседей по списку.
 *
 * Поэтому заглушка тихая: бледно-зелёная подложка бренд-цвета и приглушённый
 * подарок на ней. Её задача — занять место и не спорить с соседями по списку.
 *
 * Порядок как у [BoardPicture]: [file] важнее [url]. Только что выбранный файл
 * ещё не уехал в хранилище, и без этого правила он не показывался бы до
 * «Сохранить».
 */
@Composable
fun WishPicture(
    url: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    file: ByteArray? = null,
    contentDescription: String? = null,
) {
    val hasPicture = file != null || !url.isNullOrBlank()
    val label = contentDescription
        ?.let { text -> Modifier.semantics { this.contentDescription = text } }
        ?: Modifier

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .then(label),
        contentAlignment = Alignment.Center,
    ) {
        if (hasPicture) {
            CoilImage(
                imageLoader = { getAsyncImageLoader(getPlatformContext()) },
                imageModel = { file ?: url },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                ),
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            WishPicturePlaceholder(modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * Заглушка отдельно от [WishPicture]: её ставят и туда, где картинки нет и не
 * будет — например под миниатюру в ряду выбора.
 */
@Composable
fun WishPicturePlaceholder(modifier: Modifier = Modifier) {
    // Мятная подложка вместо серой: серое читалось как «не загрузилось», а
    // бледно-зелёное — как своё место в этом приложении. Прозрачность, а не
    // готовый тон, чтобы заглушка одинаково легла и на карточку списка, и на
    // фон экрана, и в обеих темах.
    val background = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    val glyph = MaterialTheme.colorScheme.primary.copy(alpha = 0.32f)

    Canvas(modifier = modifier.background(background)) {
        val side = size.minDimension * 0.46f
        translate(left = (size.width - side) / 2f, top = (size.height - side) / 2f) {
            drawWishGift(side, glyph)
        }
    }
}

/**
 * Подарок в квадрате `[0, side] x [0, side]`; сдвиг в центр уже сделан
 * вызывающим.
 *
 * Первый вариант был сложен из трёх прямоугольников на глаз: лента торчала
 * выше крышки и обрывалась в воздухе, скругление в 6% на полоске высотой в 20%
 * скругляло её почти в овал, а бант отсутствовал вовсе — получалась коробка,
 * перечёркнутая палкой. Здесь доли согласованы между собой: лента ровно
 * посередине и ровно от банта до низа коробки, крышка нависает над корпусом
 * одинаково с обеих сторон, скругления заданы от толщины своей детали, а не
 * общим числом.
 */
private fun DrawScope.drawWishGift(side: Float, color: Color) {
    val body = Offset(side * 0.13f, side * 0.44f)
    val bodySize = Size(side * 0.74f, side * 0.50f)
    val lidSize = Size(side * 0.86f, side * 0.14f)
    val ribbonWidth = side * 0.13f

    // корпус
    drawRoundRect(
        color = color,
        topLeft = body,
        size = bodySize,
        cornerRadius = CornerRadius(side * 0.05f, side * 0.05f),
    )
    // крышка — нависает над корпусом на 6% с каждой стороны
    drawRoundRect(
        color = color,
        topLeft = Offset(side * 0.07f, side * 0.30f),
        size = lidSize,
        cornerRadius = CornerRadius(side * 0.04f, side * 0.04f),
    )
    // лента: от нижнего края банта до дна коробки, без выступов
    drawRoundRect(
        color = color,
        topLeft = Offset((side - ribbonWidth) / 2f, side * 0.30f),
        size = Size(ribbonWidth, side * 0.64f),
        cornerRadius = CornerRadius(side * 0.02f, side * 0.02f),
    )
    // Бант — две петли контуром. Контуром, а не заливкой: залитые овалы на
    // 48dp сливаются в кляксу, а тонкие кольца читаются бантом даже там.
    val loopWidth = side * 0.30f
    val loopHeight = side * 0.24f
    val stroke = Stroke(width = side * 0.07f)
    drawOval(
        color = color,
        topLeft = Offset(side * 0.16f, side * 0.06f),
        size = Size(loopWidth, loopHeight),
        style = stroke,
    )
    drawOval(
        color = color,
        topLeft = Offset(side * 0.54f, side * 0.06f),
        size = Size(loopWidth, loopHeight),
        style = stroke,
    )
}

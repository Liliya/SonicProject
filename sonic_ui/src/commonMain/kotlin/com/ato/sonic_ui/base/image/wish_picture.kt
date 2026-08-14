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
 * Поэтому заглушка нарочно скучная: подложка на тон плотнее фона и приглушённый
 * силуэт подарка. Её задача — занять место, а не понравиться.
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
    val background = MaterialTheme.colorScheme.surfaceContainerHigh
    val glyph = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)

    Canvas(modifier = modifier.background(background)) {
        val side = size.minDimension * 0.42f
        translate(left = (size.width - side) / 2f, top = (size.height - side) / 2f) {
            drawWishGift(side, glyph)
        }
    }
}

/**
 * Подарок в квадрате `[0, side] x [0, side]`; сдвиг в центр уже сделан
 * вызывающим.
 *
 * Одним цветом и без прорезей, в отличие от подарка на картинке доски: там он
 * лежит на цветной подложке и держит картинку, здесь — приглушённый силуэт,
 * которому детали ни к чему.
 */
private fun DrawScope.drawWishGift(side: Float, color: Color) {
    val corner = CornerRadius(side * 0.06f, side * 0.06f)

    // корпус
    drawRoundRect(
        color = color,
        topLeft = Offset(side * 0.10f, side * 0.40f),
        size = Size(side * 0.80f, side * 0.55f),
        cornerRadius = corner,
    )
    // крышка чуть шире корпуса
    drawRoundRect(
        color = color,
        topLeft = Offset(side * 0.02f, side * 0.24f),
        size = Size(side * 0.96f, side * 0.20f),
        cornerRadius = corner,
    )
    // лента поперёк крышки и корпуса
    drawRoundRect(
        color = color,
        topLeft = Offset(side * 0.43f, side * 0.05f),
        size = Size(side * 0.14f, side * 0.90f),
        cornerRadius = corner,
    )
}

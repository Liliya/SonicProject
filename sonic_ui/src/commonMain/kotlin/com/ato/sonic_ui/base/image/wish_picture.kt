package com.ato.sonic_ui.base.image

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
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
 * Что стоит на месте картинки — см. [WishImagePlaceholder]: она тихая
 * намеренно, её задача занять место, а не спорить с соседями по списку.
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
            // Заглушка — общая на всё приложение: свой подарок здесь означал
            // бы два разных подарка от одной руки на одном экране.
            WishImagePlaceholder(size = size.value, shape = shape)
        }
    }
}

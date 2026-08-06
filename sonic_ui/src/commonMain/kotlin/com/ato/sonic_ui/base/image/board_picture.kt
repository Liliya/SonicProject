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
import androidx.compose.ui.unit.Dp
import com.ato.helpers.getAsyncImageLoader
import com.ato.helpers.getPlatformContext
import com.ato.ui_state.base.image.BoardPresets
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage

/**
 * Картинка доски: своя фотография, встроенный пресет или запасной пресет.
 *
 * Все три случая живут в одном поле доски, поэтому и разбираются в одном
 * месте: иначе каждый из четырёх экранов, где доска показывается, решал бы
 * «пресет это или ссылка» сам, и они разошлись бы на первом же изменении.
 *
 * Порядок важен: [file] важнее [value]. Только что выбранный из галереи файл
 * ещё не загружен в Storage, а в [value] всё это время лежит прошлое значение
 * — без этого правила выбранная картинка не появлялась бы до «Сохранить».
 *
 * @param value что сохранено у доски: `board://preset/N`, ссылка на файл,
 *   старое эмодзи или ничего.
 * @param seed идентификатор доски. По нему выбирается картинка той доске,
 *   которой ничего не выбирали.
 * @param file выбранный, но ещё не загруженный файл — показываем его.
 */
@Composable
fun BoardPicture(
    value: String?,
    seed: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    file: ByteArray? = null,
) {
    val preset = if (file == null) BoardPresets.resolve(value, seed) else null

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        if (preset != null) {
            BoardPresetImage(
                index = preset,
                shape = shape,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            CoilImage(
                imageLoader = { getAsyncImageLoader(getPlatformContext()) },
                imageModel = { file ?: value },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape),
            )
        }
    }
}

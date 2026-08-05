package com.ato.sonic_ui.base.image

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ato.helpers.getAsyncImageLoader
import com.ato.helpers.getPlatformContext
import com.ato.ui_state.base.image.AvatarPresets
import com.ato.ui_state.base.image.UiImagePicker
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage


/**
 * @param avatarSeed идентификатор человека, чью аватарку рисуем. Передан —
 *   значит вместо пустого кружка с «?» будет встроенный пресет
 *   ([AvatarPresets]); не передан — компонент ведёт себя как раньше, потому
 *   что этими же вызовами рисуются картинки желаний.
 */
@Composable
fun DisplayImage(
    imagePikerState: UiImagePicker,
    onImageClicked: () -> Unit = {},
    size: Float = 256f,
    sizeFactor: Float = 0.5f,
    shape: Shape = CircleShape,
    modifier: Modifier = Modifier,
    avatarSeed: String? = null,
    contentDescription: String? = null,
) {
    val data = imagePikerState.imageFile ?: imagePikerState.imageUrl
    // Только что выбранный файл важнее ссылки: пресет мог остаться в
    // `imageUrl` с прошлого сохранения.
    val preset = if (imagePikerState.imageFile == null) {
        AvatarPresets.resolve(imagePikerState.imageUrl, avatarSeed)
    } else {
        null
    }

    Box(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(color = Color.Transparent, shape = shape)
                .border(
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ), shape = shape
                )
                .size(size.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onImageClicked
                )
                .align(Alignment.Center)
                .avatarLabel(contentDescription)
        ) {
            when {
                preset != null -> AvatarPresetImage(
                    index = preset,
                    shape = shape,
                    modifier = Modifier.fillMaxSize(),
                )

                data == null -> Text(
                    text = "?",
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    textAlign = TextAlign.Center,
                    fontSize = (size * sizeFactor).sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                )

                else -> CoilImage(
                    imageLoader = { getAsyncImageLoader(getPlatformContext()) },
                    modifier = Modifier
                        .clip(shape)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.surface,
                            shape = shape
                        )
                        .size(size.dp),
                    imageModel = { data },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                )
            }
        }
    }
}

/**
 * Подпись для скринридера, если её передали.
 *
 * Пустая строка — это «картинка декоративная», ровно как в `base/icon.kt`:
 * подписывать кружок словом «изображение» хуже, чем промолчать.
 */
internal fun Modifier.avatarLabel(description: String?): Modifier =
    if (description.isNullOrEmpty()) {
        this
    } else {
        this.semantics { contentDescription = description }
    }

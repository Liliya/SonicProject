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
 *   значит вместо пустого кружка с «?» будет буква имени или встроенный пресет
 *   ([AvatarPresets]); не передан — компонент ведёт себя как раньше, потому
 *   что этими же вызовами рисуются картинки желаний.
 * @param avatarName имя человека — из него берётся буква на кружке. Не
 *   передано, а [avatarSeed] есть — рисуется пресет, как было раньше.
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
    avatarName: String? = null,
) {
    val data = imagePikerState.imageFile ?: imagePikerState.imageUrl
    val avatar = resolveAvatar(imagePikerState, avatarSeed, avatarName)

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
                avatar is Avatar.Letter -> LetterAvatarImage(
                    letter = avatar.letter,
                    seed = avatar.seed,
                    size = size,
                    shape = shape,
                    modifier = Modifier.fillMaxSize(),
                )

                avatar is Avatar.Preset -> AvatarPresetImage(
                    index = avatar.index,
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

                // Обводки у самой фотографии нет: рамку в 1dp уже рисует
                // внешний `Box`, и вместе они складывались в двойное белое
                // кольцо — фотография в списке весила заметно больше соседних
                // аватарок-заглушек, хотя это одна и та же строка.
                else -> CoilImage(
                    imageLoader = { getAsyncImageLoader(getPlatformContext()) },
                    modifier = Modifier
                        .clip(shape)
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
 * Что рисовать в кружке вместо фотографии.
 *
 * Отдельный тип, а не два `if` подряд на каждом месте вызова: правило выбора
 * одно на все кружки в приложении, и разъехаться оно не должно — человек с
 * буквой в списке и с подарочной коробкой в профиле выглядит как два разных
 * человека.
 */
internal sealed interface Avatar {
    /** Пресет: либо выбранный человеком, либо запасной по идентификатору. */
    data class Preset(val index: Int) : Avatar

    data class Letter(val letter: String, val seed: String?) : Avatar
}

/**
 * Порядок такой:
 *
 * 1. только что выбранный файл — это фотография, и она важнее всего;
 * 2. пресет, выбранный человеком осознанно, — его выбор не отменяется буквой;
 * 3. настоящая фотография по ссылке;
 * 4. буква имени — если имя известно;
 * 5. запасной пресет по идентификатору — если имени нет, но человек есть;
 * 6. ничего: «?», как и раньше. Сюда попадают картинки желаний, у которых нет
 *    ни имени, ни идентификатора человека.
 */
internal fun resolveAvatar(
    imagePikerState: UiImagePicker,
    avatarSeed: String?,
    avatarName: String?,
): Avatar? {
    if (imagePikerState.imageFile != null) return null

    val url = imagePikerState.imageUrl
    AvatarPresets.indexOf(url)?.let { return Avatar.Preset(it) }
    if (!url.isNullOrEmpty()) return null

    avatarInitial(avatarName)?.let { return Avatar.Letter(it, avatarSeed ?: avatarName) }

    return avatarSeed?.let { Avatar.Preset(AvatarPresets.fallbackIndex(it)) }
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

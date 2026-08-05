package com.ato.sonic_ui.base.image

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
 * @param avatarSeed см. [DisplayImage] — включает встроенные пресеты вместо
 *   заглушки «+».
 * @param showClear рисовать ли крестик. У аватарки удаление уехало в лист
 *   действий, а два способа сделать одно и то же на одном экране только
 *   путают; у картинки желания крестик остаётся единственным.
 */
@Composable
fun DisplayImageWithCross(
    imagePikerState: UiImagePicker,
    onImageClicked: () -> Unit = {},
    onClearClicked: () -> Unit = {},
    size: Float = 256f,
    sizeFactor: Float = 0.5f,
    shape: Shape = CircleShape,
    modifier: Modifier = Modifier,
    clearContentDescription: String? = null,
    contentDescription: String? = null,
    avatarSeed: String? = null,
    showClear: Boolean = true,
    noImageHolder: @Composable BoxScope.() -> Unit = {
        Text(
            text = "+",
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            textAlign = TextAlign.Center,
            fontSize = (size * sizeFactor).sp,
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
) {
    val data = imagePikerState.imageFile ?: imagePikerState.imageUrl
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

                data == null -> noImageHolder()

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

        if (showClear && data != null) {
            // Нажималось ровно по значку — 24dp, вдвое меньше минимума в 48dp,
            // и это единственный способ убрать картинку. Сам значок остался
            // прежнего размера, выросла только область.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onClearClicked
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.05f),
                            shape = shape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Default.Close,
                        contentDescription = clearContentDescription,
                        tint = LocalContentColor.current.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

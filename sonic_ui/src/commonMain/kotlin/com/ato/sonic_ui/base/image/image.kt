package com.ato.sonic_ui.base.image

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ato.helpers.getAsyncImageLoader
import com.ato.helpers.getPlatformContext
import com.ato.ui_state.base.image.UiImagePicker
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage


@Composable
fun DisplayImage(
    imagePikerState: UiImagePicker,
    onImageClicked: () -> Unit = {},
    size: Float = 256f,
    sizeFactor: Float = 0.5f,
    shape: Shape = CircleShape,
    modifier: Modifier = Modifier,
) {
    val data = imagePikerState.imageFile ?: imagePikerState.imageUrl
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
        ) {
            if (data == null) {
                Text(
                    text = "?",
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    textAlign = TextAlign.Center,
                    fontSize = (size * sizeFactor).sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            } else {
                CoilImage(
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
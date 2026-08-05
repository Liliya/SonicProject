package com.ato.sonic_ui.base.image

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ato.helpers.CropGeometry
import com.ato.helpers.CropOffset
import com.ato.helpers.NormalizedCropRect
import com.ato.helpers.getAsyncImageLoader
import com.ato.helpers.getPlatformContext
import com.ato.helpers.imagePixelSize
import com.ato.sonic_ui.base.button.DisplayButton
import com.ato.sonic_ui.base.text.DisplayText
import com.ato.ui_state.base.button.UiButton
import com.ato.ui_state.base.text.UiSimpleText
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import kotlin.math.roundToInt

/**
 * Круглое кадрирование: подвинуть и приблизить картинку до того, как она
 * станет аватаркой.
 *
 * До этого выбранное фото уходило в загрузку как есть, а по кругу его резал
 * `ContentScale.Crop` — по центру и без спроса. Для фотографии, где человек
 * стоит сбоку, это значило «лица не будет», и сделать с этим было нечего.
 *
 * Арифметика живёт в [CropGeometry] и покрыта тестами; здесь только жест и то,
 * что видно.
 *
 * @param onConfirm отдаёт выбранный кадр долями от размеров картинки — резать
 *   байты будет вызывающий, у него для этого есть корутина.
 */
@Composable
fun CircularImageCropper(
    image: ByteArray,
    confirm: UiButton,
    cancel: UiButton,
    onConfirm: (NormalizedCropRect) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    hint: UiSimpleText? = null,
) {
    val size = remember(image) { imagePixelSize(image) }
    val density = LocalDensity.current

    var viewportPx by remember { mutableStateOf(0f) }
    var zoom by remember(image) { mutableStateOf(CropGeometry.MIN_ZOOM) }
    var offset by remember(image) { mutableStateOf(CropOffset(0f, 0f)) }

    // Ставим картинку по центру, как только стал известен размер круга.
    // В самой композиции это писать нельзя — состояние, записанное во время
    // отрисовки, уводит Compose в бесконечную перекомпоновку.
    LaunchedEffect(image, size, viewportPx) {
        if (size != null && viewportPx > 0f) {
            zoom = CropGeometry.MIN_ZOOM
            offset = CropGeometry.centeredOffset(
                imageWidth = size.width,
                imageHeight = size.height,
                viewportSide = viewportPx,
                zoom = CropGeometry.MIN_ZOOM,
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.92f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        hint?.let {
            DisplayText(
                state = it,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
        }

        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .onSizeChanged { viewportPx = it.width.toFloat() }
                    .clip(CircleShape)
                    .background(Color.Black)
            ) {
                if (size == null) {
                    // Формат, который платформа не смогла раскодировать. Кадрировать
                    // нечего, но и терять выбранный файл незачем — уйдёт целиком.
                    CoilImage(
                        imageLoader = { getAsyncImageLoader(getPlatformContext()) },
                        imageModel = { image },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                        ),
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    val displayWidth =
                        CropGeometry.displayWidth(size.width, size.height, viewportPx, zoom)
                    val displayHeight =
                        CropGeometry.displayHeight(size.width, size.height, viewportPx, zoom)

                    CoilImage(
                        imageLoader = { getAsyncImageLoader(getPlatformContext()) },
                        imageModel = { image },
                        // Размер уже посчитан из пропорций картинки, так что
                        // подгонять её ещё и здесь не надо — отсюда FillBounds.
                        imageOptions = ImageOptions(contentScale = ContentScale.FillBounds),
                        modifier = Modifier
                            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                            .size(
                                width = with(density) { displayWidth.toDp() },
                                height = with(density) { displayHeight.toDp() },
                            ),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(image, viewportPx) {
                                detectTransformGestures { _, pan, zoomChange, _ ->
                                    val next = CropGeometry.clampZoom(zoom * zoomChange)
                                    // Приближаем к середине круга, а не к его левому
                                    // верхнему углу: иначе картинка убегает из-под
                                    // пальцев на каждом щипке.
                                    val ratio = next / zoom
                                    val center = viewportPx / 2f

                                    offset = CropGeometry.clampOffset(
                                        imageWidth = size.width,
                                        imageHeight = size.height,
                                        viewportSide = viewportPx,
                                        zoom = next,
                                        offset = CropOffset(
                                            x = center - (center - offset.x) * ratio + pan.x,
                                            y = center - (center - offset.y) * ratio + pan.y,
                                        ),
                                    )
                                    zoom = next
                                }
                            }
                    )
                }
            }

            // Обводка отдельным слоем поверх: на самом круге её закрыла бы
            // картинка, которая лежит внутри него.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.6f),
                        shape = CircleShape,
                    )
            )
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DisplayButton(
                state = cancel,
                onClick = onCancel,
                colors = ButtonDefaults.filledTonalButtonColors(),
                modifier = Modifier.weight(1f),
            )
            DisplayButton(
                state = confirm,
                onClick = {
                    onConfirm(
                        if (size == null || viewportPx <= 0f) {
                            NormalizedCropRect.WHOLE
                        } else {
                            CropGeometry.cropRect(
                                imageWidth = size.width,
                                imageHeight = size.height,
                                viewportSide = viewportPx,
                                zoom = zoom,
                                offset = offset,
                            )
                        }
                    )
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

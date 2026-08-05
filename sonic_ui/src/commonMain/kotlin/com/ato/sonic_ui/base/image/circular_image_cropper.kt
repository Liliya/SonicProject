package com.ato.sonic_ui.base.image

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
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
 * Две вещи в раскладке сделаны не самым коротким способом, и обе — потому что
 * короткий давал чёрный круг вместо фотографии:
 *
 *  * сторона круга берётся из [BoxWithConstraints], то есть известна прямо во
 *    время композиции. Раньше она приезжала из `onSizeChanged` — уже после
 *    измерения, — и первый кадр считался от нуля;
 *  * фотографии задан [requiredSize], а не `size`. Вся модель кадрирования
 *    стоит на том, что картинка **больше** круга и её видимую часть выбирает
 *    сдвиг. `size` подрезается ограничениями родителя до размера этого самого
 *    круга, после чего отрицательный сдвиг уносит картинку за границы и в
 *    кадре остаётся подложка. `requiredSize` ограничения игнорирует.
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            // Не тема: подложка должна быть тёмной в обеих темах, потому что
            // поверх неё лежит чужая фотография, а не наш интерфейс.
            .background(Color.Black.copy(alpha = 0.92f))
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Доля высоты, а не вся: под кругом ещё подсказка и две кнопки, и на
        // невысоком экране они должны остаться на нём.
        val viewportDp = minOf(maxWidth, maxHeight * 0.6f, 320.dp)
        val viewportPx = with(density) { viewportDp.toPx() }

        var zoom by remember(image) { mutableStateOf(CropGeometry.MIN_ZOOM) }
        var offset by remember(image, viewportPx) {
            mutableStateOf(
                size?.let {
                    CropGeometry.centeredOffset(
                        imageWidth = it.width,
                        imageHeight = it.height,
                        viewportSide = viewportPx,
                        zoom = CropGeometry.MIN_ZOOM,
                    )
                } ?: CropOffset(0f, 0f)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            hint?.let {
                DisplayText(
                    state = it,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
            }

            Box(
                modifier = Modifier
                    .size(viewportDp)
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
                    CoilImage(
                        imageLoader = { getAsyncImageLoader(getPlatformContext()) },
                        imageModel = { image },
                        // Размер уже посчитан из пропорций картинки, так что
                        // подгонять её ещё и здесь не надо — отсюда FillBounds.
                        imageOptions = ImageOptions(contentScale = ContentScale.FillBounds),
                        modifier = Modifier
                            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                            .requiredSize(
                                width = with(density) {
                                    CropGeometry.displayWidth(
                                        size.width, size.height, viewportPx, zoom
                                    ).toDp()
                                },
                                height = with(density) {
                                    CropGeometry.displayHeight(
                                        size.width, size.height, viewportPx, zoom
                                    ).toDp()
                                },
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

                // Обводка последней и поверх картинки. Обычный Box без
                // обработчика жестов события не перехватывает, так что тащить
                // фото она не мешает.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                            if (size == null) {
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
}

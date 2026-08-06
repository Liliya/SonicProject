package com.ato.sonic_ui.base.image

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ato.helpers.CropGeometry
import com.ato.helpers.CropOffset
import com.ato.helpers.NormalizedCropRect
import com.ato.helpers.decodeImage
import com.ato.helpers.getAsyncImageLoader
import com.ato.helpers.getPlatformContext
import com.ato.helpers.toPixelCrop
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
 * стоит сбоку, это значило «лица не будет».
 *
 * **Кружок рисует картинку сам, через [Canvas], и берёт для этого ровно тот
 * прямоугольник, который уйдёт в обрезку.** Это не педантизм, это следствие
 * двух заходов, в которых кадр расходился с результатом. Сначала кадр задавался
 * размером и сдвигом composable-а с картинкой, и раскладка жила по своим
 * правилам: `Modifier.size` подрезался ограничениями родителя, потом сторона
 * круга приезжала на кадр позже, чем считались размеры, потом сдвиг перестал
 * доезжать до отрисовки вовсе — на телефоне фотография просто не двигалась, а
 * обрезка накопленный сдвиг честно применяла. Каждый раз «должно быть
 * эквивалентно» оказывалось неэквивалентно.
 *
 * Теперь эквивалентности не требуется: и превью, и обрезка получают один и тот
 * же [NormalizedCropRect] и переводят его в пиксели одной и той же функцией
 * ([toPixelCrop]). Разойтись им больше нечем.
 *
 * @param shape форма рамки. Только рамки: кадр всегда квадратный, режется он
 *   одинаково, и форма здесь говорит человеку, как результат будет показан —
 *   кружком у аватарки, скруглённым квадратом у доски.
 * @param onConfirm отдаёт выбранный кадр долями от размеров картинки — резать
 *   байты будет вызывающий, у него для этого есть корутина. `null` означает
 *   «раскодировать не удалось, бери файл как есть»: кружок в этом случае
 *   показывал середину силами `ContentScale.Crop`, и любой посчитанный кадр
 *   разошёлся бы с тем, что человек видел.
 */
@Composable
fun CircularImageCropper(
    image: ByteArray,
    confirm: UiButton,
    cancel: UiButton,
    onConfirm: (NormalizedCropRect?) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    hint: UiSimpleText? = null,
    shape: Shape = CircleShape,
) {
    val bitmap = remember(image) { decodeImage(image) }
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
        // Сторона круга известна прямо здесь, во время композиции. Когда она
        // приезжала из onSizeChanged, то есть после измерения, первый кадр
        // считался от нуля.
        val viewportDp = minOf(maxWidth, maxHeight * 0.6f, 320.dp)
        val viewportPx = with(density) { viewportDp.toPx() }

        var zoom by remember(image) { mutableStateOf(CropGeometry.MIN_ZOOM) }
        var offset by remember(image, viewportPx) {
            mutableStateOf(
                bitmap?.let {
                    CropGeometry.centeredOffset(
                        imageWidth = it.width,
                        imageHeight = it.height,
                        viewportSide = viewportPx,
                        zoom = CropGeometry.MIN_ZOOM,
                    )
                } ?: CropOffset(0f, 0f)
            )
        }

        // Один прямоугольник на превью и на обрезку.
        val rect = bitmap?.let {
            CropGeometry.cropRect(
                imageWidth = it.width,
                imageHeight = it.height,
                viewportSide = viewportPx,
                zoom = zoom,
                offset = offset,
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
                    .clip(shape)
                    .background(Color.Black)
            ) {
                if (bitmap == null || rect == null) {
                    // Формат, который платформа не смогла раскодировать.
                    // Кадрировать нечего, но и терять выбранный файл незачем —
                    // уйдёт целиком.
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
                    val crop = rect.toPixelCrop(width = bitmap.width, height = bitmap.height)

                    Canvas(
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
                                        imageWidth = bitmap.width,
                                        imageHeight = bitmap.height,
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
                    ) {
                        drawImage(
                            image = bitmap,
                            srcOffset = IntOffset(crop.left, crop.top),
                            srcSize = IntSize(crop.side, crop.side),
                            dstOffset = IntOffset.Zero,
                            dstSize = IntSize(
                                size.width.roundToInt(),
                                size.height.roundToInt(),
                            ),
                            filterQuality = FilterQuality.Medium,
                        )
                    }
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
                            shape = shape,
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
                    onClick = { onConfirm(rect) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

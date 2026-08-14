package com.ato.sonic_ui.base.image

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import kotlin.math.min

/**
 * Что стоит на месте картинки у желания, которому её не задали.
 *
 * У людей и у досок картинка есть всегда — `AvatarPresets` и `BoardPresets`
 * закрыли это тем, что пустое значение перестало означать «нечего рисовать».
 * У желаний оставалось по-старому: `DisplayWish` рисовал картинку только под
 * `imageUrl != null`, поэтому в списке одна строка была с плиткой, другая без,
 * и высоты не совпадали. Ряд ехал по вертикали, и список читался как
 * недоделанный.
 *
 * Отличие от пресетов намеренное, и оно одно: пресет — это **картинка**, у
 * каждого своя и на цветной подложке; заглушка — это **её отсутствие**, и
 * выглядеть она должна тише всего вокруг. Отсюда приглушённая подложка темы
 * вместо градиента и один и тот же подарок для всех желаний: у желания нет
 * своего лица, которое стоило бы угадывать по идентификатору, а двенадцать
 * разных предметов на месте ненайденной картинки читались бы как содержимое.
 *
 * Подарок взят из `board_preset_art.kt` тот же самый — иначе на одном экране
 * оказались бы два разных подарка от одной руки.
 */
@Composable
fun WishImagePlaceholder(
    size: Float,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
) {
    // Смешение, а не прозрачность: прорезь ленты внутри подарка рисуется
    // цветом подложки, и полупрозрачная подложка не стёрла бы под собой
    // корпус коробки, а просветила бы его насквозь.
    //
    // Бледно-зелёный вместо серого: серая плитка на месте картинки читается
    // как «не загрузилось», а тон бренда — как своё место в этом приложении.
    // Подарок приглушён до трети: заглушка не должна спорить с настоящими
    // фотографиями соседей по списку.
    val background = lerp(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.primaryContainer,
        0.55f,
    )
    val glyph = lerp(background, MaterialTheme.colorScheme.primary, 0.42f)

    Box(
        modifier = modifier
            .size(size.dp)
            .background(color = background, shape = shape),
    ) {
        Canvas(modifier = Modifier.size(size.dp)) {
            // Подарок нарисован в квадрате со стороной `side`; половина размера
            // плитки — чтобы вокруг него осталось поле и он не упирался в углы.
            val side = min(this.size.width, this.size.height) * 0.52f
            val offset = Offset(
                x = (this.size.width - side) / 2f,
                y = (this.size.height - side) / 2f,
            )

            translate(left = offset.x, top = offset.y) {
                // `shade` — цвет подложки: прорезь ленты проступает фоном, как
                // и у пресетов доски.
                drawBoardGift(side = side, color = glyph, shade = background)
            }
        }
    }
}

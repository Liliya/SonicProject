package com.ato.sonic_ui.base.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.resources.Res
import com.ato.sonic_ui.resources.wish_gift
import org.jetbrains.compose.resources.painterResource

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
 * Сам подарок — ресурс `wish_gift`, а не рисунок в коде. Раньше его чертил
 * `Canvas` тем же `drawBoardGift`, что и обложки досок: заливка, вырез ленты,
 * арифметика отступов — всё в Kotlin. Правка формы означала правку кода, а
 * увидеть, что получилось, можно было только собрав приложение. Картинку
 * правят как картинку.
 */
@Composable
fun WishImagePlaceholder(
    size: Float,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
) {
    // Смешение, а не прозрачность: подложка должна быть плотной, иначе сквозь
    // неё просвечивает то, на чём лежит плитка, — а лежит она и на белой
    // карточке, и на молочном фоне списка.
    //
    // Бледно-зелёный вместо серого: серая плитка на месте картинки читается
    // как «не загрузилось», а тон бренда — как своё место в этом приложении.
    //
    // Плитка тише, чем была: в строке главное — название желания, а слева от
    // него стоял насыщенный зелёный квадрат, который взгляд ловил первым.
    // Подложка разбавлена сильнее, подарок на ней — чуть светлее; контур при
    // этом остаётся виден, ниже 0.5 он расплывается в пятно.
    val background = lerp(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.primaryContainer,
        0.42f,
    )
    val glyph = lerp(background, MaterialTheme.colorScheme.primary, 0.52f)

    Box(
        modifier = modifier
            .size(size.dp)
            .background(color = background, shape = shape),
        contentAlignment = Alignment.Center,
    ) {
        // Половина плитки, а не вся: подарку нужно поле вокруг, иначе он
        // упирается в углы и плитка перестаёт читаться как подложка. Рисунок
        // занимает не весь свой квадрат (в макете 24×24 он лежит от 2 до 22),
        // поэтому на глаз подарок выходит примерно в 45% плитки — столько же,
        // сколько в макете.
        Image(
            painter = painterResource(Res.drawable.wish_gift),
            contentDescription = null,
            colorFilter = ColorFilter.tint(glyph),
            modifier = Modifier.size((size * 0.5f).dp),
        )
    }
}

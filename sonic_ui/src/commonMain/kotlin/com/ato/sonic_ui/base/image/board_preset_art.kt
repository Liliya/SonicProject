package com.ato.sonic_ui.base.image

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.luminance
import com.ato.ui_state.base.image.BoardPresets

/**
 * Как выглядят двенадцать встроенных картинок досок из [BoardPresets].
 *
 * Те же правила, что у аватарок, и намеренно: доски и профили должны выглядеть
 * сделанными одной рукой.
 *
 * - рисуются на [Canvas], потому что одна и та же картинка нужна и на 48dp в
 *   списке досок, и на 96dp в шапке, а растр под каждый размер — это файлы в
 *   каждой плотности на каждую из двенадцати штук;
 *   [androidx.compose.ui.graphics.vector.ImageVector] тоже не подходит: в этом
 *   проекте это одноцветные значки (`MyIconPack`), под цветную подложку он не
 *   заточен;
 * - подложка бледная, предмет — насыщенный того же оттенка. Так обложка
 *   остаётся фоном для предмета, а не пятном рядом с ним.
 *
 * Сначала подложки были глубокие, а предмет — один и тот же тёплый белый: тогда
 * контраст не зависел от того, какая картинка досталась. Но в списке двенадцать
 * насыщенных квадратов забивали и названия досок рядом, и настоящие фотографии
 * тех досок, которым владелец картинку выбрал, — а фотография всегда важнее
 * заглушки. Ровно этот же разговор был у аватарок, и там он кончился шестью
 * пастельными тонами в `monogram.kt`; здесь те же шесть плюс ещё шесть в том же
 * регистре, потому что предметов шесть и каждому нужны две разные подложки.
 *
 * И тема теперь важна. Глубокая подложка была одинаковой в светлой и тёмной —
 * как настоящая фотография, которая не подкрашивается. Бледная так не может:
 * шесть светлых квадратов на тёмном экране светились бы фонариками. Отсюда два
 * набора, вывернутых друг относительно друга, — тоже как у монограмм.
 */

private class BoardPresetArt(
    val background: Color,
    /** Цвет предмета: тот же оттенок, что подложка, но насыщенный. */
    val ink: Color,
    val glyph: DrawScope.(Float, Color, Color) -> Unit,
)

/**
 * Шесть предметов на двенадцать палитр. Шесть, а не двенадцать: один и тот же
 * предмет на разных подложках различается с одного взгляда, а двенадцать разных
 * силуэтов в одном списке читаются как свалка.
 *
 * Порядок не случаен: предметы идут по кругу, поэтому пары с одним силуэтом —
 * это 0 и 6, 1 и 7 и так далее. Их оттенки нарочно разведены далеко (терракота
 * и небо, роза и мята), иначе два подарка в одном списке пришлось бы различать
 * по полутону.
 *
 * Первые шесть — те же тона, что у монограмм в `monogram.kt`, остальные шесть
 * добавлены в том же регистре. Измеренный контраст предмета к подложке: от
 * 7.8:1 в светлой теме и от 8.3:1 в тёмной. Для сплошной фигуры хватило бы и
 * 3:1, но предмет здесь тонкий в деталях — прорезь ленты, ручка чашки, — и на
 * трёх единицах они пропадают первыми.
 */
private val LightArt: List<BoardPresetArt> = listOf(
    BoardPresetArt(Color(0xFFEDD7CB), Color(0xFF5F2C16), DrawScope::drawBoardGift),   // терракота
    BoardPresetArt(Color(0xFFEDD4D8), Color(0xFF5F2B37), DrawScope::drawBoardHouse),  // пыльная роза
    BoardPresetArt(Color(0xFFD6E3D7), Color(0xFF2A4531), DrawScope::drawBoardCake),   // шалфей
    BoardPresetArt(Color(0xFFDCD8EC), Color(0xFF362D59), DrawScope::drawBoardBook),   // лаванда
    BoardPresetArt(Color(0xFFDBDBDD), Color(0xFF303035), DrawScope::drawBoardPlane),  // графит
    BoardPresetArt(Color(0xFFEBDFCB), Color(0xFF4C3A1F), DrawScope::drawBoardCup),    // тёплый беж
    BoardPresetArt(Color(0xFFD5E1F0), Color(0xFF26405F), DrawScope::drawBoardGift),   // небо
    BoardPresetArt(Color(0xFFD0E6E1), Color(0xFF204742), DrawScope::drawBoardHouse),  // мята
    BoardPresetArt(Color(0xFFE6D6E8), Color(0xFF4B2D52), DrawScope::drawBoardCake),   // слива
    BoardPresetArt(Color(0xFFE0E4CC), Color(0xFF3C4522), DrawScope::drawBoardBook),   // олива
    BoardPresetArt(Color(0xFFE4D9D1), Color(0xFF4B3A2F), DrawScope::drawBoardPlane),  // какао
    BoardPresetArt(Color(0xFFD6E2E6), Color(0xFF27414A), DrawScope::drawBoardCup),    // лёд
)

/**
 * То же самое для тёмной темы, вывернутое наизнанку: подложка приглушена почти
 * до фона экрана, предмет — светлый.
 */
private val DarkArt: List<BoardPresetArt> = listOf(
    BoardPresetArt(Color(0xFF4A3229), Color(0xFFF7D2BF), DrawScope::drawBoardGift),
    BoardPresetArt(Color(0xFF4A3034), Color(0xFFF7CFD6), DrawScope::drawBoardHouse),
    BoardPresetArt(Color(0xFF2C3B2F), Color(0xFFCBE3CE), DrawScope::drawBoardCake),
    BoardPresetArt(Color(0xFF343048), Color(0xFFD8D1F5), DrawScope::drawBoardBook),
    BoardPresetArt(Color(0xFF35343A), Color(0xFFDBDAE1), DrawScope::drawBoardPlane),
    BoardPresetArt(Color(0xFF443A2B), Color(0xFFEFDFC1), DrawScope::drawBoardCup),
    BoardPresetArt(Color(0xFF2A3949), Color(0xFFCFE0F5), DrawScope::drawBoardGift),
    BoardPresetArt(Color(0xFF26403D), Color(0xFFC7E4DE), DrawScope::drawBoardHouse),
    BoardPresetArt(Color(0xFF3E2F44), Color(0xFFEBD3EF), DrawScope::drawBoardCake),
    BoardPresetArt(Color(0xFF383D2B), Color(0xFFDDE3C4), DrawScope::drawBoardBook),
    BoardPresetArt(Color(0xFF3E332C), Color(0xFFEBDBD0), DrawScope::drawBoardPlane),
    BoardPresetArt(Color(0xFF2B3B40), Color(0xFFCFE1E7), DrawScope::drawBoardCup),
)

/** Набор по теме — ровно как `monogramPalette()` у монограмм. */
@Composable
private fun boardArt(): List<BoardPresetArt> =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) LightArt else DarkArt

/**
 * Встроенная картинка доски номер [index].
 *
 * Номер приходит из [BoardPresets], который сам держит его в диапазоне;
 * остаток здесь — на случай, если кто-то передаст своё число мимо него.
 */
@Composable
fun BoardPresetImage(
    index: Int,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
) {
    val palette = boardArt()
    val art = palette[((index % palette.size) + palette.size) % palette.size]

    Canvas(modifier = modifier.clip(shape)) {
        // Заливка ровная, без градиента. Градиент был при глубоких подложках и
        // достался им от прежних двухстоповых кружков; на бледном тоне он не
        // виден вовсе, а два близких цвета вместо одного — это два места, где
        // палитра может разойтись.
        drawRect(color = art.background)

        val side = size.minDimension * 0.46f
        translate(left = (size.width - side) / 2f, top = (size.height - side) / 2f) {
            // Вырезы внутри предмета идут цветом подложки: своим цветом они
            // слились бы с ним самим.
            art.glyph(this, side, art.ink, art.background)
        }
    }
}

// Ниже — предметы. Каждый рисует себя в квадрате [0, side] x [0, side]; сдвиг в
// центр картинки уже сделан вызывающим. [shade] — тот же цвет, что и низ
// подложки: им идут прорези, которые иначе слились бы с самим предметом.

// `internal`, а не `private`: этим же подарком рисуется заглушка желания без
// картинки (`wish_placeholder.kt`). Рисовать её вторым, своим подарком значило
// бы, что рядом на экране два разных подарка от одной руки.
internal fun DrawScope.drawBoardGift(side: Float, color: Color, shade: Color) {
    val corner = CornerRadius(side * 0.05f, side * 0.05f)

    drawRoundRect(
        color = color,
        topLeft = Offset(side * 0.10f, side * 0.38f),
        size = Size(side * 0.80f, side * 0.56f),
        cornerRadius = corner,
    )
    // крышка чуть шире корпуса, как у настоящей коробки
    drawRoundRect(
        color = color,
        topLeft = Offset(side * 0.02f, side * 0.24f),
        size = Size(side * 0.96f, side * 0.16f),
        cornerRadius = corner,
    )
    drawOval(
        color = color,
        topLeft = Offset(side * 0.20f, side * 0.06f),
        size = Size(side * 0.28f, side * 0.20f),
    )
    drawOval(
        color = color,
        topLeft = Offset(side * 0.52f, side * 0.06f),
        size = Size(side * 0.28f, side * 0.20f),
    )
    // лента — прорезь, поэтому цветом подложки
    drawRect(
        color = shade,
        topLeft = Offset(side * 0.44f, side * 0.24f),
        size = Size(side * 0.12f, side * 0.70f),
    )
}

private fun DrawScope.drawBoardHouse(side: Float, color: Color, shade: Color) {
    // крыша
    val roof = Path().apply {
        moveTo(side * 0.50f, side * 0.08f)
        lineTo(side * 0.96f, side * 0.46f)
        lineTo(side * 0.04f, side * 0.46f)
        close()
    }
    drawPath(path = roof, color = color)

    // стены
    drawRoundRect(
        color = color,
        topLeft = Offset(side * 0.16f, side * 0.46f),
        size = Size(side * 0.68f, side * 0.46f),
        cornerRadius = CornerRadius(side * 0.05f, side * 0.05f),
    )
    // дверь — прорезь
    drawRoundRect(
        color = shade,
        topLeft = Offset(side * 0.42f, side * 0.62f),
        size = Size(side * 0.16f, side * 0.30f),
        cornerRadius = CornerRadius(side * 0.04f, side * 0.04f),
    )
}

private fun DrawScope.drawBoardCake(side: Float, color: Color, shade: Color) {
    // свеча
    drawRect(
        color = color,
        topLeft = Offset(side * 0.47f, side * 0.06f),
        size = Size(side * 0.06f, side * 0.18f),
    )
    // огонёк
    drawOval(
        color = color,
        topLeft = Offset(side * 0.43f, side * 0.00f),
        size = Size(side * 0.14f, side * 0.10f),
    )
    // верхний ярус
    drawRoundRect(
        color = color,
        topLeft = Offset(side * 0.20f, side * 0.28f),
        size = Size(side * 0.60f, side * 0.26f),
        cornerRadius = CornerRadius(side * 0.06f, side * 0.06f),
    )
    // нижний ярус
    drawRoundRect(
        color = color,
        topLeft = Offset(side * 0.08f, side * 0.56f),
        size = Size(side * 0.84f, side * 0.36f),
        cornerRadius = CornerRadius(side * 0.06f, side * 0.06f),
    )
    // прорезь между ярусами
    drawRect(
        color = shade,
        topLeft = Offset(side * 0.20f, side * 0.52f),
        size = Size(side * 0.60f, side * 0.05f),
    )
}

private fun DrawScope.drawBoardBook(side: Float, color: Color, shade: Color) {
    drawRoundRect(
        color = color,
        topLeft = Offset(side * 0.12f, side * 0.10f),
        size = Size(side * 0.76f, side * 0.80f),
        cornerRadius = CornerRadius(side * 0.06f, side * 0.06f),
    )
    // корешок — прорезь вдоль левого края
    drawRect(
        color = shade,
        topLeft = Offset(side * 0.26f, side * 0.10f),
        size = Size(side * 0.06f, side * 0.80f),
    )
    // закладка
    drawRect(
        color = shade,
        topLeft = Offset(side * 0.66f, side * 0.10f),
        size = Size(side * 0.10f, side * 0.34f),
    )
}

private fun DrawScope.drawBoardPlane(side: Float, color: Color, shade: Color) {
    val body = Path().apply {
        moveTo(side * 0.94f, side * 0.30f)
        lineTo(side * 0.58f, side * 0.52f)
        lineTo(side * 0.34f, side * 0.94f)
        lineTo(side * 0.24f, side * 0.86f)
        lineTo(side * 0.34f, side * 0.50f)
        lineTo(side * 0.06f, side * 0.44f)
        lineTo(side * 0.10f, side * 0.32f)
        lineTo(side * 0.44f, side * 0.34f)
        close()
    }
    drawPath(path = body, color = color)
}

private fun DrawScope.drawBoardCup(side: Float, color: Color, shade: Color) {
    // чашка
    drawRoundRect(
        color = color,
        topLeft = Offset(side * 0.14f, side * 0.30f),
        size = Size(side * 0.56f, side * 0.52f),
        cornerRadius = CornerRadius(side * 0.10f, side * 0.10f),
    )
    // ручка
    drawRoundRect(
        color = color,
        topLeft = Offset(side * 0.68f, side * 0.40f),
        size = Size(side * 0.22f, side * 0.24f),
        cornerRadius = CornerRadius(side * 0.11f, side * 0.11f),
    )
    drawRoundRect(
        color = shade,
        topLeft = Offset(side * 0.74f, side * 0.46f),
        size = Size(side * 0.10f, side * 0.12f),
        cornerRadius = CornerRadius(side * 0.05f, side * 0.05f),
    )
    // блюдце
    drawRoundRect(
        color = color,
        topLeft = Offset(side * 0.06f, side * 0.86f),
        size = Size(side * 0.72f, side * 0.08f),
        cornerRadius = CornerRadius(side * 0.04f, side * 0.04f),
    )
}

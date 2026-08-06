package com.ato.sonic_ui.base.image

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import com.ato.ui_state.base.image.BoardPresets

/**
 * Как выглядят двенадцать встроенных картинок досок из [BoardPresets].
 *
 * Те же правила, что у аватарок в `avatar_preset_art.kt`, и намеренно: доски и
 * профили должны выглядеть сделанными одной рукой.
 *
 * - рисуются на [Canvas], потому что одна и та же картинка нужна и на 40dp в
 *   списке досок, и на 56dp в шапке, а растр под каждый размер — это файлы в
 *   каждой плотности на каждую из двенадцати штук;
 *   [androidx.compose.ui.graphics.vector.ImageVector] тоже не подходит: в этом
 *   проекте это одноцветные значки (`MyIconPack`), под градиентную подложку он
 *   не заточен;
 * - подложка глубокая у всех двенадцати, а предмет всегда одного цвета
 *   ([GLYPH_COLOR]): контраст не зависит от того, какая картинка досталась, и
 *   не разъезжается между светлой и тёмной темой.
 *
 * Разница с аватарками одна: там пять силуэтов про людей и праздник, здесь
 * шесть предметов, по которым доску узнают в списке — подарок, дом, торт,
 * книга, самолёт, чашка.
 */

/** Тёплый белый вместо чистого: на цветной подложке он не режет глаз. */
private val GLYPH_COLOR = Color(0xFFFFFDF8)

private class BoardPresetArt(
    val top: Color,
    val bottom: Color,
    val glyph: DrawScope.(Float, Color, Color) -> Unit,
)

/**
 * Шесть предметов на двенадцать палитр. Шесть, а не двенадцать: один и тот же
 * предмет на разных подложках различается с одного взгляда, а двенадцать разных
 * силуэтов в одном списке читаются как свалка.
 */
private val BOARD_PRESET_ART: List<BoardPresetArt> = listOf(
    BoardPresetArt(Color(0xFF2E7D5B), Color(0xFF1B5E3F), DrawScope::drawBoardGift),
    BoardPresetArt(Color(0xFFE07A5F), Color(0xFFC75B41), DrawScope::drawBoardHouse),
    BoardPresetArt(Color(0xFF5B7DB1), Color(0xFF3D5A8A), DrawScope::drawBoardCake),
    BoardPresetArt(Color(0xFFB5838D), Color(0xFF8E5A6B), DrawScope::drawBoardBook),
    BoardPresetArt(Color(0xFFD9A441), Color(0xFFB87C2A), DrawScope::drawBoardPlane),
    BoardPresetArt(Color(0xFF6D9773), Color(0xFF4A7856), DrawScope::drawBoardCup),
    BoardPresetArt(Color(0xFF8E7CC3), Color(0xFF6A55A0), DrawScope::drawBoardGift),
    BoardPresetArt(Color(0xFF4FA3A5), Color(0xFF2F7C80), DrawScope::drawBoardHouse),
    BoardPresetArt(Color(0xFFC96A8B), Color(0xFFA24A6C), DrawScope::drawBoardCake),
    BoardPresetArt(Color(0xFF7A8B99), Color(0xFF566873), DrawScope::drawBoardBook),
    BoardPresetArt(Color(0xFF9C6644), Color(0xFF7A4A2E), DrawScope::drawBoardPlane),
    BoardPresetArt(Color(0xFF4A6FA5), Color(0xFF2F4C77), DrawScope::drawBoardCup),
)

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
    val art = BOARD_PRESET_ART[((index % BOARD_PRESET_ART.size) + BOARD_PRESET_ART.size) % BOARD_PRESET_ART.size]

    Canvas(modifier = modifier.clip(shape)) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(art.top, art.bottom),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            )
        )

        val side = size.minDimension * 0.46f
        translate(left = (size.width - side) / 2f, top = (size.height - side) / 2f) {
            art.glyph(this, side, GLYPH_COLOR, art.bottom)
        }
    }
}

// Ниже — предметы. Каждый рисует себя в квадрате [0, side] x [0, side]; сдвиг в
// центр картинки уже сделан вызывающим. [shade] — тот же цвет, что и низ
// подложки: им идут прорези, которые иначе слились бы с самим предметом.

private fun DrawScope.drawBoardGift(side: Float, color: Color, shade: Color) {
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

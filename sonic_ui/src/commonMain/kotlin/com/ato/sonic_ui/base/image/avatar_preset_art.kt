package com.ato.sonic_ui.base.image

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import com.ato.ui_state.base.image.AvatarPresets
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Как выглядят десять встроенных аватарок из [AvatarPresets].
 *
 * Рисуются на [Canvas], а не лежат картинками: аватарка нужна и на 32dp в
 * списке, и на 256dp в профиле, а растр под оба размера — это два файла в
 * каждой плотности на каждую из десяти штук. Векторных ресурсов тоже нет, и не
 * случайно: подложка здесь градиентная, а `ImageVector` в этом проекте — это
 * значки в одном цвете (`MyIconPack`), под заливку он не заточен.
 *
 * Палитры глубокие все десять, потому что глиф всегда одного цвета
 * ([GLYPH_COLOR]): так контраст не зависит от того, какой пресет достался, и
 * не разъезжается между светлой и тёмной темой — аватарка не подкрашивается
 * темой вообще, она одна и та же везде, как и настоящая фотография.
 */

/** Тёплый белый вместо чистого: на цветной подложке он не режет глаз. */
private val GLYPH_COLOR = Color(0xFFFFFDF8)

private class AvatarPresetArt(
    val top: Color,
    val bottom: Color,
    val glyph: (DrawScope, Float, Color, Color) -> Unit,
)

/**
 * Пять рисунков на десять палитр. Пять, а не десять: два одинаковых глифа на
 * разных подложках различаются с одного взгляда, а десять разных силуэтов в
 * одном ряду читаются как свалка.
 */
private val AVATAR_PRESET_ART: List<AvatarPresetArt> = listOf(
    AvatarPresetArt(Color(0xFF2E7D5B), Color(0xFF1B5E3F), DrawScope::drawGift),
    AvatarPresetArt(Color(0xFFE07A5F), Color(0xFFC75B41), DrawScope::drawStar),
    AvatarPresetArt(Color(0xFF5B7DB1), Color(0xFF3D5A8A), DrawScope::drawHeart),
    AvatarPresetArt(Color(0xFFB5838D), Color(0xFF8E5A6B), DrawScope::drawBalloon),
    AvatarPresetArt(Color(0xFFD9A441), Color(0xFFB87C2A), DrawScope::drawSparkle),
    AvatarPresetArt(Color(0xFF6D9773), Color(0xFF4A7856), DrawScope::drawGift),
    AvatarPresetArt(Color(0xFF8E7CC3), Color(0xFF6A55A0), DrawScope::drawStar),
    AvatarPresetArt(Color(0xFF4FA3A5), Color(0xFF2F7C80), DrawScope::drawHeart),
    AvatarPresetArt(Color(0xFFC96A8B), Color(0xFFA24A6C), DrawScope::drawBalloon),
    AvatarPresetArt(Color(0xFF7A8B99), Color(0xFF566873), DrawScope::drawSparkle),
)

/**
 * Встроенная аватарка номер [index].
 *
 * Номер приходит из [AvatarPresets], который сам держит его в диапазоне;
 * остаток здесь — на случай, если кто-то передаст своё число мимо него.
 */
@Composable
fun AvatarPresetImage(
    index: Int,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
) {
    val art = AVATAR_PRESET_ART[((index % AVATAR_PRESET_ART.size) + AVATAR_PRESET_ART.size) % AVATAR_PRESET_ART.size]

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

// Ниже — глифы. Каждый рисует себя в квадрате [0, side] x [0, side]; сдвиг в
// центр аватарки уже сделан вызывающим. [shade] — тот же цвет, что и низ
// подложки: им идут прорези, которые иначе слились бы с самим глифом.

private fun DrawScope.drawGift(side: Float, color: Color, shade: Color) {
    val corner = CornerRadius(side * 0.05f, side * 0.05f)

    // корпус
    drawRoundRect(
        color = color,
        topLeft = Offset(side * 0.10f, side * 0.38f),
        size = Size(side * 0.80f, side * 0.56f),
        cornerRadius = corner,
    )
    // крышка — чуть шире корпуса, как у настоящей коробки
    drawRoundRect(
        color = color,
        topLeft = Offset(side * 0.02f, side * 0.24f),
        size = Size(side * 0.96f, side * 0.16f),
        cornerRadius = corner,
    )
    // бант: две петли
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
    // лента — прорезь в глифе, поэтому цветом подложки
    drawRect(
        color = shade,
        topLeft = Offset(side * 0.44f, side * 0.24f),
        size = Size(side * 0.12f, side * 0.70f),
    )
}

private fun DrawScope.drawStar(side: Float, color: Color, shade: Color) {
    val center = Offset(side / 2f, side * 0.52f)
    val outer = side * 0.48f
    val inner = outer * 0.45f
    val path = Path()

    repeat(10) { step ->
        val radius = if (step % 2 == 0) outer else inner
        val angle = -PI / 2 + step * PI / 5
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(path = path, color = color)
}

private fun DrawScope.drawHeart(side: Float, color: Color, shade: Color) {
    val path = Path().apply {
        moveTo(side * 0.50f, side * 0.90f)
        cubicTo(side * 0.02f, side * 0.58f, side * 0.06f, side * 0.18f, side * 0.30f, side * 0.14f)
        cubicTo(side * 0.42f, side * 0.12f, side * 0.48f, side * 0.22f, side * 0.50f, side * 0.30f)
        cubicTo(side * 0.52f, side * 0.22f, side * 0.58f, side * 0.12f, side * 0.70f, side * 0.14f)
        cubicTo(side * 0.94f, side * 0.18f, side * 0.98f, side * 0.58f, side * 0.50f, side * 0.90f)
        close()
    }

    drawPath(path = path, color = color)
}

private fun DrawScope.drawBalloon(side: Float, color: Color, shade: Color) {
    drawOval(
        color = color,
        topLeft = Offset(side * 0.18f, side * 0.04f),
        size = Size(side * 0.64f, side * 0.72f),
    )
    // узелок
    val knot = Path().apply {
        moveTo(side * 0.44f, side * 0.74f)
        lineTo(side * 0.56f, side * 0.74f)
        lineTo(side * 0.50f, side * 0.84f)
        close()
    }
    drawPath(path = knot, color = color)
    // ниточка
    val string = Path().apply {
        moveTo(side * 0.50f, side * 0.84f)
        quadraticTo(side * 0.68f, side * 0.90f, side * 0.54f, side * 1.00f)
    }
    drawPath(
        path = string,
        color = color,
        style = Stroke(width = side * 0.05f),
    )
}

private fun DrawScope.drawSparkle(side: Float, color: Color, shade: Color) {
    // Четырёхлучевая звезда: контрольная точка каждой кривой лежит в центре,
    // от этого стороны вогнутые, а лучи получаются острыми.
    fun sparkle(center: Offset, radius: Float): Path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }

    drawPath(path = sparkle(Offset(side * 0.44f, side * 0.44f), side * 0.44f), color = color)
    drawPath(path = sparkle(Offset(side * 0.82f, side * 0.80f), side * 0.20f), color = color)
}

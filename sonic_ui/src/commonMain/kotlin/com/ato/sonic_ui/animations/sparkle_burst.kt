package com.ato.sonic_ui.animations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.base.helpers.toPx
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Одна искорка. Все значения — доли от базовых размеров, чтобы эффект
// одинаково выглядел на любой плотности экрана.
@Immutable
data class Sparkle(
    val angle: Float,       // отклонение от вертикали, радианы
    val distance: Float,    // доля от riseDistance
    val scale: Float,       // доля от sparkleSize
    val delay: Float,       // задержка старта, доля от общей длительности
    val spin: Float,        // скорость вращения
    val phase: Float,       // фаза покачивания и мерцания
    val colorIndex: Int,
)

/**
 * Мягкий отклик на приятное действие: от точки [origin] расходится волна-кольцо
 * и вверх всплывают редкие искорки в цветах темы.
 *
 * В отличие от [ConfettiEffect] эффект короткий, полупрозрачный и не перекрывает
 * контент — он подсвечивает место нажатия, а не весь экран.
 *
 * @param origin точка старта в пикселях относительно этого компонента; null — центр
 * @param onFinished вызывается один раз по завершении анимации, удобно для сброса флага
 */
@Composable
fun SparkleBurstEffect(
    modifier: Modifier = Modifier,
    origin: Offset? = null,
    totalDuration: Float = 1.2f,
    sparkleCount: Int = 20,
    riseDistance: Dp = 140.dp,
    sparkleSize: Dp = 9.dp,
    pulseRadius: Dp = 96.dp,
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
    ),
    onFinished: () -> Unit = {},
) {
    val rise = riseDistance.toPx()
    val baseSize = sparkleSize.toPx()
    val pulse = pulseRadius.toPx()

    val sparkles = remember(sparkleCount) { generateSparkles(sparkleCount) }
    val progress = remember { Animatable(0f) }
    val finished by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = (totalDuration * 1000).toInt(),
                easing = LinearEasing
            )
        )
        finished()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val time = progress.value
        if (time >= 1f) return@Canvas

        val center = origin ?: Offset(size.width / 2f, size.height / 2f)
        drawPulse(center, time, pulse, colors.first())
        sparkles.forEach { sparkle ->
            drawSparkle(sparkle, center, time, rise, baseSize, colors)
        }
    }
}

// Волна-кольцо от точки нажатия: два кольца со сдвигом плюс короткое мягкое свечение.
private fun DrawScope.drawPulse(
    center: Offset,
    time: Float,
    maxRadius: Float,
    color: Color,
) {
    val bloom = (time / BLOOM_PORTION).coerceIn(0f, 1f)
    if (bloom < 1f) {
        drawCircle(
            color = color.copy(alpha = (1f - bloom) * 0.16f),
            radius = maxRadius * 0.5f * FastOutSlowInEasing.transform(bloom),
            center = center,
        )
    }

    repeat(2) { ring ->
        val local = ((time - ring * RING_GAP) / PULSE_PORTION).coerceIn(0f, 1f)
        if (local <= 0f || local >= 1f) return@repeat
        drawCircle(
            color = color.copy(alpha = (1f - local) * (1f - local) * 0.45f),
            radius = maxRadius * (0.15f + 0.85f * FastOutSlowInEasing.transform(local)),
            center = center,
            style = Stroke(width = maxRadius * 0.05f * (1f - local * 0.6f)),
        )
    }
}

private fun DrawScope.drawSparkle(
    sparkle: Sparkle,
    center: Offset,
    time: Float,
    rise: Float,
    baseSize: Float,
    colors: List<Color>,
) {
    val local = ((time - sparkle.delay) / (1f - sparkle.delay)).coerceIn(0f, 1f)
    if (local <= 0f || local >= 1f) return

    // Быстрый разлёт с мягким торможением + едва заметная гравитация в конце.
    val travel = rise * sparkle.distance * LinearOutSlowInEasing.transform(local)
    val sway = sin(sparkle.phase + local * TWO_PI) * rise * 0.06f
    val position = Offset(
        x = center.x + sin(sparkle.angle) * travel + sway,
        y = center.y - cos(sparkle.angle) * travel + rise * 0.12f * local * local,
    )

    val fade = when {
        local < FADE_IN -> local / FADE_IN
        local > FADE_OUT_START -> 1f - (local - FADE_OUT_START) / (1f - FADE_OUT_START)
        else -> 1f
    }
    val twinkle = 0.7f + 0.3f * sin(sparkle.phase + local * TWO_PI * 3f)
    val alpha = (fade * twinkle).coerceIn(0f, 1f)
    val radius = baseSize * sparkle.scale * (0.55f + 0.45f * sin(local * PI.toFloat()))

    drawPath(
        path = sparklePath(position, radius, sparkle.spin * local * TWO_PI),
        color = colors[sparkle.colorIndex % colors.size].copy(alpha = alpha),
    )
}

// Четырёхлучевая звёздочка: 8 вершин, через одну — внутренний радиус.
private fun sparklePath(center: Offset, radius: Float, rotation: Float): Path {
    val inner = radius * 0.34f
    return Path().apply {
        repeat(8) { index ->
            val pointRadius = if (index % 2 == 0) radius else inner
            val angle = rotation + index * PI.toFloat() / 4f
            val x = center.x + sin(angle) * pointRadius
            val y = center.y - cos(angle) * pointRadius
            if (index == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

private fun generateSparkles(count: Int): List<Sparkle> {
    val random = Random.Default
    return List(count) { index ->
        Sparkle(
            angle = (random.nextFloat() - 0.5f) * SPREAD,
            distance = 0.55f + random.nextFloat() * 0.45f,
            scale = 0.6f + random.nextFloat() * 0.7f,
            delay = random.nextFloat() * 0.25f,
            spin = (random.nextFloat() - 0.5f) * 1.4f,
            phase = random.nextFloat() * TWO_PI,
            colorIndex = index,
        )
    }
}

private val TWO_PI = (2 * PI).toFloat()
private const val SPREAD = 2.2f          // разлёт по горизонтали, ~±63°
private const val FADE_IN = 0.18f
private const val FADE_OUT_START = 0.55f
private const val PULSE_PORTION = 0.5f   // кольца успевают за первую половину
private const val RING_GAP = 0.1f
private const val BLOOM_PORTION = 0.35f

@file:OptIn(ExperimentalFoundationApi::class)

package com.ato.sonic_ui.base.button

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class DragAnchors(val fraction: Float) {
    Start(0f),
    End(1f),
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeButton(
    contentSize: Dp,
    colorStart: Color,
    colorEnd: Color,
    orientation: Orientation,
    modifier: Modifier = Modifier,
    commonContent: @Composable BoxScope.() -> Unit,
    draggableContent: @Composable BoxScope.(Modifier) -> Unit,
    onUnlock: () -> Unit = {}
) {
    val density = LocalDensity.current
    val positionalThreshold = { distance: Float -> distance * 0.999f }
    val velocityThreshold = { with(density) { 10000.dp.toPx() } }
    // Compose Foundation 1.7 split the single animationSpec in two: the snap that
    // settles on an anchor, and the decay that carries a fling past one.
    val snapAnimationSpec = tween<Float>()
    val decayAnimationSpec = exponentialDecay<Float>()

    val state = rememberSaveable(
        density,
        saver = AnchoredDraggableState.Saver(
            snapAnimationSpec = snapAnimationSpec,
            decayAnimationSpec = decayAnimationSpec,
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold,
        )
    ) {
        AnchoredDraggableState(
            initialValue = DragAnchors.Start,
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold,
            snapAnimationSpec = snapAnimationSpec,
            decayAnimationSpec = decayAnimationSpec,
        )
    }
    val contentSizePx = with(density) { contentSize.toPx() }
    val backgroundColor by remember(state) {
        derivedStateOf {
            calculateTrackColor(colorStart, colorEnd, state.getTrueProgress())
        }
    }

    LaunchedEffect(state.currentValue) {
        if (state.currentValue == DragAnchors.End) {
            onUnlock()
        }
    }

    Box(
        modifier
            .height(contentSize)
            .background(backgroundColor, RoundedCornerShape(28.dp))
            .onSizeChanged { layoutSize ->
                val dragEndPoint = if (orientation == Orientation.Vertical) {
                    layoutSize.height - contentSizePx
                } else {
                    layoutSize.width - contentSizePx
                }
                state.updateAnchors(
                    DraggableAnchors {
                        DragAnchors
                            .entries
                            .forEach { anchor ->
                                anchor at dragEndPoint * anchor.fraction
                            }
                    }
                )
            }
    ) {
        commonContent()

        draggableContent(
            Modifier
                .size(contentSize)
                .offset { getOffset(orientation, state) }
                .anchoredDraggable(
                    state = state,
                    orientation = orientation,
                    enabled = state.currentValue == DragAnchors.Start
                )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun getOffset(
    orientation: Orientation,
    state: AnchoredDraggableState<DragAnchors>
): IntOffset {
    return if (orientation == Orientation.Vertical) {
        IntOffset(
            x = 0,
            y = state
                .requireOffset()
                .roundToInt(),
        )
    } else {
        IntOffset(
            x = state
                .requireOffset()
                .roundToInt(),
            y = 0,
        )
    }
}

fun calculateTrackColor(
    colorStart: Color,
    colorEnd: Color,
    trueProgress: Float
): Color {
    val endOfColorChangeFraction = 0.999f
    val fraction = (trueProgress / endOfColorChangeFraction).coerceIn(0f..1f)
    return lerp(colorStart, colorEnd, fraction)
}

fun getProgress(
    swipeFraction: Float,
    currentValue: DragAnchors,
    targetValue: DragAnchors
): Float {
    return when {
        currentValue == DragAnchors.Start && targetValue == DragAnchors.Start && swipeFraction == 1f -> 0f
        currentValue == DragAnchors.End && targetValue == DragAnchors.End && (swipeFraction == 0f || swipeFraction == 1f) -> 1f
        currentValue == DragAnchors.Start -> swipeFraction
        else -> 1 - swipeFraction
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun AnchoredDraggableState<DragAnchors>.getTrueProgress(): Float {
    return getProgress(
        this.progress,
        this.currentValue,
        this.targetValue
    )
}

@Composable
fun Preview() {
    SwipeButton(
        contentSize = 56.dp,
        colorStart = Color.Gray,
        colorEnd = Color.Green,
        commonContent = {
            Text(
                text = "Сдвиньте, чтобы разблокировать",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        draggableContent = { modifier ->
            Box(
                modifier = modifier
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        orientation = Orientation.Horizontal,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        onUnlock = {

        }
    )
}
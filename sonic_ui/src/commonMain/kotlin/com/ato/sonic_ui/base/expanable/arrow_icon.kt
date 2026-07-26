package com.ato.sonic_ui.base.expanable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp


@Composable
fun ExpandableCircleWithIcon(
    modifier: Modifier = Modifier,
    expandContentDescription: String? = null,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(bottom = 8.dp)
    ) {
        // Круг с иконкой
        Surface(
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { expanded = !expanded },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
//            shadowElevation = 4.dp // с прозрачностью не красиво
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Анимация поворота иконки
                val rotationAngle by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = tween(durationMillis = 300)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = expandContentDescription,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }
        }

        // Анимированное раскрытие текста
        AnimatedVisibility(visible = expanded) {
            content()
        }
    }
}
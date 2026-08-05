package com.ato.sonic_ui.menu

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Переключатель на несколько разделов внутри экрана.
 *
 * Раньше выбранный пункт красился в `surface` и «выбивался» из дорожки
 * blend-режимом `SrcOut`. В тёмной теме `surface` — почти чёрный, а дорожка была
 * `primaryContainer`, то есть зелёной: выбранный раздел выглядел дырой, а
 * невыбранный — залитой кнопкой, и глаз читал активным не тот пункт.
 *
 * Теперь никаких вычитаний: дорожка нейтральная, выбранный пункт — плашка
 * `secondaryContainer`, как у `SegmentedButton` в Material 3. Работает в обеих
 * темах, потому что обе роли берутся из схемы парой со своим `on…`.
 */
@Composable
fun TextSwitch(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    items: List<String>,
    onSelectionChange: (Int) -> Unit
) {
    if (items.isEmpty()) return

    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val selectedColor = MaterialTheme.colorScheme.secondaryContainer
    val selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer
    val idleTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    val trackPadding = 4.dp
    val tabBarHeight = 48.dp
    val shape = MaterialTheme.shapes.small

    BoxWithConstraints(
        modifier
            .height(tabBarHeight)
            .clip(shape)
            .background(trackColor)
            .padding(trackPadding)
    ) {
        val tabWidth = this.maxWidth / items.size

        val indicatorOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
            label = "indicator offset"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .clip(shape)
                .background(selectedColor)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, text ->
                val isSelected = index == selectedIndex

                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                            onClick = { onSelectionChange(index) }
                        )
                        // Экранный диктор объявляет раздел и то, выбран ли он;
                        // без этого оба пункта звучали одинаково.
                        .clearAndSetSemantics {
                            this.text = AnnotatedString(text)
                            this.selected = isSelected
                            this.role = Role.Tab
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) selectedTextColor else idleTextColor
                    )
                }
            }
        }
    }
}

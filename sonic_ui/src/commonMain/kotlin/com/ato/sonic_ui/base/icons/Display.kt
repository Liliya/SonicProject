package com.ato.sonic_ui.base.icons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ato.ui_state.base.UiIcon
import org.jetbrains.compose.resources.painterResource

@Composable
fun DisplayIcon(
    state: UiIcon?,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    tint: Color? = null
) {
    if (state == null) return

    Box(
        modifier = modifier
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(4.dp).align(Alignment.Center)
            )
            return@Box
        }

        val color = tint ?: if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else MaterialTheme.colorScheme.onPrimary.copy(
            alpha = 0.6f
        )
        val description = state.contentDescription.ifEmpty { null }
        // Заливка только там, где она есть: у половины иконок Material Symbols
        // заполненного варианта не существует, и тогда контурная рисуется в
        // обоих состояниях.
        val resource = state.selectedIconRes?.takeIf { selected } ?: state.iconRes
        val vector = state.icon

        when {
            resource != null -> Icon(
                modifier = Modifier.align(Alignment.Center),
                painter = painterResource(resource),
                contentDescription = description,
                tint = color,
            )

            vector != null -> Icon(
                modifier = Modifier.align(Alignment.Center),
                imageVector = vector,
                // UiIcon всегда носил contentDescription, и он всегда терялся здесь:
                // для скринридера каждая такая иконка была безымянной.
                contentDescription = description,
                tint = color,
            )
        }
    }
}

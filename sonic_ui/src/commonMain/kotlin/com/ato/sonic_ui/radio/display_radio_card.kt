package com.ato.sonic_ui.radio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ato.ui_state.base.radio.UiRadioCard
import org.jetbrains.compose.resources.stringResource

@Composable
fun DisplayUiRadioCard(
    state: UiRadioCard,
    modifier: Modifier = Modifier,
    onClick: (UiRadioCard) -> Unit,
    content: @Composable () -> Unit = {}
) {
    val title = if (state.formatArgs == null) {
        stringResource(state.text)
    } else {
        stringResource(state.text, state.formatArgs!!)
    }

    val containerColor = if (state.isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    OutlinedCard(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.outlinedCardColors(),
        border = CardDefaults.outlinedCardBorder(enabled = state.isSelected),
        modifier = modifier
            .clickable(onClick = { onClick.invoke(state) })
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            RadioButton(
                selected = state.isSelected,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                ),
                onClick = null
            )
            Text(
                text = title,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f),
            )

            state.icon?.let {
                Icon(
                    imageVector = state.icon!!,
                    contentDescription = null
                )
            }
        }

        AnimatedVisibility(
            visible = state.isSelected,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            content.invoke()
        }
    }
}
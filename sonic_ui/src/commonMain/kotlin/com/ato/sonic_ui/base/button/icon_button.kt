package com.ato.sonic_ui.base.button

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ato.ui_state.base.button.UiIconButton

@Composable
fun DisplayUiIconButton(
    state: UiIconButton,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
) {
    if (state.isVisible) {
        Button(
            colors = colors,
            modifier = modifier,
            onClick = onClick,
            enabled = state.isEnabled,
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = state.icon,
                    contentDescription = state.contentDescription,
                )
            }
        }
    } else {
        Text("")
    }
}
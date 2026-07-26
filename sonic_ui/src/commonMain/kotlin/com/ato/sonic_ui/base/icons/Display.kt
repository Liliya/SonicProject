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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ato.ui_state.base.UiIcon

@Composable
fun DisplayIcon(
    state: UiIcon?,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    tint: Color? = null
) =
    with(state) {
        if (this == null) {

        } else {
            Box(
                modifier = modifier
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(4.dp).align(Alignment.Center)
                    )
                } else {
                    Icon(
                        modifier = Modifier.align(Alignment.Center),
                        imageVector = icon as ImageVector,
                        contentDescription = contentDescription.ifEmpty { null },
                        tint = tint ?: if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else MaterialTheme.colorScheme.onPrimary.copy(
                            alpha = 0.6f
                        )
                    )
                }
            }
        }
    }
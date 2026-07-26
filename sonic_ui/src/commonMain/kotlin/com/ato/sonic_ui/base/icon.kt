package com.ato.sonic_ui.base

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ato.ui_state.base.UiIcon
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun UiIcon.Display(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    tint: Color? = null
) {
    Box(
        modifier = modifier
    ) {
        Icon(
            modifier = Modifier.align(Alignment.Center),
            imageVector = icon as ImageVector,
            // UiIcon всегда носил contentDescription, и он всегда терялся здесь:
            // для скринридера каждая такая иконка была безымянной.
            contentDescription = contentDescription.ifEmpty { null },
            tint = tint ?: if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else MaterialTheme.colorScheme.onPrimary.copy(
                alpha = 0.6f
            )
        )
    }
}

// ------------------------------------------------------------------------
// ------------------------------------------------------------------------
// ------------------------------------------------------------------------

@Preview()
@Composable
fun UiIconPreview() {
    UiIconSamples.menu.Display()
}


object UiIconSamples {
    val menu = UiIcon(
        icon = Icons.Filled.Menu,
        contentDescription = "Menu"
    )
    val edit = UiIcon(
        icon = Icons.Filled.Edit,
        contentDescription = "Edit"
    )
    val close = UiIcon(
        icon = Icons.Filled.Close,
        contentDescription = "Close"
    )
    val settings = UiIcon(
        icon = Icons.Filled.Settings,
        contentDescription = "Settings"
    )
    val home = UiIcon(
        icon = Icons.Filled.Home,
        contentDescription = "Home"
    )
    val profile = UiIcon(
        icon = Icons.Filled.AccountCircle,
        contentDescription = "Home"
    )

    val five_items = listOf(
        menu,
        edit,
        close,
        settings,
        home,
    )
}
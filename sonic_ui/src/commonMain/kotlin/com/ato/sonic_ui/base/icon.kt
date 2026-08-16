package com.ato.sonic_ui.base

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ato.sonic_ui.base.icons.DisplayIcon
import com.ato.ui_state.base.UiIcon
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Здесь была вторая, посимвольно такая же отрисовка иконки, что и в
 * [DisplayIcon]. Две копии одного кода разошлись бы на первой же правке —
 * например когда иконка научилась приходить ассетом, а не только вектором.
 */
@Composable
fun UiIcon.Display(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    tint: Color? = null
) = DisplayIcon(
    state = this,
    modifier = modifier,
    selected = selected,
    tint = tint,
)

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
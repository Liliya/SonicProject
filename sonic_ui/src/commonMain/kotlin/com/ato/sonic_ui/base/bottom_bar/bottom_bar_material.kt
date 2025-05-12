package com.ato.sonic_ui.base.bottom_bar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.base.Display
import com.ato.ui_state.base.NavBarItem
import com.ato.ui_state.base.UiIcon
import com.ato.ui_state.base.UiNavBar
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun UiNavBar.Display(onClick: (Int) -> Unit = { }) {
    NavigationBar(
        tonalElevation = 0.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .height(64.dp)
    ) {
        items.forEachIndexed { index, (uiIconText, text, isSelected) ->
            NavigationBarItem(
                selected = isSelected,
                onClick = { onClick(index) },
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                    ) {
                        uiIconText.Display(
                            tint = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else LocalContentColor.current.copy(alpha = 0.6f),
                            selected = isSelected
                        )
                        if (text != null) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else LocalContentColor.current.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun NavigationBar(vararg items: Pair<NavBarItem, () -> Unit>) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val list: List<Pair<NavBarItem, () -> Unit>> by remember { mutableStateOf(items.asList()) }

    NavigationBar {
        list.forEachIndexed { index, (navBarItem: NavBarItem, action) ->
            NavigationBarItem(
                selected = false,
                onClick = {
                    selectedTabIndex = index
                    action.invoke()
                },
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        navBarItem.icon.Display(selected = selectedTabIndex == index)
                        if (navBarItem.title != null) {
                            Text(
                                text = navBarItem.title!!,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            )
        }
    }
}

@Preview()
@Composable
fun UiNavBarItem_Empty_Preview() {
    UiNavBar(
        listOf()
    ).Display()
}

@Preview()
@Composable
fun MUiBottomBarItem_One_Preview() {
    UiNavBar(
        listOf(
            NavBarItem(UiIcon(Icons.Filled.Home), "Home")
        )
    ).Display()
}

@Preview()
@Composable
fun MUiBottomBarItem_Two_Preview() {
    UiNavBar(
        listOf(
            NavBarItem(
                icon = UiIcon(Icons.Filled.Home),
                title = "Home"
            ),
            NavBarItem(
                icon = UiIcon(Icons.Filled.Add),
                title = "Add"
            ),
        )
    ).Display()
}


@Preview()
@Composable
fun MUiBottomBarItem_No_Title_Two_Preview() {
    UiNavBar(
        listOf(
            NavBarItem(
                icon = UiIcon(Icons.Filled.Home),
                title = null
            ),
            NavBarItem(
                icon = UiIcon(Icons.Filled.Add),
                title = "Add"
            ),
        )
    ).Display()
}

@Preview()
@Composable
fun MUiBottomBarItem_Five_Preview() {
    UiNavBar(
        listOf(
            NavBarItem(
                icon = UiIcon(Icons.Filled.Home),
                title = "Home"
            ),
            NavBarItem(
                icon = UiIcon(Icons.Filled.Add),
                title = "Add"
            ),
            NavBarItem(
                icon = UiIcon(Icons.Filled.Settings),
                title = "Settings"
            ),
            NavBarItem(
                icon = UiIcon(Icons.Filled.Email),
                title = "Email"
            ),
            NavBarItem(
                icon = UiIcon(Icons.Filled.Edit),
                title = "Edit"
            ),
        )
    ).Display()
}
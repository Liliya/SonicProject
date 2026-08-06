package com.ato.sonic_ui.base.bottom_bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.base.Display
import com.ato.sonic_ui.base.badge.CountBadge
import com.ato.ui_state.base.NavBarItem
import com.ato.ui_state.base.UiIcon
import com.ato.ui_state.base.UiNavBar
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Нижняя навигация.
 *
 * Три вещи, которые тут были не так:
 * - подпись рисовалась внутри слота `icon`, поэтому пилюля выделения Material 3
 *   охватывала иконку вместе с текстом; теперь текст в своём слоте `label`, а
 *   пилюля — вокруг иконки, как и задумано;
 * - высота была жёстко 64dp, и при системном увеличении шрифта подпись
 *   обрезалась; теперь высота своя у `NavigationBar`;
 * - цвета брались из `LocalContentColor` с альфой, вместо ролей темы.
 */
@Composable
fun UiNavBar.Display(onClick: (Int) -> Unit = { }) {
    NavigationBar(
        tonalElevation = 0.dp,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        items.forEachIndexed { index, item ->
            val label = item.titleRes?.let { stringResource(it) } ?: item.title

            NavigationBarItem(
                modifier = Modifier.testTag("nav_item_$index"),
                selected = item.isSelected,
                onClick = { onClick(index) },
                icon = {
                    Box {
                        item.icon.Display(
                            // Подпись уже читается скринридером, поэтому иконку
                            // отдельно озвучивать не нужно.
                            tint = LocalContentColor.current,
                            selected = item.isSelected,
                        )
                        // Значок висит над правым верхним углом иконки, а не
                        // внутри неё: пилюля выделения обводит именно иконку, и
                        // значок под ней на выбранной вкладке было бы не видно.
                        CountBadge(
                            count = item.badgeCount,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 10.dp, y = (-4).dp),
                        )
                    }
                },
                label = label?.let {
                    {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
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
            NavBarItem(icon = UiIcon(Icons.Filled.Home), title = "Home")
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
fun MUiBottomBarItem_Badge_Preview() {
    UiNavBar(
        listOf(
            NavBarItem(
                icon = UiIcon(Icons.Filled.Home),
                title = "Home",
                isSelected = true,
            ),
            NavBarItem(
                icon = UiIcon(Icons.Filled.Email),
                title = "Email",
                badgeCount = 3,
            ),
            NavBarItem(
                icon = UiIcon(Icons.Filled.Settings),
                title = "Settings",
                badgeCount = 120,
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
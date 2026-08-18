package com.ato.sonic_ui.base.bottom_bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.NavigationBarDefaults
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
import androidx.compose.ui.platform.LocalDensity
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

/** Пилюля под выбранной вкладкой: у Material 3 она 64×32dp, в макете — плотнее. */
private val IndicatorWidth = 56.dp
private val IndicatorHeight = 28.dp
private val NavIconSize = 24.dp

/**
 * Высота полосы без системных отступов.
 *
 * У Material 3 это `defaultMinSize(80dp)`, а содержимому элемента нужно 52:
 * пилюля 28, отступ до подписи 8 (`IndicatorVerticalPadding` плюс
 * `NavigationBarIndicatorToLabelPadding`) и сама подпись 16. Оставшиеся
 * двадцать восемь точек расходились по четырнадцать сверху и снизу, и полоса
 * выглядела просторнее всего остального в приложении. Здесь 64: те же 52 плюс
 * по шесть.
 *
 * Ниже 60 опускать нельзя. Material 3 считает высоту элемента как
 * «содержимое плюс поля», где поле не меньше `IndicatorVerticalPadding` (4dp),
 * и при меньшей высоте элемент перестаёт помещаться в полосу — не сжимается, а
 * обрезается.
 */
private val BarHeight = 64.dp

/**
 * Нижняя навигация.
 *
 * Три вещи, которые тут были не так:
 * - подпись рисовалась внутри слота `icon`, поэтому пилюля выделения Material 3
 *   охватывала иконку вместе с текстом; теперь текст в своём слоте `label`, а
 *   пилюля — вокруг иконки, как и задумано;
 * - высота была жёстко 64dp, и при системном увеличении шрифта подпись
 *   обрезалась. Сначала её отдали `NavigationBar` целиком, но его минимум —
 *   80dp, и полоса вышла просторнее всего остального в приложении. Теперь
 *   высота снова своя ([BarHeight]), только растёт вместе с масштабом шрифта,
 *   поэтому подпись обрезать не может;
 * - цвета брались из `LocalContentColor` с альфой, вместо ролей темы.
 *
 * Пилюля рисуется своя, а материаловская гасится прозрачным цветом: её размер
 * зашит в токены `NavigationBarTokens` и параметром не задаётся, а нужный по
 * макету размер меньше стандартного.
 */
@Composable
fun UiNavBar.Display(onClick: (Int) -> Unit = { }) {
    val insets = NavigationBarDefaults.windowInsets

    // Полоса растёт вместе с системным размером шрифта: подпись под иконкой
    // набрана в sp, и на жёсткой высоте её однажды уже обрезало — ради этого
    // высоту когда-то и отдали Material 3 целиком. Множитель возвращает
    // плотность, не возвращая той поломки; меньше единицы не берём, потому что
    // на уменьшенном шрифте полосе сжиматься уже некуда.
    val fontScale = LocalDensity.current.fontScale.coerceAtLeast(1f)

    NavigationBar(
        // Системный отступ входит в высоту, потому что `NavigationBar`
        // добавляет его внутри себя: без этого слагаемого на телефоне с
        // жестовой навигацией полоса съела бы его из содержимого.
        modifier = Modifier.height(
            BarHeight * fontScale + insets.asPaddingValues().calculateBottomPadding()
        ),
        windowInsets = insets,
        tonalElevation = 0.dp,
        // `surfaceContainer`, а не `surface`: в Material 3 `surface` — это цвет
        // самого экрана, и панель, залитая им, от экрана ничем не отличается.
        // Роль контейнера на тон плотнее — ровно та полоса внизу, которую и
        // должно быть видно, без разделительной линии поверх.
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        items.forEachIndexed { index, item ->
            val label = item.titleRes?.let { stringResource(it) } ?: item.title

            NavigationBarItem(
                modifier = Modifier.testTag("nav_item_$index"),
                selected = item.isSelected,
                onClick = { onClick(index) },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(width = IndicatorWidth, height = IndicatorHeight)
                            .background(
                                color = if (item.isSelected) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    Color.Transparent
                                },
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box {
                            item.icon.Display(
                                modifier = Modifier.size(NavIconSize),
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
                    // Выбранная вкладка — бренд-цвет на своём бледном контейнере
                    // (в светлой теме 6.5:1, в тёмной 6:1). Было `onSecondaryContainer`
                    // и `onSurface`, то есть чёрным по зелёному: выбранная вкладка
                    // отличалась от остальных только фоном пилюли.
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    // Пилюля своя, см. Box в слоте icon.
                    indicatorColor = Color.Transparent,
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
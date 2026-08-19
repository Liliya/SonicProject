package com.ato.sonic_ui.base.bottom_bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ato.sonic_ui.base.Display
import com.ato.sonic_ui.base.badge.CountBadge
import com.ato.ui_state.base.NavBarItem
import com.ato.ui_state.base.UiIcon
import com.ato.ui_state.base.UiNavBar
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Пилюля под выбранной вкладкой.
 *
 * У Material 3 она 64×32dp, здесь была 56×28 — и всё равно перетягивала
 * внимание на себя: на экране из очень лёгких карточек выбранную вкладку
 * показывал прежде всего размер зелёной плашки, а не сама иконка. Теперь 46×25,
 * то есть −18% по ширине и −11% по высоте, а разницу добирает иконка —
 * [SelectedNavIconSize] против [NavIconSize].
 *
 * Рисуется пилюля не фоном слота, а поверх [drawBehind], и выходит за его
 * границы. Слот меряется по иконке, поэтому высота пилюли в высоту элемента не
 * входит: иначе полоса не могла бы стать ниже, чем «пилюля плюс подпись плюс
 * двенадцать точек материаловских полей», — см. [BarHeight].
 */
private val IndicatorWidth = 46.dp
private val IndicatorHeight = 25.dp

/**
 * Иконка вкладки. Было 24dp — материаловский размер; рядом с подписью в 11sp он
 * выглядел крупно, особенно у широких глифов вроде «Настроек» и «Подарю».
 */
private val NavIconSize = 22.dp

/**
 * Выбранная иконка на точку крупнее остальных (+4.5%): акцент на самой иконке,
 * а не только на плашке под ней.
 *
 * Рисуется через `requiredSize`, а не `size`: слот у всех вкладок одинаковый и
 * меряется по [NavIconSize], иначе выбранный элемент был бы на точку выше
 * остальных и высоту полосы задавал бы он.
 */
private val SelectedNavIconSize = 23.dp

/**
 * Ведущая подписи.
 *
 * Сама подпись — `labelSmall` (11sp вместо прежних 12sp у `labelMedium`), но
 * шкала задаёт ей 16sp: это ведущая для абзаца, а подпись здесь всегда в одну
 * строку, и лишние точки уходили в пустоту между иконкой и текстом. Четырнадцать
 * — это метрики самой гарнитуры, то есть текст не сжат, а просто без запаса на
 * вторую строку.
 */
private val LabelLineHeight = 14.sp

/**
 * Высота полосы без системных отступов.
 *
 * Было 64: пилюля 28, отступ до подписи 8, подпись 16 и по шесть сверху и снизу.
 * Полоса всё равно читалась тяжелее остального экрана. Теперь 55 (−14%), и
 * место нашлось не в полях, а в содержимом: иконка 22 вместо 24, подпись 11sp с
 * ведущей 14 вместо 12sp с ведущей 16, а пилюля вынесена из обмера.
 *
 * Ниже опускать нельзя. Material 3 считает высоту элемента как
 * `иконка + IndicatorVerticalPadding + NavigationBarIndicatorToLabelPadding +
 * подпись`, а потом добавляет по `IndicatorVerticalPadding` сверху и снизу.
 * Двенадцать точек полей параметрами не убираются, и при иконке 22 и подписи 14
 * элементу нужно 52 — остаток от 55 как раз уходит на [ContentLift].
 */
private val BarHeight = 55.dp

/**
 * На сколько содержимое приподнято над серединой полосы.
 *
 * Снизу у телефона своя системная полоса навигации, и оптический центр пункта
 * оказывался ниже геометрического. Три точки — примерно 5% высоты — возвращают
 * его на место.
 *
 * Это сдвиг, а не отступ: отступ вычитался бы из места под элемент, а места там
 * ровно столько, сколько ему нужно. Уехать за верхний край элементу не даёт
 * собственное материаловское поле в 4dp, из которого пилюля занимает полторы.
 */
private val ContentLift = 3.dp

/**
 * На сколько цвет неактивных вкладок сдвинут к цвету самой полосы.
 *
 * `onSurfaceVariant` — почти чёрный, и на светлой панели он давал 11.6:1: рядом
 * с воздушными карточками экрана невыбранные вкладки выглядели тяжелее всего
 * остального на нём. Восемнадцать процентов по Oklab дают 7:1 и в светлой, и в
 * тёмной теме — заметно легче и всё ещё в полтора раза выше нормы AA.
 *
 * Смешение с цветом полосы, а не альфа: цвет остаётся непрозрачным и не зависит
 * от того, что окажется под панелью.
 */
private const val UnselectedLightening = 0.18f

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

    // `surfaceContainer`, а не `surface`: в Material 3 `surface` — это цвет
    // самого экрана, и панель, залитая им, от экрана ничем не отличается.
    // Роль контейнера на тон плотнее — ровно та полоса внизу, которую и
    // должно быть видно, без разделительной линии поверх.
    val barColor = MaterialTheme.colorScheme.surfaceContainer
    val indicatorColor = MaterialTheme.colorScheme.secondaryContainer
    val unselectedColor = lerp(
        MaterialTheme.colorScheme.onSurfaceVariant,
        barColor,
        UnselectedLightening,
    )

    NavigationBar(
        // Системный отступ входит в высоту, потому что `NavigationBar`
        // добавляет его внутри себя: без этого слагаемого на телефоне с
        // жестовой навигацией полоса съела бы его из содержимого.
        modifier = Modifier.height(
            BarHeight * fontScale + insets.asPaddingValues().calculateBottomPadding()
        ),
        windowInsets = insets,
        tonalElevation = 0.dp,
        containerColor = barColor,
    ) {
        items.forEachIndexed { index, item ->
            val label = item.titleRes?.let { stringResource(it) } ?: item.title

            NavigationBarItem(
                modifier = Modifier
                    .offset(y = -ContentLift)
                    .testTag("nav_item_$index"),
                selected = item.isSelected,
                onClick = { onClick(index) },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(NavIconSize)
                            .drawBehind {
                                if (!item.isSelected) return@drawBehind

                                val pill = Size(
                                    width = IndicatorWidth.toPx(),
                                    height = IndicatorHeight.toPx(),
                                )
                                drawRoundRect(
                                    color = indicatorColor,
                                    topLeft = Offset(
                                        x = (size.width - pill.width) / 2f,
                                        y = (size.height - pill.height) / 2f,
                                    ),
                                    size = pill,
                                    cornerRadius = CornerRadius(pill.height / 2f),
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        item.icon.Display(
                            modifier = Modifier.requiredSize(
                                if (item.isSelected) SelectedNavIconSize else NavIconSize
                            ),
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
                            style = MaterialTheme.typography.labelSmall.copy(
                                lineHeight = LabelLineHeight,
                            ),
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
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor,
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
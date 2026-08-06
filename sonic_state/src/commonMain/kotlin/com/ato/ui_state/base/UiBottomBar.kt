package com.ato.ui_state.base

import org.jetbrains.compose.resources.StringResource

data class UiBottomBar(
    val items: List<UiIconText>
)

data class UiNavBar(
    val items: List<NavBarItem>
)

data class NavBarItem(
    val icon: UiIcon,
    /**
     * Подпись вкладки как ресурс. Предпочтительный вариант: домен не должен
     * доставать строки, а вкладка без подписи — это четыре одинаковых серых
     * значка, по которым не угадать, где события, а где друзья.
     */
    val titleRes: StringResource? = null,
    /** Готовая строка. Остаётся для превью и для подписей, которых нет в ресурсах. */
    val title: String? = null,
    val isSelected: Boolean = false,
    val reset: Boolean = false,
    /**
     * Сколько всего ждёт ответа за этой вкладкой. Ноль — значка нет.
     *
     * Число, а не флаг: «есть что-то новое» и «новых три» — разные сообщения,
     * и второе стоит ровно столько же.
     */
    val badgeCount: Int = 0,
    val meta: Any? = null
)



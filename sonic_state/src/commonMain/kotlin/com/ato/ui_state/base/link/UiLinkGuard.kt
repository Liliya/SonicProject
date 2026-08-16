package com.ato.ui_state.base.link

import org.jetbrains.compose.resources.StringResource

/**
 * Слова, которыми карточка ссылки объясняет, что с ссылкой не так.
 *
 * Разбор ссылки — в `com.ato.helpers.links.LinkSafety`, он про строки и ничего
 * не знает про язык. Слова живут в `common_resources` приложения, о котором
 * `sonic_ui` не знает вовсе. Значит, они приходят сюда — как и весь остальной
 * текст в этих модулях.
 *
 * Собирается в одном месте — `com.ato.common_resources.wish.linkGuard()`, —
 * чтобы предупреждение об одном и том же было одним и тем же на экране
 * добавления, изменения и просмотра желания.
 */
data class UiLinkGuard(
    /** Заголовок вопроса перед выходом из приложения. */
    val confirmTitle: StringResource,
    val confirmOpen: StringResource,
    val confirmCancel: StringResource,
    /** «Настоящий адрес — то, что после @». */
    val hiddenPrefix: StringResource,
    /** «Имя сайта написано так, чтобы выглядеть чужим». */
    val disguisedName: StringResource,
    /** «Соединение без шифрования» — показывается, но не спрашивает. */
    val notEncrypted: StringResource,
    /** Это не адрес: открывать нечего. */
    val notALink: StringResource,
    /** Схема не http и не https: открылась бы не страница. */
    val foreignScheme: StringResource,
)

package com.ato.ui_state.base.menu

import com.ato.ui_state.Ui
import org.jetbrains.compose.resources.StringResource

data class UiSections(
    val items: List<Section>,
) : Ui

data class Section(
    val title: StringResource? = null,
    val name: String? = null,
    val isSelected: Boolean = false,
    /** Сколько за разделом ждёт ответа. Ноль — значка нет. */
    val badgeCount: Int = 0,
    /**
     * Сколько всего лежит за разделом — размер списка, а не «ждёт ответа».
     *
     * `null`, пока не посчитали: ноль означал бы «пусто», а на холодном старте
     * это была бы неправда, которая тут же сменилась бы на другое число.
     */
    val count: Int? = null,
)
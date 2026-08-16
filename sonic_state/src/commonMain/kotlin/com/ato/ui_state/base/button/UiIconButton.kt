package com.ato.ui_state.base.button

import androidx.compose.ui.graphics.vector.ImageVector
import com.ato.ui_state.Button
import com.ato.ui_state.Ui
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class UiIconButton(
    val icon: ImageVector,
    val key: String? = null,
    val isEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val isVisible: Boolean = true,
    /**
     * Подпись для скринридера. У кнопки нет текста — только значок, — поэтому
     * без неё это «кнопка» и больше ничего.
     */
    val contentDescription: String? = null,
    /**
     * Рисовать кнопку кружком со значком вместо залитой кнопки во всю высоту.
     *
     * Для второстепенных действий в шапке — поиска, например. Залитая
     * зелёная кнопка рядом с заголовком весит столько же, сколько сам
     * заголовок, и экран получает два главных элемента вместо одного; там же,
     * где значок подтверждает форму («Готово», «Добавить»), вес как раз нужен,
     * поэтому по умолчанию всё остаётся как было.
     */
    val isCompact: Boolean = false,
): Ui, Button



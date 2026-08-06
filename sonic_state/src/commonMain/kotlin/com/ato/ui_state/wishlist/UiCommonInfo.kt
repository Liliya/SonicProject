package com.ato.ui_state.wishlist

import com.ato.ui_state.base.text.UiSimpleText

data class UiCommonInfo(
    val info: List<Pair<UiSimpleText, UiSimpleText>>,
)

/**
 * Шапка доски.
 *
 * Раньше это была таблица «поле — значение» на отдельной вкладке «О доске»:
 * название доски (оно же в заголовке над таблицей), количество желаний,
 * завершено, взято, приватность. Вкладка стоила половину экрана и одного
 * нажатия, а показывала пять строк, из которых первая дублировала заголовок, а
 * ещё две обычно были нулями. Теперь то же самое — две строки под названием.
 */
data class UiBoardInfo(
    /**
     * Картинка доски: `board://preset/N` из
     * [com.ato.ui_state.base.image.BoardPresets] либо старое эмодзи.
     */
    val picture: String?,
    /**
     * Сводка счётчиков. Части соединяются точкой при показе; пустые счётчики
     * сюда не попадают, поэтому у доски без выполненных и забронированных
     * желаний остаётся одно число, а не строка нулей.
     */
    val summary: List<UiSimpleText>,
    val privacy: UiSimpleText,
)
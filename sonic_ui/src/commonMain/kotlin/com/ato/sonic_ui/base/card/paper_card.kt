package com.ato.sonic_ui.base.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp

/**
 * Карточка-«лист бумаги»: заливка чуть светлее фона плюс волосяная граница.
 *
 * Тени нет вовсе — приподнятая карточка спорит с плоской типографикой экранов,
 * а отделить её от молочного фона хватает и линии толщиной в волос.
 *
 * Правило вынесено сюда, потому что так выглядят карточки на «Подарю» и так же
 * должны выглядеть карточки людей в списках. Два экрана, набирающие один и тот
 * же цвет своими руками, расходятся при первой же правке темы — и расходятся
 * молча, потому что рядом их не видно.
 */

/**
 * Заливка карточки.
 *
 * По светлоте фона, а не по `isSystemInDarkTheme()`: тему можно переключить и
 * внутри приложения, и тогда системный флаг врёт. В светлой теме карточка
 * светлее фона, в тёмной — темнее, и в обеих она остаётся «листом», а не
 * подсвеченным прямоугольником.
 */
@Composable
fun paperCardColor(): Color {
    val scheme = MaterialTheme.colorScheme

    return if (scheme.surface.luminance() > 0.5f) {
        scheme.surfaceContainerLowest
    } else {
        scheme.surfaceContainerHigh
    }
}

/** Граница карточки: волос, приглушённый до 0.7 — контур, а не рамка. */
@Composable
fun paperCardBorder(): BorderStroke = BorderStroke(
    width = Dp.Hairline,
    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
)

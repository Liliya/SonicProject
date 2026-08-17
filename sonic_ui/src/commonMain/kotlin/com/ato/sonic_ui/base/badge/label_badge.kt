package com.ato.sonic_ui.base.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.base.text.DisplayText
import com.ato.ui_state.base.text.UiSimpleText

/**
 * Короткая подпись-«пилюля»: ярлык, приклеенный к тому, рядом с чем стоит —
 * «Основная» у названия доски.
 *
 * Ярлык, а не свойство со значением. «Основная» — целиком то, что нужно знать:
 * доска или основная, или нет. Как только внутрь просится значение («видно
 * всем», «5 участников»), пилюля перестаёт работать: из тега «Всем» не понять,
 * всем чего, и такому место в обычной строке, где рядом стоит слово, которое
 * объясняет число.
 *
 * Не [CountBadge] — тот кружок с числом и красный, потому что означает «здесь
 * что-то ждёт ответа». Здесь ровно наоборот: это спокойная пометка о свойстве
 * того, на чём она стоит, и в глаза она бросаться не должна.
 *
 * И не `FilterChip` из `base/chip`: чип — это орган управления, он нажимается и
 * имеет состояние «выбран». Пилюля не нажимается никогда. Внешне они похожи, и
 * именно поэтому пилюля собрана отдельно: чип, на который нельзя нажать, —
 * обещание, которого интерфейс не выполняет.
 *
 * Радиус — `extraSmall`: по шкале приложения это радиус чипов и мелких
 * бейджей.
 *
 * Цвет один и не настраивается, и это решение, а не упущение. Тон в карточке
 * отдан действию — кнопке «плюс», — а ярлык рядом с ней информационный. Тонируй
 * его, и на карточке окажется два цветных пятна, спорящих за внимание, а цвет
 * перестанет значить «сюда можно нажать».
 */
@Composable
fun LabelBadge(
    text: UiSimpleText,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(BADGE_HEIGHT)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = MaterialTheme.shapes.extraSmall,
            )
            .padding(horizontal = BADGE_INSET),
        contentAlignment = Alignment.Center,
    ) {
        // Одна строка и многоточие: пилюля стоит в ряду с другими, и перевод
        // подлиннее английского не должен превращать ряд пометок в абзац.
        DisplayText(
            state = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Высота пилюли.
 *
 * Задана явно, а не полями вокруг текста: пилюля стоит в строке рядом с
 * названием доски, и от кегля подписи её высота зависеть не должна — иначе
 * строка съезжает при первой правке шкалы.
 */
private val BADGE_HEIGHT = 20.dp

/** Поля по горизонтали. По вертикали их нет — высота фиксированная. */
private val BADGE_INSET = 8.dp

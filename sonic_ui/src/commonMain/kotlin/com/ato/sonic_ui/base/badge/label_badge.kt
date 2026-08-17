package com.ato.sonic_ui.base.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.base.text.DisplayText
import com.ato.ui_state.base.text.UiSimpleText

/**
 * Короткая подпись-«пилюля»: «Основная», «Только друзья».
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
 * отдан действию — кнопке «плюс», — а пометки рядом с ней информационные.
 * Тонируй одну из них, и на карточке окажется два цветных пятна, спорящих за
 * внимание; тонируй обе — цвет перестанет значить «сюда можно нажать».
 * Различаются пометки словом и порядком, а не цветом.
 */
@Composable
fun LabelBadge(
    text: UiSimpleText,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = MaterialTheme.shapes.extraSmall,
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
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

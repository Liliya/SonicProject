package com.ato.sonic_ui.base.image

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ato.ui_state.base.image.BoardPresets

/**
 * Выбор встроенной картинки доски.
 *
 * Сетка собрана обычными [Row], а не ленивой: картинок ровно
 * [BoardPresets.COUNT], и ленивая сетка внутри шторки пришлось бы ограничивать
 * по высоте, иначе она конфликтует со скроллом самой шторки.
 *
 * [selectedIndex] может быть `null` — у доски, которой ничего не выбирали, не
 * должно быть отмеченной ячейки, иначе выбор выглядит уже сделанным.
 */
@Composable
fun BoardPresetPicker(
    selectedIndex: Int?,
    onPicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 4,
) {
    val gap = 12.dp

    Column(modifier = modifier.fillMaxWidth()) {
        (0 until BoardPresets.COUNT).chunked(columns).forEachIndexed { rowIndex, row ->
            if (rowIndex > 0) Spacer(Modifier.height(gap))

            Row(
                horizontalArrangement = Arrangement.spacedBy(gap),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { index ->
                    BoardPresetCell(
                        index = index,
                        isSelected = index == selectedIndex,
                        onPicked = onPicked,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Хвост последнего ряда: без распорок оставшиеся ячейки
                // растянулись бы на всю ширину и стали больше остальных.
                repeat(columns - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BoardPresetCell(
    index: Int,
    isSelected: Boolean,
    onPicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium

    BoardPresetImage(
        index = index,
        shape = shape,
        modifier = modifier
            .aspectRatio(1f)
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = { onPicked(index) },
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = shape,
                    )
                } else {
                    Modifier
                }
            )
    )
}

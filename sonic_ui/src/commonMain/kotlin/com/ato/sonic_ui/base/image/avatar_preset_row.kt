package com.ato.sonic_ui.base.image

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ato.ui_state.base.image.AvatarPresets

/**
 * Ряд встроенных аватарок для выбора.
 *
 * Подписи для скринридера приходят снаружи: локализованные строки лежат в
 * `common_resources` приложения, а этот модуль о нём ничего не знает.
 *
 * @param selected какой пресет сейчас выбран, или `null` если стоит фотография
 * @param itemContentDescription подпись для пресета с этим номером
 */
@Composable
fun AvatarPresetRow(
    selected: Int?,
    onPresetClicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemSize: Dp = 64.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    itemContentDescription: @Composable (Int) -> String? = { null },
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = List(AvatarPresets.COUNT) { it }, key = { it }) { index ->
            val isSelected = index == selected

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(itemSize)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { onPresetClicked(index) }
                    )
                    .semantics { this.selected = isSelected }
                    .avatarLabel(itemContentDescription(index))
            ) {
                // Кольцо рисуется снаружи картинки, а не поверх: обводка поверх
                // круга съедала бы часть рисунка, и выбранная аватарка
                // отличалась бы от того, что человек увидит в профиле.
                AvatarPresetImage(
                    index = index,
                    modifier = Modifier
                        .size(if (isSelected) itemSize - 8.dp else itemSize)
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(itemSize)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            )
                            .padding(1.dp)
                    )
                }
            }
        }
    }
}

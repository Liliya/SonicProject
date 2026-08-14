package com.ato.sonic_ui.base.image

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ato.ui_state.base.image.UiImageGallery

/**
 * Ряд картинок желания с плиткой «добавить» в конце.
 *
 * Раньше картинка была одна, и её редактирование было квадратом на 256dp
 * посреди экрана: место под ровно одну штуку, и никакого способа показать, что
 * их может быть больше. Ряд миниатюр говорит это сам, без подписи, — и он же
 * показывает, сколько ещё влезет: плитка «добавить» пропадает на последней.
 *
 * `LazyRow`, а не `Row` с равными долями: доли делили бы ширину экрана между
 * тем, что есть, и одна картинка растягивалась бы во весь экран, а три
 * съёживались. Миниатюра одного размера всегда, а если ряд не помещается —
 * прокручивается.
 */
@Composable
fun ImageGalleryEditor(
    state: UiImageGallery,
    onAddClicked: () -> Unit,
    onRemoveClicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemSize: Dp = 96.dp,
    shape: Shape = MaterialTheme.shapes.medium,
    addContentDescription: String? = null,
    removeContentDescription: String? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = contentPadding,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexedSlots(state, itemSize, shape, removeContentDescription, onRemoveClicked)

        if (state.canAddMore) {
            item(key = "add") {
                AddTile(
                    size = itemSize,
                    shape = shape,
                    contentDescription = addContentDescription,
                    onClick = onAddClicked,
                )
            }
        }
    }
}

/**
 * Вынесено из тела [LazyRow] отдельной функцией, чтобы ключи миниатюр и плитки
 * «добавить» задавались в одном месте: без ключей `LazyRow` считает элементы по
 * порядковому номеру, и удаление картинки из середины переносило бы состояние
 * соседней на её место.
 */
private fun LazyListScope.itemsIndexedSlots(
    state: UiImageGallery,
    itemSize: Dp,
    shape: Shape,
    removeContentDescription: String?,
    onRemoveClicked: (Int) -> Unit,
) {
    state.items.forEachIndexed { index, slot ->
        item(key = slot.url ?: "file-$index") {
            Box {
                WishPicture(
                    url = slot.url,
                    file = slot.file,
                    size = itemSize,
                    shape = shape,
                    modifier = Modifier.border(
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                        ),
                        shape = shape,
                    ),
                )

                // Крестик наполовину вынесен за угол миниатюры: внутри он
                // закрывал бы саму картинку, а у 96dp миниатюры её и так немного.
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button) { onRemoveClicked(index) },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = removeContentDescription,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddTile(
    size: Dp,
    shape: Shape,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                ),
                shape = shape,
            )
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
    }
}

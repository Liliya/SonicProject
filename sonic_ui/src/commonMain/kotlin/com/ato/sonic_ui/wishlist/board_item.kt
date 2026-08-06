package com.ato.sonic_ui.wishlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.base.image.BoardPicture
import com.ato.sonic_ui.base.text.DisplayText
import com.ato.ui_state.wishlist.UiBoard


@Composable
fun DisplayBoard(
    data: UiBoard,
    onAddClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    addButtonTestTag: String? = null,
    addContentDescription: String? = null,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        // Одна заливка и никакой рамки: раньше карточка была залита
        // `surfaceVariant` и обведена одновременно, а в Material 3 это либо
        // залитая карточка, либо outlined — не оба сразу. `surfaceContainer` —
        // та роль, которая для контейнера карточки и предназначена, и в теме
        // она уже описана вместе со всей шкалой.
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Раньше здесь был эмодзи на прозрачном кружке: без подложки и без
            // кольца он висел в пустоте и читался как «не дорисовали».
            BoardPicture(
                value = data.board?.emoji,
                seed = data.board?.documentId,
                size = 44.dp,
                shape = MaterialTheme.shapes.small,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Название доски — заголовок, а не абзац текста: `bodyLarge`
                // ставил его вровень со счётчиком под ним, и список читался
                // одинаково серым.
                DisplayText(
                    state = data.boardName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                DisplayText(
                    state = data.boardWishCount,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onAddClicked != null) {
                Spacer(modifier = Modifier.width(12.dp))

                CenteredIconCard(
                    elevation = CardDefaults.cardElevation(0.dp),
                    icon = Icons.Filled.Add,
                    onClick = onAddClicked,
                    contentDescription = addContentDescription,
                    // Было 40dp — ниже минимума в 48dp, из-за чего в том числе
                    // промахивались UI-тесты.
                    modifier = Modifier
                        .height(48.dp)
                        .width(48.dp)
                        .let { if (addButtonTestTag != null) it.testTag(addButtonTestTag) else it }
                )
            }
        }
    }
}

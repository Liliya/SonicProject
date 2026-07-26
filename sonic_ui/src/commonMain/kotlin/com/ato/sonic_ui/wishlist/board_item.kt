package com.ato.sonic_ui.wishlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.ato.sonic_ui.base.emoji.DisplayEmojiAva
import com.ato.sonic_ui.base.text.DisplayText
import com.ato.ui_state.wishlist.UiBoard


@Composable
fun DisplayBoard(
    data: UiBoard,
    onAddClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    addButtonTestTag: String? = null,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder(), // тонкая рамка вместо тени
        elevation = CardDefaults.cardElevation(0.dp) // без тени — минимализм
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (data.boardEmoji != null) {
                DisplayEmojiAva(
                    size = 48f,           // немного меньше, чтобы не перетягивал фокус
                    sizeFactor = 0.65f,
                    emoji = data.boardEmoji,
                    boarderAlpha = 0f
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                DisplayText(
                    state = data.boardName,
                    style = MaterialTheme.typography.bodyLarge,
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
                    modifier = Modifier
                        .height(40.dp)
                        .width(40.dp)
                        .let { if (addButtonTestTag != null) it.testTag(addButtonTestTag) else it }
                )
            }
        }
    }
}

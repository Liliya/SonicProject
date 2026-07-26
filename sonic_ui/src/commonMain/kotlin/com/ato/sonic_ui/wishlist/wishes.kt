package com.ato.sonic_ui.wishlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.base.image.DisplayImage
import com.ato.ui_state.base.image.UiImagePicker
import com.ato.ui_state.wishlist.WishlistWish


@Composable
fun DisplayWish(
    wish: WishlistWish,
    onClick: (WishlistWish) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardColors = if (wish.isCompleted == true) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    } else {
        CardDefaults.cardColors()
    }

    Card(
        modifier = modifier,
        colors = cardColors,
        onClick = remember(wish) { { onClick.invoke(wish) } },
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    wish.name?.let { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = wish.description.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                wish.imageUrl?.let { imageUrl ->
                    Box(
                        modifier = Modifier,
                        contentAlignment = Alignment.Center
                    ) {
                        DisplayImage(
                            imagePikerState = UiImagePicker(imageUrl),
                            size = 64f,
                            shape = MaterialTheme.shapes.extraSmall,
                            onImageClicked = { onClick.invoke(wish) }
                        )
                    }
                }
            }
        }
    }
}

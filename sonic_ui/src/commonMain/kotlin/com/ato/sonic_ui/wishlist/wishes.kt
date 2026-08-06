package com.ato.sonic_ui.wishlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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


/**
 * Строка списка желаний.
 *
 * Была высокой и наполовину пустой: картинка на 64dp задавала высоту карточке,
 * текст висел в ней по центру, а пустое описание всё равно рисовалось — пустой
 * `Text` занимает строку, и под названием получалась дырка. На доске с одним
 * желанием экран выглядел недоделанным.
 *
 * Теперь миниатюра 48dp, описание рисуется только когда оно есть, и в тот же
 * вертикальный размер помещается вчетверо больше желаний.
 */
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
        shape = MaterialTheme.shapes.medium,
        colors = cardColors,
        onClick = remember(wish) { { onClick.invoke(wish) } },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            wish.imageUrl?.let { imageUrl ->
                DisplayImage(
                    imagePikerState = UiImagePicker(imageUrl),
                    size = 48f,
                    shape = MaterialTheme.shapes.small,
                    onImageClicked = { onClick.invoke(wish) }
                )
                Spacer(Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                wish.name?.let { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Пустое описание раньше всё равно занимало строку.
                wish.description?.takeIf { it.isNotBlank() }?.let { description ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

package com.ato.sonic_ui.wishlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.base.card.paperCardBorder
import com.ato.sonic_ui.base.card.paperCardColor
import com.ato.sonic_ui.base.image.DisplayImage
import com.ato.sonic_ui.base.image.WishPicture
import com.ato.sonic_ui.base.text.LinkPreview
import com.ato.ui_state.base.image.UiImagePicker
import com.ato.ui_state.base.link.UiLinkGuard
import com.ato.ui_state.wishlist.WishlistWish


@Composable
fun DisplayFullWish(
    wish: WishlistWish,
    linkGuard: UiLinkGuard,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        // Как и строка желания в списке — «лист бумаги» вместо серой заливки
        // по умолчанию. Экран желания открывается прямо из списка, и карточка,
        // меняющая цвет на переходе, читается как другой объект.
        colors = CardDefaults.cardColors(containerColor = paperCardColor()),
        border = paperCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            val images = wish.images
            when (images.size) {
                0 -> Unit

                // Одна картинка остаётся ровно тем же квадратом посреди
                // карточки, что и раньше: у большинства желаний она одна, и
                // ряд из одного элемента выглядел бы как недогрузившийся.
                1 -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        DisplayImage(
                            imagePikerState = UiImagePicker(images.first()),
                            shape = MaterialTheme.shapes.medium,
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                else -> {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(images) { image ->
                            WishPicture(
                                url = image,
                                size = 160.dp,
                                shape = MaterialTheme.shapes.medium,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            wish.name?.let { name ->
                SelectionContainer {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (!wish.description.isNullOrEmpty()) {
                SelectionContainer {
                    Text(
                        text = wish.description.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Каждая ссылка — своя карточка с именем сайта. Раньше здесь стоял
            // сам адрес целиком, и ссылка из поиска занимала экран на двадцать
            // строк, из которых читалось одно слово.
            //
            // Это чужое желание: ссылку в нём набирал не тот, кто её сейчас
            // читает. Поэтому карточка не просто сокращает адрес, а проверяет
            // его — см. [LinkPreview].
            wish.links.forEach { link ->
                LinkPreview(
                    url = link,
                    guard = linkGuard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
        }
    }
}

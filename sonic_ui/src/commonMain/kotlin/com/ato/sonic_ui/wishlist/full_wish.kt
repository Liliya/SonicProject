package com.ato.sonic_ui.wishlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.base.card.paperCardBorder
import com.ato.sonic_ui.base.card.paperCardColor
import com.ato.sonic_ui.base.image.WishGallery
import com.ato.sonic_ui.base.text.LinkPreview
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
        Column(modifier = Modifier.fillMaxWidth()) {
            // Фотографии во всю ширину и без полей: карточка обрезает
            // содержимое по своему скруглению, поэтому кадр доходит до её краёв
            // и верхние углы получаются скруглёнными. Поля начинаются ниже, у
            // текста — иначе их пришлось бы гасить отрицательным отступом.
            //
            // Раньше здесь стояло два разных вида: одна фотография — квадратом
            // 128dp по центру, две-три — рядом миниатюр по 160dp, который надо
            // было скроллить, не зная, что он скроллится. Один блок на любое
            // число снимков, с перелистыванием и точками, — в WishGallery.
            WishGallery(
                images = wish.images,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
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
}

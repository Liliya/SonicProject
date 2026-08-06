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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
 * **Выполненное желание больше не подсвечивается.** Карточка заливалась
 * `primary` с прозрачностью — тем же цветом, которым в этом интерфейсе
 * выделяют важное и выбранное. Получалось наоборот: сделанное кричало громче
 * несделанного, и на доске взгляд первым делом падал на то, чем заниматься уже
 * не надо. Теперь выполненное приглушено — зачёркнутое название серым — и
 * подписано словом.
 *
 * Одного зачёркивания мало: скринридер его не произносит, а на однострочном
 * названии с многоточием оно ещё и мешает читать. Поэтому вместе с ним идут
 * [completedLabel] на месте описания и `stateDescription` на карточке —
 * состояние объявляется словами, а не только начертанием.
 *
 * @param completedLabel «Выполнено» на языке интерфейса. Приходит снаружи,
 *   потому что строки живут в ресурсах приложения, а не библиотеки.
 */
@Composable
fun DisplayWish(
    wish: WishlistWish,
    onClick: (WishlistWish) -> Unit,
    completedLabel: String,
    modifier: Modifier = Modifier,
) {
    val isCompleted = wish.isCompleted == true

    Card(
        modifier = if (isCompleted) {
            modifier.semantics { stateDescription = completedLabel }
        } else {
            modifier
        },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(),
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
                        color = if (isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (isCompleted) {
                    // Вместо описания, а не вдобавок к нему: у сделанного
                    // желания «что это» уже не важно, важно «оно сделано», а
                    // лишняя строка вернула бы список к прежней рыхлости.
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = completedLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                } else {
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
}

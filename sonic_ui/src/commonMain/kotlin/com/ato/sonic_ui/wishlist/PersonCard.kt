package com.ato.sonic_ui.wishlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.base.image.DisplayImage
import com.ato.sonic_ui.base.skeleton.SkeletonBlock
import com.ato.ui_state.base.image.UiImagePicker

@Composable
fun PersonCard(
    name: String,
    nick: String,
    avaUrl: String?,
    onClick: (() -> Unit)? = null,
    colors: CardColors = CardDefaults.cardColors(),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = colors,
        onClick = { onClick?.invoke() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Одна строка и многоточие: длинное имя раньше переносилось на
                // три-четыре строки и растягивало карточку выше соседних.
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "@$nick",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 8.dp)
                )
            }
            // Раньше кружок рисовался только под `avaUrl != null`, поэтому у
            // людей без фотографии карточка была голой, а у остальных нет —
            // и список ехал по вертикали. С монограммой аватарка есть всегда,
            // и ряд карточек наконец одной высоты.
            Box(
                modifier = Modifier.padding(8.dp).padding(end = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                DisplayImage(
                    imagePikerState = UiImagePicker(avaUrl),
                    size = 64f,
                    avatarSeed = nick,
                    avatarName = name.ifBlank { nick },
                    onImageClicked = onClick ?: {}
                )
            }
        }
    }
}

/**
 * Ширина карточки человека в горизонтальном списке.
 *
 * Задана явно, и это не украшательство: в `LazyRow` ширина ребёнка не
 * ограничена ничем, а `Modifier.weight(1f)` при бесконечной ширине превращается
 * в ноль. Именно поэтому «добавленные пользователи» выглядели как столбики из
 * букв — обычная [PersonCard] сжималась до нулевой ширины, и имя переносилось
 * по одному символу на строку.
 */
private val COMPACT_CARD_WIDTH = 104.dp
private val COMPACT_AVATAR_SIZE = 56f

/**
 * Карточка человека для горизонтального списка: аватарка сверху, под ней имя и
 * ник в одну строку.
 *
 * Обычная [PersonCard] здесь не годится — она рассчитана на всю ширину экрана,
 * и в ряду из трёх-четырёх человек либо не помещается, либо схлопывается.
 */
@Composable
fun PersonCompactCard(
    name: String,
    nick: String,
    avaUrl: String?,
    onClick: (() -> Unit)? = null,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(COMPACT_CARD_WIDTH),
        shape = MaterialTheme.shapes.medium,
        colors = colors,
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = { onClick?.invoke() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            DisplayImage(
                imagePikerState = UiImagePicker(avaUrl),
                size = COMPACT_AVATAR_SIZE,
                avatarSeed = nick,
                avatarName = name.ifBlank { nick },
                onImageClicked = onClick ?: {}
            )

            Spacer(Modifier.height(8.dp))

            // Имя может быть пустым — тогда показываем ник, иначе под аватаркой
            // остаётся дырка, а строкой ниже всё равно написано «@ник».
            Text(
                text = name.ifBlank { nick },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "@$nick",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Заглушка [PersonCompactCard] на время загрузки — той же формы и ширины. */
@Composable
fun PersonCompactCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.width(COMPACT_CARD_WIDTH),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            SkeletonBlock(
                modifier = Modifier.size(COMPACT_AVATAR_SIZE.dp),
                shape = CircleShape
            )
            Spacer(Modifier.height(8.dp))
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
            )
            Spacer(Modifier.height(6.dp))
            SkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(12.dp)
            )
        }
    }
}

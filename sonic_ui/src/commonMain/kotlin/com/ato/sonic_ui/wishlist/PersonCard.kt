package com.ato.sonic_ui.wishlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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

/**
 * Аватарка человека в списке.
 *
 * 64dp уходило только на кружок, и карточка на одного человека занимала
 * восьмую часть экрана. 48 хватает, чтобы узнать лицо, и список наконец
 * показывает больше двух человек за раз.
 */
private const val AVATAR_SIZE = 48f

/**
 * Строка со человеком: аватарка слева, справа имя и `@ник`.
 *
 * Раньше было наоборот — текст слева, кружок у правого края, — и список
 * читался хуже: взгляд идёт сверху вниз по левому краю, а там были имена
 * разной длины вместо ровного столбца аватарок.
 *
 * Карточка молочная с тонкой обводкой, как группы на «Подарю»: серая заливка
 * осталась за некликабельными блоками, а всё, что нажимается, выглядит
 * одинаково. Шеврон рисуется только когда есть [onClick] — стрелка на строке,
 * которая никуда не ведёт, обещает переход, которого нет.
 *
 * @param status маленькая подпись под `@ником` — например «Ждёт ответа».
 *   Слот, а не строка, потому что цвет у статуса свой в каждом списке.
 */
@Composable
fun PersonCard(
    name: String,
    nick: String,
    avaUrl: String?,
    onClick: (() -> Unit)? = null,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    border: BorderStroke? = CardDefaults.outlinedCardBorder(),
    status: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.large
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)

    if (onClick == null) {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            border = border,
            elevation = elevation,
        ) {
            PersonRow(name, nick, avaUrl, onClick = null, status = status)
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            border = border,
            elevation = elevation,
            onClick = onClick,
        ) {
            PersonRow(name, nick, avaUrl, onClick = onClick, status = status)
        }
    }
}

@Composable
private fun PersonRow(
    name: String,
    nick: String,
    avaUrl: String?,
    onClick: (() -> Unit)?,
    status: (@Composable () -> Unit)?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        // Кружок рисуется всегда, даже без `avaUrl`: со встроенным пресетом
        // аватарка есть у каждого, и ряд карточек одной высоты.
        DisplayImage(
            imagePikerState = UiImagePicker(avaUrl),
            size = AVATAR_SIZE,
            avatarSeed = nick,
            onImageClicked = onClick ?: {}
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Имени может не быть вовсе — тогда главной строкой становится
            // `@ник`, иначе сверху оставалась бы пустая строка.
            val hasName = name.isNotBlank()

            // Одна строка и многоточие: длинное имя раньше переносилось на
            // три-четыре строки и растягивало карточку выше соседних.
            Text(
                text = if (hasName) name else "@$nick",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (hasName) {
                Text(
                    text = "@$nick",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            status?.let {
                Spacer(Modifier.height(4.dp))
                it()
            }
        }

        if (onClick != null) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
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

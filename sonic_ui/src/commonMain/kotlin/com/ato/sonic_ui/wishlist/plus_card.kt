package com.ato.sonic_ui.wishlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CenteredIconCard(
    icon: ImageVector,
    onClick: () -> Unit,
    elevation: CardElevation = CardDefaults.cardElevation(),
    colors: CardColors = CardDefaults.cardColors(),
    modifier: Modifier = Modifier,
    /**
     * Что произнесёт скринридер. У карточки нет текста, поэтому без подписи она
     * для него — безымянная кнопка.
     */
    contentDescription: String? = null,
    /**
     * Форма карточки. По умолчанию — та, что даёт `Card`, чтобы прежние вызовы
     * не изменились; передаётся там, где кнопка стоит рядом с чем-то своей
     * формы и обязана с ним рифмоваться.
     */
    shape: Shape = CardDefaults.shape,
    /**
     * Размер значка. Он не выводится из размера карточки: у одной и той же
     * кнопки 48dp значок может быть и крупным (главное действие), и мелким
     * (второстепенное), а угадывать это по площади — значит менять смысл при
     * каждой правке размера.
     */
    iconSize: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Card(
        modifier = modifier,
        shape = shape,
        elevation = elevation,
        colors = colors,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize),
                tint = tint
            )
        }
    }
}
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
import androidx.compose.ui.graphics.vector.ImageVector
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
) {
    Card(
        modifier = modifier,
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
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
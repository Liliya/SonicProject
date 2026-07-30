package com.ato.sonic_ui.base.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CircularIcon(
    icon: ImageVector,
    iconSize: Dp = 20.dp,
    /** Не меньше 48dp: это и область нажатия. */
    circleSize: Dp = 48.dp,
    /**
     * Кружок под иконкой. По умолчанию прозрачный — иконка на однотонной
     * поверхности в подложке не нуждается. Задавать его нужно там, где под
     * кнопкой может оказаться что угодно: фотография, градиент, чужая
     * картинка. Тогда [iconTint] берут контрастным именно к нему, и читаемость
     * перестаёт зависеть от того, что там внизу.
     */
    backgroundColor: Color = Color.Transparent,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(circleSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = iconTint
        )
    }
}
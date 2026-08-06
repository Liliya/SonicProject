package com.ato.sonic_ui.base.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Значок с числом — «здесь что-то ждёт ответа».
 *
 * Собран из `Box` и `Text`, а не из `Badge`/`BadgedBox` material3, по двум
 * причинам. `BadgedBox` сам решает, где значку быть относительно содержимого, и
 * в нижней навигации это оказывается не там, где нужно, — здесь место выбирает
 * вызывающая сторона своим `Modifier`. А `Badge` в разных версиях material3 то
 * помечен экспериментальным, то нет, и зависеть от этого ради круга с цифрой
 * не стоит.
 *
 * Ноль не рисуется вовсе: пустой кружок сообщал бы, что что-то есть.
 */
@Composable
fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    if (count <= 0) return

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
            .background(
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(50),
            )
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = badgeLabel(count),
            color = MaterialTheme.colorScheme.onError,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Подпись значка.
 *
 * Больше девяноста девяти запросов на подписку не бывает, но если вдруг —
 * трёхзначное число растянет кружок в овал поверх соседней вкладки, поэтому
 * счёт обрезается.
 */
fun badgeLabel(count: Int): String = if (count > 99) "99+" else count.toString()

package com.ato.sonic_ui.wishlist

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ato.ui_state.base.text.UiSimpleText
import org.jetbrains.compose.resources.stringResource

/**
 * Счётчики доски одной строкой: «5 желаний · 2 выполнено · 1 забронировано».
 *
 * Это замена вкладке «О доске», где те же числа лежали таблицей. Точка вместо
 * запятой — потому что части независимы: это не перечисление одного ряда, а
 * три разных счётчика, и запятая читалась бы как «5 желаний, 2 выполнено» с
 * подчинением второго первому.
 *
 * Пустой список ничего не рисует: у доски без желаний вместо «0 желаний»
 * уместнее пустая строка — про пустоту скажет сам список.
 */
@Composable
fun BoardSummary(
    parts: List<UiSimpleText>,
    modifier: Modifier = Modifier,
) {
    if (parts.isEmpty()) return

    // `map` — inline, поэтому `stringResource` внутри него законен; в
    // `joinToString` с лямбдой он бы не собрался.
    val resolved = parts.map { part ->
        val args = part.formatArgs
        if (args == null) stringResource(part.text) else stringResource(part.text, args)
    }

    Text(
        text = resolved.joinToString(SEPARATOR),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

private const val SEPARATOR = " · "

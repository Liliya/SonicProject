package com.ato.sonic_ui.base.text

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.ato.ui_state.base.text.UiSimpleText
import org.jetbrains.compose.resources.stringResource

@Composable
fun DisplayText(
    state: UiSimpleText,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    /**
     * Сколько строк разрешено занять. По умолчанию — сколько угодно, как было
     * до появления параметра: обрезать текст молча нельзя, это решение места
     * вызова.
     *
     * Нужен там, где строка стоит в ряду с чем-то ещё и обязана держать высоту
     * — например название доски в карточке списка, рядом с которым справа
     * бейдж: без ограничения длинное название переносилось на три строки и
     * карточка вырастала выше соседних.
     */
    maxLines: Int = Int.MAX_VALUE,
    /** Что делать с тем, что не влезло. Многоточие имеет смысл при [maxLines]. */
    overflow: TextOverflow = TextOverflow.Clip,
    modifier: Modifier = Modifier
) {
    val title = if (state.formatArgs == null) {
        stringResource(state.text)
    } else {
        stringResource(state.text, state.formatArgs!!)
    }

    Text(
        text = title,
        fontWeight = fontWeight,
        color = color,
        fontSize = fontSize,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier,
        style = style
    )
}

@Composable
fun DisplaySingleLineText(
    state: UiSimpleText,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    modifier: Modifier = Modifier
) {
    val title = if (state.formatArgs == null) {
        stringResource(state.text)
    } else {
        stringResource(state.text, state.formatArgs!!)
    }

    Text(
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontWeight = fontWeight,
        fontSize = fontSize,
        modifier = modifier
    )
}
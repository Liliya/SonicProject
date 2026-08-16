package com.ato.sonic_ui.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ato.sonic_ui.base.badge.CountBadge
import com.ato.sonic_ui.base.badge.badgeLabel
import com.ato.ui_state.base.menu.Section
import com.ato.ui_state.base.menu.UiSections
import org.jetbrains.compose.resources.stringResource

/**
 * Цвета переключателя разделов.
 *
 * Собраны в один объект, а не в девять параметров подряд: у переключателя есть
 * подложка, обводка, выбранная вкладка, два цвета подписи и два состояния
 * счётчика, и список аргументов на месте вызова превращался в стену из
 * `Color(0xFF…)`, в которой не видно, что чему соответствует.
 */
data class SectionsColors(
    val container: Color,
    val border: Color,
    val selected: Color,
    val selectedText: Color,
    val unselectedText: Color,
    val selectedCount: Color,
    val selectedCountText: Color,
    val count: Color,
    val countText: Color,
)

object SectionsDefaults {

    @Composable
    fun colors(
        container: Color = MaterialTheme.colorScheme.surface,
        border: Color = MaterialTheme.colorScheme.outlineVariant,
        selected: Color = MaterialTheme.colorScheme.primaryContainer,
        selectedText: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        unselectedText: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedCount: Color = MaterialTheme.colorScheme.primary,
        selectedCountText: Color = MaterialTheme.colorScheme.onPrimary,
        count: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
        countText: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    ): SectionsColors = SectionsColors(
        container = container,
        border = border,
        selected = selected,
        selectedText = selectedText,
        unselectedText = unselectedText,
        selectedCount = selectedCount,
        selectedCountText = selectedCountText,
        count = count,
        countText = countText,
    )
}

/**
 * Переключатель разделов страницы.
 *
 * Светлая подложка с тонкой обводкой, а не сплошная зелёная капсула: это
 * управление содержимым, а не главное действие экрана, и по акценту оно должно
 * уступать и заголовку, и карточкам под собой. Выбранная вкладка отмечена
 * светло-зелёной заливкой и тёмно-зелёной подписью — этого достаточно, чтобы
 * увидеть, где находишься.
 *
 * Скруглением в 20dp, а не в «капсулу»: капсула читается как кнопка, а тут
 * ничего не нажимается целиком — нажимается половина.
 */
@Composable
fun DisplaySections(
    state: UiSections,
    onSelected: (Section) -> Unit,
    modifier: Modifier = Modifier,
    colors: SectionsColors = SectionsDefaults.colors(),
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = colors.container,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            state.items.forEach { item ->
                // Раздел — строка из подписи, счёта содержимого и, если за ним
                // что-то ждёт ответа, значка. Подпись берёт `weight(fill =
                // false)`, чтобы длинное слово ужималось, а не выталкивало
                // цифры за край.
                Row(
                    modifier = Modifier
                        .weight(1f)
                        // clip → background → clickable, иначе подсветка нажатия
                        // выходит прямоугольником за скруглённую вкладку.
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (item.isSelected) colors.selected else Color.Transparent
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onSelected(item) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.title?.let { stringResource(it) } ?: item.name.orEmpty(),
                        modifier = Modifier.weight(1f, fill = false),
                        color = if (item.isSelected) {
                            colors.selectedText
                        } else {
                            colors.unselectedText
                        },
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )

                    item.count?.let { count ->
                        Spacer(Modifier.width(6.dp))
                        SectionCount(
                            count = count,
                            background = if (item.isSelected) {
                                colors.selectedCount
                            } else {
                                colors.count
                            },
                            contentColor = if (item.isSelected) {
                                colors.selectedCountText
                            } else {
                                colors.countText
                            },
                        )
                    }

                    if (item.badgeCount > 0) {
                        Spacer(Modifier.width(6.dp))
                        CountBadge(count = item.badgeCount)
                    }
                }
            }
        }
    }
}

/**
 * Сколько всего лежит за разделом.
 *
 * Это не [CountBadge]: тот красный и означает «здесь тебя ждут», а здесь
 * просто размер списка — спокойная цифра, которая не должна перетягивать на
 * себя внимание с самого списка. Ноль показывается: «Подписчики 0» — это
 * ответ, а пропавший счётчик выглядит как ещё не загрузившийся.
 */
@Composable
private fun SectionCount(
    count: Int,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .background(color = background, shape = RoundedCornerShape(50))
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = badgeLabel(count),
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun AutoResizeText(
    text: String,
    fontSizeRange: FontSizeRange,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current,
) {
    var fontSizeValue by remember { mutableStateOf(fontSizeRange.max.value) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        color = color,
        maxLines = maxLines,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        style = style,
        fontSize = fontSizeValue.sp,
        onTextLayout = {
            if (it.didOverflowHeight && !readyToDraw) {
                val nextFontSizeValue = fontSizeValue - fontSizeRange.step.value
                if (nextFontSizeValue <= fontSizeRange.min.value) {
                    // Reached minimum, set minimum font size and it's readToDraw
                    fontSizeValue = fontSizeRange.min.value
                    readyToDraw = true
                } else {
                    // Text doesn't fit yet and haven't reached minimum text range, keep decreasing
                    fontSizeValue = nextFontSizeValue
                }
            } else {
                // Text fits before reaching the minimum, it's readyToDraw
                readyToDraw = true
            }
        },
        modifier = modifier.drawWithContent { if (readyToDraw) drawContent() }
    )
}

data class FontSizeRange(
    val min: TextUnit,
    val max: TextUnit,
    val step: TextUnit = DEFAULT_TEXT_STEP,
) {
    init {
        require(min < max) { "min should be less than max, $this" }
        require(step.value > 0) { "step should be greater than 0, $this" }
    }

    companion object {
        private val DEFAULT_TEXT_STEP = 1.sp
    }
}
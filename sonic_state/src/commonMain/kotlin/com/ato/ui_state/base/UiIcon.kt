package com.ato.ui_state.base

import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.DrawableResource

/**
 * Иконка в состоянии экрана.
 *
 * Два способа задать картинку, и оба нужны:
 * - [icon] — готовый [ImageVector], то есть иконка из material-icons или
 *   нарисованная кодом;
 * - [iconRes] — файл-ассет (`composeResources/drawable`). Для иконок, которые
 *   пришли из макета, это единственный честный вариант: перерисовывать чужую
 *   геометрию путями в Kotlin — значит каждый раз получать «почти такую же».
 *
 * Ресурс здесь лежит нераспакованным (как и `NavBarItem.titleRes`) намеренно:
 * состояние собирает домен, а он не composable и в картинку ресурс превратить
 * не может. Это делает слой отрисовки.
 *
 * Новые поля дописаны в конец, а не рядом с [icon], хотя по смыслу им место
 * там: `UiIcon(Icons.Default.Close, removeLabel)` зовут позиционно, и вставка
 * параметра вторым молча меняет смысл этого аргумента.
 */
data class UiIcon(
    val icon: ImageVector? = null,
    val contentDescription: String = "",
    val isLoading: Boolean = false,
    val iconRes: DrawableResource? = null,
    /**
     * Вариант с заливкой для выбранного состояния — как того требует Material 3
     * от нижней навигации: невыбранные вкладки контурные, выбранная залитая.
     * Если варианта нет, в обоих состояниях рисуется [iconRes].
     */
    val selectedIconRes: DrawableResource? = null,
)

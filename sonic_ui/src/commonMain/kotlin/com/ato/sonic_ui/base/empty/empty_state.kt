package com.ato.sonic_ui.base.empty

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val TileShape = RoundedCornerShape(24.dp)

/**
 * Пустое состояние списка.
 *
 * Было три разных приглашения нажать одно и то же: кружок с плюсом, слово
 * «Пусто» и залитая кнопка под ним — а на экране доски рядом ещё и FAB с тем же
 * плюсом. Четыре объекта на одно действие: глазу не за что зацепиться, и ни один
 * из них не выглядит главным.
 *
 * Теперь это одна пунктирная плитка — привычная форма «здесь пока пусто, нажми и
 * заполни». Пунктир отличает её от настоящих карточек списка: она не содержимое,
 * а место под него. Внутри всё то же самое, но в одном порядке чтения: иконка,
 * состояние ([text]), действие ([actionLabel]). Нажимается плитка целиком, так
 * что отдельная кнопка не нужна — и цель получается больше, а не меньше.
 *
 * Без [onAction] плитка остаётся просто сообщением: ни рамки, ни нажатия — не
 * везде есть что предложить.
 */
@Composable
fun EmptyState(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val hasAction = actionLabel != null && onAction != null

    if (!hasAction) {
        EmptyStateBody(
            text = text,
            icon = icon,
            actionLabel = null,
            highlighted = false,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
        )
        return
    }

    val outline = MaterialTheme.colorScheme.outlineVariant

    EmptyStateBody(
        text = text,
        icon = icon,
        actionLabel = actionLabel,
        highlighted = true,
        modifier = modifier
            .fillMaxWidth()
            // Отступы снаружи небольшие: плитку списки и так вставляют в свои
            // поля, а узкая колонка посреди пустого экрана выглядит случайной.
            .padding(horizontal = 8.dp, vertical = 24.dp)
            // Пунктир рисуется вручную: `BorderStroke` умеет только сплошную
            // линию, а сплошная рамка на этом экране читалась бы как ещё одна
            // карточка.
            .drawBehind {
                val stroke = 1.5.dp.toPx()
                drawRoundRect(
                    color = outline,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(size.width - stroke, size.height - stroke),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    style = Stroke(
                        width = stroke,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(8.dp.toPx(), 6.dp.toPx())
                        )
                    )
                )
            }
            .clip(TileShape)
            .clickable(role = Role.Button) { onAction?.invoke() }
            .padding(horizontal = 24.dp, vertical = 32.dp),
    )
}

@Composable
private fun EmptyStateBody(
    text: String,
    icon: ImageVector?,
    actionLabel: String?,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        // Зелёный кружок — только там, где на него можно
                        // нажать. В сообщении без действия он обещал бы
                        // кнопку, которой нет.
                        color = if (highlighted) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    // Иконка здесь декоративная: смысл несёт текст ниже, и
                    // скринридер не должен читать её отдельно.
                    contentDescription = null,
                    tint = if (highlighted) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (actionLabel != null) {
            Spacer(Modifier.height(6.dp))
            // Подпись действия, а не кнопка: кнопка внутри нажимаемой плитки —
            // это две цели друг в друге, и меньшая из них перехватывает нажатие.
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

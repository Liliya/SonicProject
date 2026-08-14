package com.ato.sonic_ui.base.text

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Ссылка, разобранная на то, что человек о ней и хочет знать.
 *
 * Ссылка из поиска Google — это восемьсот символов служебных параметров, и
 * ровно так она и рисовалась: адрес переносился на двадцать строк и занимал
 * экран целиком. Прочитать в нём можно было ровно одно слово — имя сайта, —
 * которое стояло в самом начале и тонуло в остальном.
 *
 * Поэтому здесь имя сайта — заголовок, а от адреса остаётся одна строка с
 * многоточием. Никакой загрузки страницы ради настоящего превью: заголовок и
 * картинку пришлось бы тянуть с чужого сайта на каждое открытие экрана, платя
 * за это временем и трафиком человека, и всё равно ничего не получить с тех
 * сайтов, которые от такого закрываются.
 */
@Composable
fun LinkPreview(
    url: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current
    val parts = remember(url) { LinkParts.of(url) }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.clickable(role = Role.Button) {
            if (onClick != null) {
                onClick()
            } else {
                // Открыть можно не всякую строку: адрес набирает человек, и
                // «ozon ru» без схемы платформа отвергает исключением.
                runCatching { uriHandler.openUri(url) }
            }
        },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            // Первая буква сайта вместо favicon: за значком пришлось бы идти в
            // сеть на каждую ссылку, а буква на подложке узнаётся не хуже и
            // рисуется мгновенно.
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = parts.initial,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = parts.host,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // Хвост показывается, только если он есть: у ссылки на главную
                // страницу вторая строка была бы пустой, а место занимала.
                parts.tail?.let { tail ->
                    Text(
                        text = tail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * Разбор адреса на имя сайта и хвост.
 *
 * Руками, а не готовым разборщиком URL: в общем коде такого нет, а тянуть ради
 * трёх строк зависимость на все платформы — дороже самой задачи. Разбор нарочно
 * снисходительный: сюда попадает то, что человек набрал руками, и «ozon.ru» без
 * схемы должно читаться так же, как «https://ozon.ru/».
 */
internal data class LinkParts(
    val host: String,
    val tail: String?,
) {
    /** Первая буква имени сайта, заглавная. Пустой ссылки здесь не бывает. */
    val initial: String
        get() = host.take(1).uppercase().ifEmpty { "?" }

    companion object {
        fun of(url: String): LinkParts {
            val trimmed = url.trim()
            val withoutScheme = trimmed
                .substringAfter("://", trimmed)
                .removePrefix("www.")

            val host = withoutScheme.substringBefore('/').substringBefore('?')
            val tail = withoutScheme.removePrefix(host).takeIf { it.isNotBlank() && it != "/" }

            return LinkParts(
                // Ссылка может быть и не ссылкой вовсе — человек набирает что
                // угодно. Тогда заголовком становится то, что набрано.
                host = host.ifBlank { trimmed },
                tail = tail,
            )
        }
    }
}

package com.ato.sonic_ui.base.text

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ato.helpers.links.CheckedLink
import com.ato.helpers.links.LinkRefusal
import com.ato.helpers.links.LinkSafety
import com.ato.helpers.links.LinkWarning
import com.ato.sonic_ui.dialog.CustomDialog
import com.ato.ui_state.base.link.UiLinkGuard
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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
 * сайтов, которые от такого закрываются. Это же и ответ на вопрос «а не
 * навредит ли предпросмотр»: предпросмотр никуда не ходит, он разбирает
 * строку.
 *
 * ЗАГОЛОВОК — ЭТО ОБЕЩАНИЕ
 * ------------------------
 * Раз имя сайта показано крупно и первым, по нему и решают, нажимать ли. Значит
 * оно обязано быть настоящим, и разбор [LinkSafety] нужен здесь именно за этим:
 * у `https://ozon.ru@evil.example` заголовком станет `evil.example`, а не
 * `ozon.ru`, а невидимые символы, которыми строку разворачивают задом наперёд,
 * до экрана не доедут вовсе.
 *
 * Дальше три исхода, и они разные:
 *
 *   обычная ссылка   открывается по нажатию, как и раньше;
 *   подозрительная   сначала показывает адрес целиком и спрашивает;
 *   не ссылка        не нажимается совсем — `javascript:` и `intent:` не
 *                    открывают страницу, они запускают что-то другое.
 */
@Composable
fun LinkPreview(
    url: String,
    guard: UiLinkGuard,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current
    val link = remember(url) { LinkSafety.check(url) }
    var confirming by remember(url) { mutableStateOf(false) }

    // Открывать нечего — значит и нажимать не на что. Кнопка, которая по
    // нажатию честно ничего не делает, выглядит сломанной; строка, которая не
    // выглядит кнопкой, ничего не обещает.
    val isClickable = onClick != null || link.isOpenable

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = if (isClickable) {
            modifier.clickable(role = Role.Button) {
                when {
                    onClick != null -> onClick()
                    link.asksFirst -> confirming = true
                    // Открыть можно не всякую строку: адрес набирает человек, и
                    // платформа отвергает исключением то, что ей не нравится.
                    else -> link.url?.let { runCatching { uriHandler.openUri(it) } }
                }
            }
        } else {
            modifier
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
                color = if (link.isOpenable) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                },
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = link.initial,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (link.isOpenable) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = link.host,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // Хвост показывается, только если он есть: у ссылки на главную
                // страницу вторая строка была бы пустой, а место занимала.
                link.tail?.let { tail ->
                    Text(
                        text = tail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                // Предупреждение — под адресом, а не вместо него: человек всё
                // ещё должен видеть, о какой ссылке речь.
                link.noteFor(guard)?.let { note ->
                    Text(
                        text = stringResource(note),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (link.isCalm) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                    )
                }
            }
        }
    }

    ConfirmOpening(
        show = confirming,
        link = link,
        guard = guard,
        onDismiss = { confirming = false },
        onConfirm = {
            confirming = false
            link.url?.let { runCatching { uriHandler.openUri(it) } }
        },
    )
}

/**
 * Вопрос перед выходом из приложения — и адрес целиком в нём.
 *
 * Показывается не на каждую ссылку, а только на ту, чьё имя может значить не
 * то, чем выглядит (см. [LinkWarning.asksFirst]). Вопрос на каждую ссылку — это
 * вопрос, который через неделю нажимают не читая, и тогда он не защищает уже
 * ни от чего.
 *
 * Адрес здесь развёрнут целиком, в четыре строки: это единственное место, где
 * человеку показывают ровно то, что откроется, вместе с той частью, которую
 * карточка сокращает.
 */
@Composable
private fun ConfirmOpening(
    show: Boolean,
    link: CheckedLink,
    guard: UiLinkGuard,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    CustomDialog(show = show, onDismiss = { onDismiss() }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(guard.confirmTitle),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            link.noteFor(guard)?.let { note ->
                Text(
                    text = stringResource(note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            ) {
                Text(
                    text = link.url.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(8.dp),
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(guard.confirmCancel))
                }
                Button(onClick = onConfirm) {
                    Text(text = stringResource(guard.confirmOpen))
                }
            }
        }
    }
}

/**
 * Одна строчка про ссылку — или ничего, если сказать нечего.
 *
 * Причина одна, даже когда их несколько: `http://оzon.ru@evil.example` плох
 * трижды, но три строки подряд читаются хуже одной. Сверху — то, что дороже
 * стоит.
 */
private fun CheckedLink.noteFor(guard: UiLinkGuard): StringResource? = when {
    refusal == LinkRefusal.ForeignScheme -> guard.foreignScheme
    refusal != null -> guard.notALink
    LinkWarning.HiddenPrefix in warnings -> guard.hiddenPrefix
    LinkWarning.DisguisedName in warnings -> guard.disguisedName
    LinkWarning.NotEncrypted in warnings -> guard.notEncrypted
    else -> null
}

/** Замечание, которое ни о чём не предупреждает, — серое, а не красное. */
private val CheckedLink.isCalm: Boolean
    get() = refusal == null && !asksFirst

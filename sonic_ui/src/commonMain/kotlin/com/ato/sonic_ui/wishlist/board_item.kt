package com.ato.sonic_ui.wishlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.base.badge.LabelBadge
import com.ato.sonic_ui.base.card.paperCardBorder
import com.ato.sonic_ui.base.card.paperCardColor
import com.ato.sonic_ui.base.image.BoardPicture
import com.ato.sonic_ui.base.skeleton.SkeletonBlock
import com.ato.sonic_ui.base.text.DisplayText
import com.ato.ui_state.wishlist.UiBoard

/**
 * Обложка доски.
 *
 * Крупнее прежних 44dp: картинка — это то, по чему доску узнают в списке, и
 * отличать «Дом» от «Дня рождения» приходилось по подписи, а не по ней. На
 * 56dp у карточки появляется и внятная высота — раньше она держалась на
 * вертикальных полях, и ряд карточек читался как полосатый фон.
 */
private val COVER_SIZE = 56.dp

/** Зазор между обложкой и текстом. Столько же держит карточка человека. */
private val MEDIA_GAP = 12.dp

/** Поля внутри карточки — как у карточки человека в списках. */
private val ROW_INSET = 12.dp

/** Зазор между пометками в ряду и между пометкой и названием. */
private val BADGE_GAP = 6.dp

/**
 * Строка доски в списке: обложка, название, счётчик и пометки.
 *
 * Оформлена «листом бумаги» ([paperCardColor], [paperCardBorder]) — как
 * карточка человека и карточки на «Подарю». До этого доска была залита
 * `surfaceContainer`, то есть серым, а серый в приложении означает «нажимать
 * нечего»: так залиты список людей, которым видна доска, и блоки-пояснения. По
 * доске же нажимают всегда — это единственный способ её открыть, — и она
 * выглядела единственным нажимаемым элементом, притворяющимся ненажимаемым.
 *
 * Пометки ([UiBoard.mainBadge], [UiBoard.privacy]) стоят пилюлями под
 * счётчиком, а не строкой с иконкой: их две, они об одном и том же — о
 * свойствах доски, — и одинаковая форма говорит это сама. Обе необязательны и
 * приходят только со своих досок, поэтому на странице человека карточка
 * остаётся ровно тем, чем была: обложка, название, счётчик.
 *
 * Кнопка «+» намеренно осталась внутри карточки, рядом с названием: на экране
 * своих досок это самое частое действие, и уносить его в меню — значит менять
 * два касания на три.
 */
@Composable
fun DisplayBoard(
    data: UiBoard,
    onAddClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    addButtonTestTag: String? = null,
    addContentDescription: String? = null,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = paperCardColor()),
        border = paperCardBorder(),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ROW_INSET, vertical = ROW_INSET),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Раньше здесь был эмодзи на прозрачном кружке: без подложки и без
            // кольца он висел в пустоте и читался как «не дорисовали».
            BoardPicture(
                value = data.board?.emoji,
                seed = data.board?.documentId,
                size = COVER_SIZE,
                shape = MaterialTheme.shapes.small,
            )

            Spacer(modifier = Modifier.width(MEDIA_GAP))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Название доски — заголовок, а не абзац текста: `bodyLarge`
                // ставил его вровень со счётчиком под ним, и список читался
                // одинаково серым.
                DisplayText(
                    state = data.boardName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(2.dp))

                DisplayText(
                    state = data.boardWishCount,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                BoardBadges(data)
            }

            if (onAddClicked != null) {
                Spacer(modifier = Modifier.width(MEDIA_GAP))

                CenteredIconCard(
                    elevation = CardDefaults.cardElevation(0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    icon = Icons.Filled.Add,
                    onClick = onAddClicked,
                    contentDescription = addContentDescription,
                    // Было 40dp — ниже минимума в 48dp, из-за чего в том числе
                    // промахивались UI-тесты.
                    modifier = Modifier
                        .height(48.dp)
                        .width(48.dp)
                        .let { if (addButtonTestTag != null) it.testTag(addButtonTestTag) else it }
                )
            }
        }
    }
}

/**
 * Заглушка [DisplayBoard] на время загрузки — той же формы и той же высоты.
 *
 * Общая `SkeletonCard` здесь больше не годится: у неё кружок 48dp и поля 20/16,
 * то есть геометрия прежней карточки доски. Обложка же теперь квадрат со
 * скруглением, и разница в высоте — те самые несколько точек, на которые список
 * дёргается ровно в тот момент, ради которого заглушка и рисуется.
 *
 * Двух строк достаточно, хотя своя доска покажет ещё и ряд пометок: на холодном
 * старте неизвестно, чьи это доски, а высоту карточки задаёт обложка — она выше
 * трёх строк текста.
 */
@Composable
fun BoardCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = paperCardColor()),
        border = paperCardBorder(),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ROW_INSET, vertical = ROW_INSET),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBlock(
                modifier = Modifier.size(COVER_SIZE),
                shape = MaterialTheme.shapes.small,
            )
            Spacer(Modifier.width(MEDIA_GAP))
            Column(modifier = Modifier.weight(1f)) {
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(16.dp)
                )
                Spacer(Modifier.height(8.dp))
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(12.dp)
                )
            }
            Spacer(Modifier.width(MEDIA_GAP))
            SkeletonBlock(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.small,
            )
        }
    }
}

/**
 * Ряд пометок под счётчиком — или ничего, если пометок нет.
 *
 * Отступ сверху рисуется здесь, а не в карточке: пустой ряд оставил бы после
 * счётчика воздух, которому нечего разделять, и карточки чужих досок стали бы
 * на несколько точек выше своих без всякой причины.
 */
@Composable
private fun BoardBadges(data: UiBoard) {
    val main = data.mainBadge
    val privacy = data.privacy

    if (main == null && privacy == null) return

    Spacer(modifier = Modifier.height(BADGE_GAP))

    Row(
        horizontalArrangement = Arrangement.spacedBy(BADGE_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // «Основная» идёт первой: это свойство самой доски, а приватность —
        // настройка, которую владелец меняет. Цветом они не различаются
        // намеренно — см. LabelBadge.
        main?.let { LabelBadge(text = it) }

        // `fill = false` и `weight`: пометка сжимается до многоточия, если
        // перевод не влез, вместо того чтобы вытолкнуть соседнюю за край.
        privacy?.let {
            LabelBadge(
                text = it,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}

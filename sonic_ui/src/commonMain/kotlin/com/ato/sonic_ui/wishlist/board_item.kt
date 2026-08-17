package com.ato.sonic_ui.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
private val COVER_SIZE = 80.dp

/**
 * Скругление обложки — 18dp, вне шкалы `MaterialTheme.shapes`.
 *
 * Шкала даёт 12 (`small`) и 16 (`medium`), и обложка размером 80dp на обоих
 * читается иначе, чем задумано: на 12 угол выглядит почти прямым, на 16 —
 * повторяет угол самой карточки, и картинка перестаёт быть отдельным объектом
 * внутри неё. Значение из макета, поэтому и стоит числом.
 */
private val COVER_RADIUS = 18.dp

/** Зазор между обложкой и текстом. */
private val MEDIA_GAP = 12.dp

/** Поля внутри карточки. */
private val ROW_INSET = 16.dp

/**
 * Высота карточки — нижняя граница, а не точный размер.
 *
 * Обложка с полями даёт ровно её: 80 + 16 + 16 = 112. Но на своей доске под
 * счётчиком стоит ещё ряд пометок, и текстовая колонка выходит выше обложки —
 * такая карточка станет примерно 124dp. Жёсткая высота обрезала бы пометки,
 * поэтому предел минимальный: карточки без пометок держат 112, с пометками
 * растут ровно на то, что в них добавилось.
 */
private val CARD_MIN_HEIGHT = 112.dp

/** Кнопка «плюс» справа. */
private val ADD_BUTTON_SIZE = 64.dp
private val ADD_BUTTON_RADIUS = 20.dp
private val ADD_ICON_SIZE = 26.dp

/** Зазор между пометками в ряду. */
private val BADGE_GAP = 6.dp

/** Зазор между названием и счётчиком. */
private val TITLE_GAP = 4.dp

/**
 * Кегль названия и счётчика — поверх шкалы, значениями из макета.
 *
 * `titleMedium` даёт 16sp, `bodySmall` — 12sp, и на карточке высотой 112dp с
 * обложкой 80dp текст такого размера теряется рядом с картинкой. От стилей
 * берутся семейство и начертание (`titleMedium` уже SemiBold), меняется только
 * размер — вместе с межстрочным: `titleMedium` держит `lineHeight` 24sp, и
 * оставить его при кегле 24sp значило бы прижать строку к самой себе.
 */
private val TITLE_TEXT_SIZE = 24.sp
private val TITLE_TEXT_LINE = 32.sp
private val COUNT_TEXT_SIZE = 17.sp
private val COUNT_TEXT_LINE = 22.sp

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
                .heightIn(min = CARD_MIN_HEIGHT)
                .padding(horizontal = ROW_INSET, vertical = ROW_INSET),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Раньше здесь был эмодзи на прозрачном кружке: без подложки и без
            // кольца он висел в пустоте и читался как «не дорисовали».
            BoardPicture(
                value = data.board?.emoji,
                seed = data.board?.documentId,
                size = COVER_SIZE,
                shape = RoundedCornerShape(COVER_RADIUS),
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
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = TITLE_TEXT_SIZE,
                        lineHeight = TITLE_TEXT_LINE,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(TITLE_GAP))

                DisplayText(
                    state = data.boardWishCount,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = COUNT_TEXT_SIZE,
                        lineHeight = COUNT_TEXT_LINE,
                    ),
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
                    shape = RoundedCornerShape(ADD_BUTTON_RADIUS),
                    iconSize = ADD_ICON_SIZE,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .size(ADD_BUTTON_SIZE)
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
 *
 * @param hasAction рисовать ли справа заглушку кнопки. Пометки на заглушке
 *   можно не угадывать — высота от них не зависит, — а вот кнопка занимает
 *   место, и на чужих досках, где её нет, пустой квадрат обещал бы действие,
 *   которого на карточке не появится.
 */
@Composable
fun BoardCardSkeleton(
    modifier: Modifier = Modifier,
    hasAction: Boolean = true,
) {
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
                .heightIn(min = CARD_MIN_HEIGHT)
                .padding(horizontal = ROW_INSET, vertical = ROW_INSET),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBlock(
                modifier = Modifier.size(COVER_SIZE),
                shape = RoundedCornerShape(COVER_RADIUS),
            )
            Spacer(Modifier.width(MEDIA_GAP))
            Column(modifier = Modifier.weight(1f)) {
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(24.dp)
                )
                Spacer(Modifier.height(TITLE_GAP))
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(17.dp)
                )
            }
            if (hasAction) {
                Spacer(Modifier.width(MEDIA_GAP))
                SkeletonBlock(
                    modifier = Modifier.size(ADD_BUTTON_SIZE),
                    shape = RoundedCornerShape(ADD_BUTTON_RADIUS),
                )
            }
        }
    }
}

/**
 * Карточка «новая доска» — последняя строка списка.
 *
 * До этого действие висело под списком отдельным кружком с подписью: ни на что
 * вокруг не похоже, ни к чему не привязано, и на экране с восемью досками
 * читалось как случайно оставшийся элемент. Теперь это строка того же списка —
 * тот же «лист бумаги», те же поля, плюс на месте обложки, — и список
 * заканчивается тем же, чем состоит.
 *
 * Ниже карточек с досками: 72dp против 112dp. Здесь нет ни счётчика, ни
 * пометок, и держать полную высоту не на чем — пустая карточка выглядела бы
 * недогруженной, а не просторной.
 */
@Composable
fun AddBoardCard(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = paperCardColor()),
        border = paperCardBorder(),
        elevation = CardDefaults.cardElevation(0.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ADD_CARD_HEIGHT)
                .padding(horizontal = ROW_INSET),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Плюс стоит на месте обложки и того же скругления, что кнопка «+»
            // на карточке доски: два действия «добавить» на одном экране должны
            // выглядеть одним и тем же действием.
            Box(
                modifier = Modifier
                    .size(ADD_CARD_ICON_BOX)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(ADD_BUTTON_RADIUS),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    // Подпись рядом уже всё сказала — значок для скринридера
                    // повторил бы её вторым голосом.
                    contentDescription = null,
                    modifier = Modifier.size(ADD_ICON_SIZE),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            Spacer(Modifier.width(MEDIA_GAP))

            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = COUNT_TEXT_SIZE,
                    lineHeight = COUNT_TEXT_LINE,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Высота карточки «новая доска». */
private val ADD_CARD_HEIGHT = 72.dp

/** Квадрат с плюсом внутри неё — по высоте карточки минус поля. */
private val ADD_CARD_ICON_BOX = 48.dp

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

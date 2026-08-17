package com.ato.sonic_ui.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
 * Крупнее аватарки человека (32dp) — картинка сложнее лица и на кружке в
 * тридцать две точки от неё остаётся пятно, — но той же сетки: список досок и
 * список друзей должны читаться как один экран, набранный одной рукой.
 *
 * До этого здесь было 80dp по макету, и карточка занимала шестую часть экрана:
 * на телефон влезало четыре доски.
 */
private val COVER_SIZE = 48.dp

/** Зазор между обложкой и текстом. Столько же держит карточка человека. */
private val MEDIA_GAP = 12.dp

/** Поля внутри карточки по горизонтали — как у карточки человека. */
private val ROW_INSET = 12.dp

/**
 * Поля по вертикали. Меньше горизонтальных: высоту здесь задаёт обложка, а не
 * они, и лишние точки сверху и снизу только отодвигают соседние карточки.
 */
private val ROW_VERTICAL_INSET = 10.dp

/** Кнопка «плюс» справа. 48dp — минимальная цель для пальца, ниже нельзя. */
private val ADD_BUTTON_SIZE = 48.dp

/** Зазор между названием и пометкой «основная» справа от него. */
private val BADGE_GAP = 6.dp

/** Зазор между строками в текстовой колонке. */
private val LINE_GAP = 2.dp

/**
 * Строка доски в списке: обложка, название, счётчик и кому доска видна.
 *
 * Оформлена «листом бумаги» ([paperCardColor], [paperCardBorder]) — как
 * карточка человека и карточки на «Подарю». До этого доска была залита
 * `surfaceContainer`, то есть серым, а серый в приложении означает «нажимать
 * нечего»: так залиты список людей, которым видна доска, и блоки-пояснения. По
 * доске же нажимают всегда — это единственный способ её открыть, — и она
 * выглядела единственным нажимаемым элементом, притворяющимся ненажимаемым.
 *
 * Размеры — по карточке человека: та же сетка, те же поля, тот же кегль. Доска
 * несёт на строку больше, поэтому карточка выходит выше, но не в полтора раза,
 * как было по макету, — там обложка 80dp занимала шестую часть экрана.
 *
 * Две пометки, и нарочно разные на вид. [UiBoard.mainBadge] — пилюля справа от
 * названия: это ярлык, приклеенный к доске, и стоит он там же, где ярлыки
 * ставят. [UiBoard.privacy] — обычная строка под счётчиком: видимость это не
 * ярлык, а свойство со значением, и в пилюле оно читалось как тег «Всем», из
 * которого не понять, всем чего. Строка «Видно всем» говорит это словами.
 *
 * Обе необязательны и приходят только со своих досок, поэтому на странице
 * человека карточка остаётся обложкой, названием и счётчиком.
 *
 * Кнопка «+» намеренно осталась внутри карточки: на экране своих досок это
 * самое частое действие, и уносить его в меню — значит менять два касания на
 * три.
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
                .padding(horizontal = ROW_INSET, vertical = ROW_VERTICAL_INSET),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Название доски — заголовок, а не абзац текста:
                    // `bodyLarge` ставил его вровень со счётчиком под ним, и
                    // список читался одинаково серым.
                    //
                    // `fill = false`: название занимает столько, сколько ему
                    // нужно, и пилюля стоит сразу за ним. С `fill = true` она
                    // уезжала к правому краю, и у коротких названий между ними
                    // зияла дырка.
                    DisplayText(
                        state = data.boardName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )

                    data.mainBadge?.let {
                        Spacer(modifier = Modifier.width(BADGE_GAP))
                        LabelBadge(text = it)
                    }
                }

                Spacer(modifier = Modifier.height(LINE_GAP))

                DisplayText(
                    state = data.boardWishCount,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Видимость — строкой, не пилюлей: см. описание выше.
                data.privacy?.let {
                    Spacer(modifier = Modifier.height(LINE_GAP))
                    DisplayText(
                        state = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
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
 * Общая `SkeletonCard` здесь не годится: у неё кружок и поля 20/16, то есть
 * своя геометрия. Обложка доски — квадрат со скруглением, и разница в высоте это
 * те самые несколько точек, на которые список дёргается ровно в тот момент,
 * ради которого заглушка и рисуется.
 *
 * @param own заглушка своей доски или чужой. Один флаг на два отличия, потому
 *   что оба следуют из одного: на своей доске есть кнопка «плюс» и строка
 *   видимости, на чужой — ни того, ни другого. Кнопка занимает место по
 *   горизонтали, строка — по вертикали, и без флага заглушка своих досок
 *   оказывалась на двенадцать точек ниже них, а на чужих обещала кнопку,
 *   которой не появится.
 */
@Composable
fun BoardCardSkeleton(
    modifier: Modifier = Modifier,
    own: Boolean = true,
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
                .padding(horizontal = ROW_INSET, vertical = ROW_VERTICAL_INSET),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBlock(
                modifier = Modifier.size(COVER_SIZE),
                shape = MaterialTheme.shapes.small,
            )
            Spacer(Modifier.width(MEDIA_GAP))
            Column(modifier = Modifier.weight(1f)) {
                // Высоты блоков — межстрочные интервалы тех же стилей, какими
                // набраны настоящие строки: 24 у titleMedium, 16 у bodySmall.
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(24.dp)
                )
                Spacer(Modifier.height(LINE_GAP))
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(16.dp)
                )
                if (own) {
                    Spacer(Modifier.height(LINE_GAP))
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .height(16.dp)
                    )
                }
            }
            if (own) {
                Spacer(Modifier.width(MEDIA_GAP))
                SkeletonBlock(
                    modifier = Modifier.size(ADD_BUTTON_SIZE),
                    shape = MaterialTheme.shapes.medium,
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
 * заканчивается тем же, из чего состоит.
 *
 * Ниже карточек с досками: здесь нет ни счётчика, ни видимости, и держать их
 * высоту не на чем — пустая карточка выглядела бы недогруженной, а не
 * просторной.
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
                .padding(horizontal = ROW_INSET, vertical = ROW_VERTICAL_INSET),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Плюс стоит на месте обложки и той же формы, что кнопка «+» на
            // карточке доски: два действия «добавить» на одном экране должны
            // выглядеть одним и тем же действием.
            Box(
                modifier = Modifier
                    .size(ADD_BUTTON_SIZE)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    // Подпись рядом уже всё сказала — значок для скринридера
                    // повторил бы её вторым голосом.
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.width(MEDIA_GAP))

            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

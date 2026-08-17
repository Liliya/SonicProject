package com.ato.sonic_ui.base.image

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ato.ui_state.base.image.UiImageGallery

/**
 * Картинки желания при правке: крупный кадр с перелистыванием.
 *
 * Раньше здесь был ряд миниатюр по 96dp. Ряд честно говорил, что картинок может
 * быть несколько, и показывал, сколько ещё влезет, — но разглядеть на нём
 * что-либо было нельзя, а именно за этим на них при правке и смотрят: та ли
 * фотография и не пора ли её заменить. До ряда была одна картинка квадратом на
 * 256dp, то есть место под ровно одну штуку.
 *
 * Теперь кадр во всю ширину, ровно тот же и того же соотношения, что на экране
 * просмотра ([WishGallery]): человек правит желание и видит его таким, каким
 * увидит тот, кому он его покажет. До этого два экрана кадрировали одну и ту же
 * фотографию по-разному.
 *
 * Что было в ряду ценного — понимание, сколько картинок всего, — дают точки, те
 * же самые, что в просмотре. Чего ряд лишился — вида на все три разом; за это и
 * платим крупным кадром, и размен того стоит: картинку удаляют по одной и глядя
 * на неё, а не выбирая из трёх.
 *
 * @param addContentDescription подпись кнопки «добавить» для скринридера. Она
 *   же и единственное, что о кнопке говорит: текста на ней нет, потому что
 *   строки живут в ресурсах приложения, а не библиотеки.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageGalleryEditor(
    state: UiImageGallery,
    onAddClicked: () -> Unit,
    onRemoveClicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    addContentDescription: String? = null,
    removeContentDescription: String? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
) {
    Column(modifier = modifier.padding(contentPadding)) {
        if (state.isEmpty) {
            // Пусто — вся площадь будущего кадра и есть кнопка «добавить».
            // Маленькая плитка на её месте оставляла бы экран с дыркой, по
            // которой не понять, что здесь будет фотография и какого размера.
            AddArea(
                shape = shape,
                contentDescription = addContentDescription,
                onClick = onAddClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(GALLERY_RATIO),
            )
            return@Column
        }

        val pager = rememberPagerState(pageCount = { state.items.size })

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(GALLERY_RATIO)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    shape = shape,
                ),
        ) {
            HorizontalPager(
                state = pager,
                modifier = Modifier.fillMaxSize(),
                // Ключ по содержимому слота: картинку удаляют из середины, и
                // без ключа страница показала бы соседнюю.
                key = { page -> state.items[page].url ?: "file-$page" },
            ) { page ->
                val slot = state.items[page]
                WishPhoto(
                    url = slot.url,
                    file = slot.file,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Крестик удаляет ту картинку, которая сейчас на экране. Раньше он
            // висел у каждой миниатюры, наполовину вынесенный за её угол; на
            // крупном кадре ему хватает места внутри.
            OverlayButton(
                icon = Icons.Filled.Close,
                contentDescription = removeContentDescription,
                onClick = { onRemoveClicked(pager.currentPage) },
                modifier = Modifier.align(Alignment.TopEnd),
            )

            if (state.items.size > 1) {
                PageDots(
                    count = state.items.size,
                    current = pager.currentPage,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = DOTS_BOTTOM_INSET),
                )
            }
        }

        // Кнопка «добавить» пропадает на последней картинке — так же, как
        // пропадала плитка в конце ряда.
        if (state.canAddMore) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                AddArea(
                    shape = shape,
                    contentDescription = addContentDescription,
                    onClick = onAddClicked,
                    modifier = Modifier.size(ADD_TILE_SIZE),
                )
            }
        }
    }
}

/** Сторона кнопки «добавить», когда картинки уже есть. */
private val ADD_TILE_SIZE = 56.dp

/**
 * Место под картинку, оно же кнопка «добавить».
 *
 * Рамка обычная, волосяная, а не пунктирная: пунктир означал бы «здесь чего-то
 * не хватает», а здесь всё в порядке — просто сюда можно нажать.
 */
@Composable
private fun AddArea(
    shape: Shape,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                ),
                shape = shape,
            )
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
    }
}

/**
 * Круглая кнопка поверх фотографии.
 *
 * Белое по затемнённому кругу, а не цвета темы: под кнопкой чужая фотография, и
 * `onSurfaceVariant` читается на ней ровно настолько, насколько повезёт с
 * кадром. Затемнение то же, что под точками ([SCRIM_ALPHA]).
 *
 * Размеров два, как и у «плюса» на карточке доски: круг нарисован на 36dp,
 * потому что на кадре 4:3 круг в сорок восемь точек закрывает заметную его
 * часть, — а нажимается всё равно 48dp, минимальная цель для пальца. Тем более
 * что кнопка удаляет.
 */
@Composable
private fun OverlayButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(OVERLAY_TOUCH_SIZE)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(OVERLAY_VISUAL_SIZE)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = SCRIM_ALPHA)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private val OVERLAY_TOUCH_SIZE = 48.dp
private val OVERLAY_VISUAL_SIZE = 36.dp

package com.ato.sonic_ui.base.image

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ato.helpers.getAsyncImageLoader
import com.ato.helpers.getPlatformContext
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage

/**
 * Фотографии желания: одна во всю ширину или несколько с перелистыванием.
 *
 * До этого фотографии были мелкими и в двух разных видах: одна рисовалась
 * квадратом 128dp по центру карточки, а две-три — рядом миниатюр по 160dp,
 * который надо было скроллить пальцем вбок, не понимая, что он скроллится.
 * Фотография вещи — это главное, что человек хочет разглядеть на экране
 * желания, а она занимала шестую часть карточки.
 *
 * Теперь блок один на любое число фотографий, во всю ширину карточки и без
 * полей: карточка сама обрезает содержимое по своему скруглению, поэтому
 * фотография доходит до её краёв и верхние углы получаются скруглёнными.
 *
 * Соотношение 4:3, а не квадрат: во всю ширину телефона квадрат — это триста
 * точек высоты, после которых название желания уезжает под сгиб. Кадр
 * обрезается по центру ([ContentScale.Crop]), потому что фотографии
 * пользовательские и портрет рядом с пейзажем иначе давал бы блоки разной
 * высоты.
 *
 * Перелистывание — `HorizontalPager`: он сам доводит страницу до края и держит
 * состояние. Точки под фотографией нужны именно из-за него: страница занимает
 * всю ширину, соседней не видно, и без точек по одной фотографии не догадаться,
 * что их три. Больше трёх не бывает — [com.ato.ui_state.wishlist.WishlistWish.MAX_IMAGES],
 * — поэтому точки, а не «2 / 12».
 *
 * @param contentDescription что произнесёт скринридер на весь блок. Одна
 *   подпись на все фотографии, а не своя на каждую: он всё равно не покажет их
 *   человеку, и три «фотография желания» подряд — это шум.
 */
// `HorizontalPager` и `rememberPagerState` в этой версии foundation уже
// стабильны, но опт-ин оставлен: он не мешает, а на версии, где они ещё
// экспериментальные, без него не собирается — и `friends_root` со своим pager
// его как раз держит.
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WishGallery(
    images: List<String>,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    if (images.isEmpty()) return

    val label = contentDescription
        ?.let { text -> Modifier.semantics { this.contentDescription = text } }
        ?: Modifier

    Box(
        modifier = modifier
            .aspectRatio(GALLERY_RATIO)
            // Подложка под фотографией, а не сквозная дырка: пока Coil тянет
            // файл, на месте кадра иначе просвечивала бы карточка, и блок
            // появлялся бы рывком вместе с картинкой.
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .then(label),
    ) {
        if (images.size == 1) {
            WishPhoto(url = images.first(), modifier = Modifier.fillMaxSize())
        } else {
            val state = rememberPagerState(pageCount = { images.size })

            HorizontalPager(
                state = state,
                modifier = Modifier.fillMaxSize(),
                // Ключ по адресу: у желания фотографию можно удалить из
                // середины, и без ключа страница показала бы соседний кадр.
                key = { page -> images[page] },
            ) { page ->
                WishPhoto(url = images[page], modifier = Modifier.fillMaxSize())
            }

            PageDots(
                count = images.size,
                current = state.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = DOTS_BOTTOM_INSET),
            )
        }
    }
}

/** Соотношение сторон блока с фотографиями. */
private const val GALLERY_RATIO = 4f / 3f

private val DOTS_BOTTOM_INSET = 12.dp

/** Одна фотография, обрезанная по центру под размер блока. */
@Composable
private fun WishPhoto(url: String, modifier: Modifier = Modifier) {
    CoilImage(
        imageLoader = { getAsyncImageLoader(getPlatformContext()) },
        imageModel = { url },
        imageOptions = ImageOptions(
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
        ),
        modifier = modifier,
    )
}

/**
 * Точки под фотографиями: какая страница из скольких.
 *
 * Лежат на затемнённой плашке, а не прямо на кадре. Фотографии
 * пользовательские, и белые точки на светлом снегу пропадают ровно так же, как
 * тёмные на ночном кадре; плашка даёт им собственный фон, который не зависит от
 * того, что под ним.
 *
 * Активная точка не только светлее, но и шире — вытянута в короткую полоску.
 * Одной яркости мало: на плашке шириной в тридцать точек разницу в прозрачности
 * между тремя кружками надо искать, а разницу в форме видно сразу. Ширина
 * анимирована, поэтому при свайпе полоска переезжает, а не перескакивает.
 */
@Composable
private fun PageDots(
    count: Int,
    current: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(Color.Black.copy(alpha = SCRIM_ALPHA))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(DOT_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val active = index == current
            val width by animateDpAsState(
                targetValue = if (active) DOT_ACTIVE_WIDTH else DOT_SIZE,
                label = "dot_width",
            )

            Box(
                modifier = Modifier
                    .size(width = width, height = DOT_SIZE)
                    .clip(CircleShape)
                    .background(
                        Color.White.copy(alpha = if (active) 1f else DOT_IDLE_ALPHA)
                    ),
            )
        }
    }
}

private val DOT_SIZE = 6.dp
private val DOT_ACTIVE_WIDTH = 18.dp
private val DOT_GAP = 5.dp
private const val DOT_IDLE_ALPHA = 0.45f
private const val SCRIM_ALPHA = 0.32f

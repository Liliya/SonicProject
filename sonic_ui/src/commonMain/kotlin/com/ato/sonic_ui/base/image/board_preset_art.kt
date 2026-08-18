package com.ato.sonic_ui.base.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import com.ato.ui_state.base.image.BoardPresets

/**
 * Как выглядят двенадцать встроенных картинок досок из [BoardPresets]:
 * готовый значок на бледной подложке.
 *
 * Значки раньше чертились здесь же — шесть предметов путями по `Canvas`, полтораста
 * строк арифметики вида `Offset(side * 0.44f, side * 0.24f)`. Ровно этот разговор
 * уже был у заглушки желания (`wish_placeholder.kt`), и кончился он тем же:
 * правка формы означала правку кода, увидеть результат можно было только собрав
 * приложение, а нарисованное руками рядом с настоящими иконками видно сразу —
 * толщина линий своя, оптический размер свой, скругления свои.
 *
 * Теперь это `material-icons-core`, тот же набор, из которого взяты все прочие
 * значки приложения. Их шесть на двенадцать палитр: один и тот же значок на
 * разных подложках различается с одного взгляда, а двенадцать разных силуэтов в
 * одном списке читаются как свалка.
 *
 * Набор в `material-icons-core` небольшой — сорок девять значков, — и подарка
 * среди них нет, хотя для этого приложения он был бы первым. Расширенный набор
 * (`material-icons-extended`) не подключён и подключён не будет: Google его
 * закрыл, о чём написано прямо в `libs.versions.toml`. Поэтому шесть тем — те,
 * что в наборе есть и про подарки говорят: дом, любимое, избранное, покупки,
 * даты, места.
 *
 * Подложка бледная, значок насыщенный того же оттенка — правило то же, что у
 * монограмм в `monogram.kt`, и тема здесь важна: шесть светлых квадратов на
 * тёмном экране светились бы фонариками.
 */

private class BoardPresetArt(
    val background: Color,
    /** Цвет значка: тот же оттенок, что подложка, но насыщенный. */
    val ink: Color,
    val icon: ImageVector,
)

/**
 * Двенадцать палитр на шесть значков.
 *
 * Порядок не случаен: значки идут по кругу, поэтому пары с одним силуэтом —
 * это 0 и 6, 1 и 7 и так далее. Их оттенки нарочно разведены далеко (терракота
 * и небо, роза и мята), иначе два дома в одном списке пришлось бы различать по
 * полутону.
 *
 * Первые шесть тонов — те же, что у монограмм, остальные шесть добавлены в том
 * же регистре. Измеренный контраст значка к подложке: от 7.8:1 в светлой теме
 * и от 8.3:1 в тёмной. Для сплошной фигуры хватило бы и 3:1, но у значков есть
 * тонкие места — ножка звезды, ручка корзины, — и на трёх единицах они
 * пропадают первыми.
 */
private val LightArt: List<BoardPresetArt> = listOf(
    BoardPresetArt(Color(0xFFEDD7CB), Color(0xFF5F2C16), Icons.Filled.Home),          // терракота
    BoardPresetArt(Color(0xFFEDD4D8), Color(0xFF5F2B37), Icons.Filled.Favorite),      // пыльная роза
    BoardPresetArt(Color(0xFFD6E3D7), Color(0xFF2A4531), Icons.Filled.Star),          // шалфей
    BoardPresetArt(Color(0xFFDCD8EC), Color(0xFF362D59), Icons.Filled.ShoppingCart),  // лаванда
    BoardPresetArt(Color(0xFFDBDBDD), Color(0xFF303035), Icons.Filled.DateRange),     // графит
    BoardPresetArt(Color(0xFFEBDFCB), Color(0xFF4C3A1F), Icons.Filled.Place),         // тёплый беж
    BoardPresetArt(Color(0xFFD5E1F0), Color(0xFF26405F), Icons.Filled.Home),          // небо
    BoardPresetArt(Color(0xFFD0E6E1), Color(0xFF204742), Icons.Filled.Favorite),      // мята
    BoardPresetArt(Color(0xFFE6D6E8), Color(0xFF4B2D52), Icons.Filled.Star),          // слива
    BoardPresetArt(Color(0xFFE0E4CC), Color(0xFF3C4522), Icons.Filled.ShoppingCart),  // олива
    BoardPresetArt(Color(0xFFE4D9D1), Color(0xFF4B3A2F), Icons.Filled.DateRange),     // какао
    BoardPresetArt(Color(0xFFD6E2E6), Color(0xFF27414A), Icons.Filled.Place),         // лёд
)

/**
 * То же самое для тёмной темы, вывернутое наизнанку: подложка приглушена почти
 * до фона экрана, значок — светлый.
 */
private val DarkArt: List<BoardPresetArt> = listOf(
    BoardPresetArt(Color(0xFF4A3229), Color(0xFFF7D2BF), Icons.Filled.Home),
    BoardPresetArt(Color(0xFF4A3034), Color(0xFFF7CFD6), Icons.Filled.Favorite),
    BoardPresetArt(Color(0xFF2C3B2F), Color(0xFFCBE3CE), Icons.Filled.Star),
    BoardPresetArt(Color(0xFF343048), Color(0xFFD8D1F5), Icons.Filled.ShoppingCart),
    BoardPresetArt(Color(0xFF35343A), Color(0xFFDBDAE1), Icons.Filled.DateRange),
    BoardPresetArt(Color(0xFF443A2B), Color(0xFFEFDFC1), Icons.Filled.Place),
    BoardPresetArt(Color(0xFF2A3949), Color(0xFFCFE0F5), Icons.Filled.Home),
    BoardPresetArt(Color(0xFF26403D), Color(0xFFC7E4DE), Icons.Filled.Favorite),
    BoardPresetArt(Color(0xFF3E2F44), Color(0xFFEBD3EF), Icons.Filled.Star),
    BoardPresetArt(Color(0xFF383D2B), Color(0xFFDDE3C4), Icons.Filled.ShoppingCart),
    BoardPresetArt(Color(0xFF3E332C), Color(0xFFEBDBD0), Icons.Filled.DateRange),
    BoardPresetArt(Color(0xFF2B3B40), Color(0xFFCFE1E7), Icons.Filled.Place),
)

/** Набор по теме — ровно как `monogramPalette()` у монограмм. */
@Composable
private fun boardArt(): List<BoardPresetArt> =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) LightArt else DarkArt

/**
 * Какую долю плитки занимает значок.
 *
 * Столько же занимали нарисованные предметы, и менять это при переходе на
 * готовые значки не пришлось: у material-иконок внутри своего квадрата уже есть
 * поле, поэтому на глаз они выходят чуть мельче прежних — ровно настолько,
 * насколько плитка стала спокойнее.
 */
private const val GLYPH_FRACTION = 0.46f

/**
 * Встроенная картинка доски номер [index].
 *
 * Номер приходит из [BoardPresets], который сам держит его в диапазоне;
 * остаток здесь — на случай, если кто-то передаст своё число мимо него.
 */
@Composable
fun BoardPresetImage(
    index: Int,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
) {
    val palette = boardArt()
    val art = palette[((index % palette.size) + palette.size) % palette.size]

    Box(
        // Заливка ровная, без градиента. Градиент был при глубоких подложках и
        // достался им от прежних двухстоповых кружков; на бледном тоне он не
        // виден вовсе, а два близких цвета вместо одного — это два места, где
        // палитра может разойтись.
        modifier = modifier
            .clip(shape)
            .background(art.background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = art.icon,
            // Картинка доски декоративная: рядом всегда стоит её название, и
            // скринридер прочитал бы «звезда» вторым голосом к нему.
            contentDescription = null,
            tint = art.ink,
            modifier = Modifier.fillMaxSize(GLYPH_FRACTION),
        )
    }
}

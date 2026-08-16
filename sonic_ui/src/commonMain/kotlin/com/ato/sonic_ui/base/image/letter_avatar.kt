package com.ato.sonic_ui.base.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.ato.ui_state.base.image.AvatarPresets

/**
 * Аватарка из первой буквы имени.
 *
 * Заглушка для тех, кто ничего не выбирал. Раньше на этом месте был рисунок из
 * [AvatarPresetImage] — подарок, звезда, шарик, — и список из четырёх человек
 * выглядел как набор наклеек: картинки красивые, но про человека не говорят
 * ничего, и двух знакомых в списке приходилось различать по подписи под
 * кружком. Буква говорит: имя начинается с неё.
 *
 * Набрана акцентной гарнитурой темы — той же, которой набран заголовок экрана.
 * Пастельная подложка и глубокая буква одного тона: контраст держится, а
 * кружок не спорит с фотографиями соседей.
 */

/** Пастельная подложка и буква на ней. Восемь пар — по количеству оттенков темы. */
private data class LetterAvatarPalette(
    val background: Color,
    val letter: Color,
)

/**
 * Восемь пар, а не десять как у [AvatarPresets]: пресеты различаются рисунком,
 * а здесь единственное отличие — оттенок, и близкие тона в одном списке
 * читались бы как один цвет.
 */
private val LETTER_AVATAR_PALETTES: List<LetterAvatarPalette> = listOf(
    LetterAvatarPalette(Color(0xFFF6D9DF), Color(0xFF8E3A50)), // розовый
    LetterAvatarPalette(Color(0xFFDDD9F2), Color(0xFF473A8E)), // лиловый
    LetterAvatarPalette(Color(0xFFCFE7DA), Color(0xFF1F5E42)), // мятный
    LetterAvatarPalette(Color(0xFFFAE2C8), Color(0xFF8A4E1B)), // персиковый
    LetterAvatarPalette(Color(0xFFD5E4F4), Color(0xFF25507F)), // небесный
    LetterAvatarPalette(Color(0xFFF3E3B8), Color(0xFF7A5B14)), // песочный
    LetterAvatarPalette(Color(0xFFE6DCCF), Color(0xFF5C4632)), // кофейный
    LetterAvatarPalette(Color(0xFFD8E6E8), Color(0xFF23575E)), // морской
)

/**
 * Первая буква для аватарки, или `null` если буквы нет.
 *
 * `@` отбрасывается: в списках сюда попадает и ник, а «@» — это разметка, а не
 * имя, и все ники начинались бы с одного знака. Цифры и знаки препинания
 * оставлены как есть: ник «7sim» начинается с семёрки, и семёрка на кружке
 * честнее пустоты.
 */
fun avatarInitial(text: String?): String? {
    val letter = text?.trimStart { it.isWhitespace() || it == '@' }?.firstOrNull() ?: return null
    return letter.toString().uppercase()
}

/**
 * @param seed по нему выбирается оттенок. Тот же идентификатор, что и у
 *   пресетов, поэтому у человека кружок одного цвета на всех экранах — даже
 *   там, где имя показано, а ник нет.
 * @param size сторона кружка в dp. Нужна здесь, потому что буква обязана расти
 *   вместе с ним: одна и та же `fontSize` смотрелась бы точкой в списке и
 *   маркой на профиле.
 */
@Composable
fun LetterAvatarImage(
    letter: String,
    seed: String?,
    size: Float,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
) {
    val palette = LETTER_AVATAR_PALETTES[
        AvatarPresets.paletteIndex(seed, LETTER_AVATAR_PALETTES.size)
    ]

    Box(
        modifier = modifier.background(color = palette.background, shape = shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter,
            color = palette.letter,
            // Акцентная гарнитура темы: её же носят крупные заголовки, и
            // кружок с буквой читается как часть того же набора, а не как
            // случайный элемент из другого приложения.
            fontFamily = MaterialTheme.typography.headlineLarge.fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (size * LETTER_SIZE_FACTOR).sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

/** Доля стороны кружка, которую занимает буква. */
private const val LETTER_SIZE_FACTOR = 0.44f

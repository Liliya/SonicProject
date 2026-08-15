package com.ato.sonic_ui.base.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ato.ui_state.base.image.AvatarPresets

/**
 * Кружок с первой буквой имени — то, что видно у человека без фотографии.
 *
 * До этого здесь была картинка из [AvatarPresets]: звезда, шарик, планета —
 * стабильно выбранные по идентификатору, но не значащие ничего. В списке из
 * пяти человек получалась россыпь наклеек, по которой нельзя было ни узнать
 * знакомого, ни отличить тёзок: предмет не имеет отношения к тому, кто под ним.
 * Буква имеет.
 *
 * Буква набрана акцентной гарнитурой темы — той же, которой набран заголовок
 * экрана. Это единственная причина брать `headlineMedium` вместо `titleMedium`:
 * не размер, а гарнитура, чтобы кружок читался частью того же оформления, а не
 * технической заглушкой.
 *
 * Цвет постоянен для человека: он выбирается [AvatarPresets.bucket] от того же
 * идентификатора, что раньше выбирал пресет. Аватарка не должна перекрашиваться
 * при каждом открытии экрана — человек запоминается в том числе цветом.
 */
@Composable
fun MonogramAvatar(
    /**
     * Имя, из которого берётся буква.
     *
     * Именно имя, а не [seed]: тот у части экранов — идентификатор из
     * Firestore, и буква «C» от `C1j1H9fC…` не сказала бы о человеке ничего.
     * Чем заменить пустое имя — решает вызывающая сторона: там видно, есть ли
     * под рукой ник. Не нашлось ничего — остаётся кружок без буквы; это
     * по-прежнему аватарка, просто молчаливая.
     */
    name: String?,
    /**
     * Что считать «этим человеком» при выборе цвета: идентификатор или ник.
     * Не имя — тёзки должны отличаться, а сменивший имя не должен перекрашиваться.
     */
    seed: String?,
    size: Float,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
) {
    val palette = monogramPalette()
    val tone = palette[AvatarPresets.bucket(seed ?: name, palette.size)]
    val letter = monogramLetter(name)

    Box(
        modifier = modifier
            .size(size.dp)
            .background(color = tone.background, shape = shape),
        contentAlignment = Alignment.Center,
    ) {
        letter?.let {
            Text(
                text = it.toString(),
                style = MaterialTheme.typography.headlineMedium,
                // Кегль от размера кружка, а не из шкалы: тот же компонент
                // рисует и аватарку 32dp в строке списка, и 96dp в профиле.
                fontSize = (size * 0.42f).sp,
                lineHeight = (size * 0.42f).sp,
                color = tone.ink,
            )
        }
    }
}

/**
 * Первая буква имени, или `null` если брать нечего.
 *
 * Пробелы и знаки препинания пропускаются: у «@nick» первая буква — «n», а не
 * собачка, а у имени с пробелом впереди — сама буква.
 */
private fun monogramLetter(source: String?): Char? =
    source?.firstOrNull { it.isLetter() || it.isDigit() }?.uppercaseChar()

/** Фон кружка и цвет буквы на нём. */
private data class MonogramTone(val background: Color, val ink: Color)

/**
 * Шесть спокойных тонов: терракота, пыльная роза, шалфей, лаванда, графит,
 * тёплый беж.
 *
 * Тон бледный, буква — насыщенная того же оттенка: так кружок остаётся фоном
 * для буквы, а не пятном рядом с ней, и контраст держится с запасом (у всех
 * пар — больше 7:1). Насыщенные кружки с белой буквой смотрелись бы ярче
 * фотографий соседей по списку — а фотография человека всегда важнее заглушки.
 */
@Composable
private fun monogramPalette(): List<MonogramTone> =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        LightTones
    } else {
        DarkTones
    }

private val LightTones = listOf(
    MonogramTone(Color(0xFFEFDCD2), Color(0xFF8C4A2F)), // терракота
    MonogramTone(Color(0xFFEFDADD), Color(0xFF8B4A57)), // пыльная роза
    MonogramTone(Color(0xFFDCE7DC), Color(0xFF3F5C46)), // шалфей
    MonogramTone(Color(0xFFE2DEEF), Color(0xFF4E4574)), // лаванда
    MonogramTone(Color(0xFFDFDFE0), Color(0xFF45454A)), // графит
    MonogramTone(Color(0xFFEDE3D3), Color(0xFF6E5836)), // тёплый беж
)

/**
 * То же самое для тёмной темы, вывернутое наизнанку: фон приглушён до почти
 * фона экрана, буква — светлая. Бледные тона светлой темы здесь светились бы
 * шестью фонариками.
 */
private val DarkTones = listOf(
    MonogramTone(Color(0xFF4A3229), Color(0xFFF0C4AE)),
    MonogramTone(Color(0xFF4A3034), Color(0xFFF0C0C8)),
    MonogramTone(Color(0xFF2C3B2F), Color(0xFFBCD7BF)),
    MonogramTone(Color(0xFF343048), Color(0xFFCBC3EC)),
    MonogramTone(Color(0xFF35343A), Color(0xFFCFCED6)),
    MonogramTone(Color(0xFF443A2B), Color(0xFFE4D2B0)),
)

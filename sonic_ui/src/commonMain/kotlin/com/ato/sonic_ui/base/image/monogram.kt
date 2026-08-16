package com.ato.sonic_ui.base.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.paddingFromBaseline
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
        // Не `Center`: по вертикали букву ставит базовая линия, см. ниже.
        contentAlignment = Alignment.TopCenter,
    ) {
        letter?.let {
            Text(
                text = it.toString(),
                style = MaterialTheme.typography.headlineMedium,
                // Кегль от размера кружка, а не из шкалы: тот же компонент
                // рисует и аватарку 32dp в строке списка, и 96dp в профиле.
                fontSize = (size * LETTER_SIZE).sp,
                lineHeight = (size * LETTER_SIZE).sp,
                // Трекинг у шкалы отрицательный — он для строк текста, а не
                // для одной буквы: на ней это просто сдвиг вправо на пол-сотой.
                letterSpacing = 0.sp,
                color = tone.ink,
                modifier = Modifier.paddingFromBaseline(top = (size * BASELINE).dp),
            )
        }
    }
}

/** Кегль буквы от диаметра кружка. */
private const val LETTER_SIZE = 0.42f

/**
 * Где проходит базовая линия буквы, считая от верха кружка.
 *
 * Буква ставится по базовой линии, а не центрированием строки, и вот почему:
 * центрируется при этом **строчный бокс**, а он несимметричен относительно
 * самой буквы. Сверху в нём запас на выносные элементы, снизу — на подстрочные,
 * у заглавной буквы не занятые ничем; плюс Android добавляет своё поле шрифта.
 * Измеренный результат — буква ниже центра кружка на 2.9% диаметра: на 64dp
 * аватарке это почти две точки, и на глаз видно.
 *
 * Здесь центрируется сама буква: заглавная стоит от базовой линии вверх на
 * высоту прописных (у Playfair Display это 0.708 кегля — прочитано из `OS/2`
 * шрифта), значит её середина окажется в центре кружка, если базовую линию
 * опустить на половину этой высоты ниже центра.
 *
 * Число зависит от гарнитуры, а гарнитуру задаёт тема приложения, не эта
 * библиотека. Промах не страшен: у почти всех текстовых шрифтов высота
 * прописных 0.70 ± 0.03 кегля, и на аватарке 32dp такая ошибка — две десятых
 * точки.
 */
private const val CAP_HEIGHT = 0.708f

/**
 * Оптический подъём — на сколько буква поднята выше геометрического центра.
 *
 * Геометрия и глаз здесь расходятся. Буква, поставленная по габаритам ровно
 * (измерено: промах 0.00%), всё равно читается сидящей низко, если её масса
 * внизу: у «А» центр тяжести ниже середины на 5.9% кегля, у «Q» — на 9.9%,
 * у «Д» и «L» — на 5.6% и 4.3%. Обратные примеры есть, но их меньше и они
 * мягче на вид: «V», «У», «Т» тяжелее сверху на те же 5-6%.
 *
 * Одинаковой поправки, которая устроила бы и «А», и «Т», не существует —
 * промеры по всему алфавиту дают медиану около нуля. Поэтому подъём взят
 * небольшой, вполовину типичного перекоса: буквы с тяжёлым низом он вытягивает
 * почти в центр, буквы с тяжёлым верхом уводит вверх на столько же. Это не
 * симметричный размен: приподнятую букву глаз принимает за центрированную, а
 * опущенную — нет; на том же и держится правило оптического центра.
 */
private const val OPTICAL_LIFT = 0.03f

private const val BASELINE = 0.5f + LETTER_SIZE * (CAP_HEIGHT / 2f - OPTICAL_LIFT)

/**
 * Первая буква имени, или `null` если брать нечего.
 *
 * Пробелы и знаки препинания пропускаются: у «@nick» первая буква — «n», а не
 * собачка, а у имени с пробелом впереди — сама буква.
 *
 * Цифра берётся только если букв нет вовсе: у «5star» монограмма — «S». Дело
 * не только в том, что буква говорит больше, — у акцентной гарнитуры цифры
 * старостильные, с выносными элементами, и «5» уезжает из центра кружка вниз
 * на 6% диаметра, потому что стоит не на базовой линии, а под ней.
 */
private fun monogramLetter(source: String?): Char? {
    if (source == null) return null

    val letter = source.firstOrNull { it.isLetter() }
        ?: source.firstOrNull { it.isDigit() }

    return letter?.uppercaseChar()
}

/** Фон кружка и цвет буквы на нём. */
private data class MonogramTone(val background: Color, val ink: Color)

/**
 * Шесть спокойных тонов: терракота, пыльная роза, шалфей, лаванда, графит,
 * тёплый беж.
 *
 * Тон бледный, буква — насыщенная того же оттенка: так кружок остаётся фоном
 * для буквы, а не пятном рядом с ней. Насыщенные кружки с белой буквой
 * смотрелись бы ярче фотографий соседей по списку — а фотография человека
 * всегда важнее заглушки.
 *
 * Буквы темнее, чем были в первом варианте: на розовом и терракотовом фоне
 * измеренный контраст выходил 4.9:1 и 5.1:1 — формально это проходной для
 * мелкого текста минимум, но буква в кружке набрана светлым серифом, у
 * которого тонкие штрихи, и на глаз она таяла. Теперь у всех шести пар от
 * 7.9:1 (светлая тема) и от 8.4:1 (тёмная).
 */
@Composable
private fun monogramPalette(): List<MonogramTone> =
    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
        LightTones
    } else {
        DarkTones
    }

private val LightTones = listOf(
    MonogramTone(Color(0xFFEDD7CB), Color(0xFF5F2C16)), // терракота
    MonogramTone(Color(0xFFEDD4D8), Color(0xFF5F2B37)), // пыльная роза
    MonogramTone(Color(0xFFD6E3D7), Color(0xFF2A4531)), // шалфей
    MonogramTone(Color(0xFFDCD8EC), Color(0xFF362D59)), // лаванда
    MonogramTone(Color(0xFFDBDBDD), Color(0xFF303035)), // графит
    MonogramTone(Color(0xFFEBDFCB), Color(0xFF4C3A1F)), // тёплый беж
)

/**
 * То же самое для тёмной темы, вывернутое наизнанку: фон приглушён до почти
 * фона экрана, буква — светлая. Бледные тона светлой темы здесь светились бы
 * шестью фонариками.
 */
private val DarkTones = listOf(
    MonogramTone(Color(0xFF4A3229), Color(0xFFF7D2BF)),
    MonogramTone(Color(0xFF4A3034), Color(0xFFF7CFD6)),
    MonogramTone(Color(0xFF2C3B2F), Color(0xFFCBE3CE)),
    MonogramTone(Color(0xFF343048), Color(0xFFD8D1F5)),
    MonogramTone(Color(0xFF35343A), Color(0xFFDBDAE1)),
    MonogramTone(Color(0xFF443A2B), Color(0xFFEFDFC1)),
)

package com.ato.ui_state.base.image

/**
 * Встроенные аватарки — десять картинок, которые рисуются кодом.
 *
 * Профиль без фотографии показывал «?» в пустом кружке, а карточка человека в
 * списке не показывала вообще ничего: [com.ato.sonic_ui.wishlist.PersonCard]
 * рисовал аватарку только под `avaUrl != null`. Пресеты закрывают этот случай
 * так, чтобы ничего не пришлось мигрировать: пустой `avaUrl` больше не значит
 * «нечего рисовать», он значит «возьми [fallbackIndex] от идентификатора».
 * Существующие пользователи получают картинку в тот же день, ни одна запись в
 * Firestore при этом не меняется.
 *
 * Осознанный выбор — отдельная история: там пресет попадает в `avaUrl` строкой
 * `avatar://preset/7`. Схема, а не `https`, выбрана намеренно — так значение
 * нельзя спутать со ссылкой в Storage ни в базе, ни в коде: всё, что не
 * разбирается [indexOf], уходит в загрузчик картинок как раньше.
 *
 * Ничего не грузится по сети, поэтому аватарка есть и офлайн, и до того, как
 * Firestore ответит.
 */
object AvatarPresets {

    /** Сколько пресетов нарисовано. Художественная часть — `avatar_preset_art.kt` в sonic_ui. */
    const val COUNT: Int = 10

    private const val PREFIX = "avatar://preset/"

    /** Значение для `avaUrl`, означающее «пользователь выбрал пресет [index]». */
    fun url(index: Int): String = PREFIX + normalize(index)

    /**
     * Номер пресета из `avaUrl`, или `null` если это обычная ссылка на картинку.
     *
     * Номер вне известного диапазона тоже даёт `null`: если когда-нибудь
     * пресетов станет больше, старый клиент нарисует [fallbackIndex] вместо
     * пустого места.
     */
    fun indexOf(avaUrl: String?): Int? {
        val url = avaUrl ?: return null
        if (!url.startsWith(PREFIX)) return null
        val index = url.substring(PREFIX.length).toIntOrNull() ?: return null
        return index.takeIf { it in 0 until COUNT }
    }

    fun isPreset(avaUrl: String?): Boolean = indexOf(avaUrl) != null

    /**
     * Какой пресет показать тому, кто ничего не выбирал.
     *
     * FNV-1a, а не `String.hashCode()`: у одного и того же пользователя картинка
     * должна совпадать на Android, на iOS и в тестах, а гарантий на совпадение
     * `hashCode` между платформами Kotlin не даёт.
     *
     * Пустой [seed] всегда даёт нулевой пресет. Аккаунтов без идентификатора в
     * норме не бывает, но лучше одинаковая картинка, чем падение.
     */
    fun fallbackIndex(seed: String?): Int {
        if (seed.isNullOrEmpty()) return 0

        var hash = 2166136261u
        for (char in seed) {
            hash = hash xor char.code.toUInt()
            hash *= 16777619u
        }
        return (hash % COUNT.toUInt()).toInt()
    }

    /**
     * Что рисовать для этой пары «ссылка + идентификатор», или `null` если
     * рисовать пресет не надо — ссылку заберёт загрузчик картинок, а пустоту
     * покажет обычная заглушка.
     *
     * Пустой [seed] означает «это не аватарка»: те же самые компоненты рисуют
     * картинки желаний, и подарочная коробка вместо «+» на пустом желании была
     * бы враньём. Запасной пресет включается ровно там, где сверху передали
     * идентификатор человека.
     */
    fun resolve(avaUrl: String?, seed: String?): Int? = when {
        isPreset(avaUrl) -> indexOf(avaUrl)
        avaUrl.isNullOrEmpty() && seed != null -> fallbackIndex(seed)
        else -> null
    }

    private fun normalize(index: Int): Int = ((index % COUNT) + COUNT) % COUNT
}

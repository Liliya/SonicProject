package com.ato.ui_state.base.image

/**
 * Встроенные аватарки — десять картинок, которые рисуются кодом.
 *
 * Пресет показывается тому, кто **сам его выбрал**: тогда в `avaUrl` лежит
 * строка `avatar://preset/7`. Схема, а не `https`, выбрана намеренно — так
 * значение нельзя спутать со ссылкой в Storage ни в базе, ни в коде: всё, что
 * не разбирается [indexOf], уходит в загрузчик картинок как раньше.
 *
 * Ничего не грузится по сети, поэтому аватарка есть и офлайн, и до того, как
 * Firestore ответит.
 *
 * Раньше пресет доставался и тем, кто ничего не выбирал: пустой `avaUrl`
 * означал «возьми [fallbackIndex] от идентификатора», и человек без фотографии
 * оказывался звездой, воздушным шариком или планетой. Картинка была стабильной,
 * но не значила ничего: по ней нельзя было узнать человека, а список из
 * разноцветных предметов читался как набор наклеек. Вместо этого у людей без
 * фотографии теперь монограмма — первая буква имени на спокойном фоне
 * (`MonogramAvatar` в sonic_ui).
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
     * Устойчивый номер «корзины» от идентификатора: одному и тому же человеку
     * всегда одно и то же число от 0 до [buckets].
     *
     * FNV-1a, а не `String.hashCode()`: у одного и того же пользователя цвет
     * должен совпадать на Android, на iOS и в тестах, а гарантий на совпадение
     * `hashCode` между платформами Kotlin не даёт.
     *
     * Этим числом монограмма выбирает себе цвет кружка. Оно и было написано
     * ради пресетов, но к пресетам не привязано — привязано к обещанию, что
     * при каждом открытии экрана человек выглядит одинаково.
     *
     * Пустой [seed] всегда даёт ноль. Аккаунтов без идентификатора в норме не
     * бывает, но лучше одинаковый цвет, чем падение.
     */
    fun bucket(seed: String?, buckets: Int): Int {
        if (seed.isNullOrEmpty() || buckets <= 0) return 0

        var hash = 2166136261u
        for (char in seed) {
            hash = hash xor char.code.toUInt()
            hash *= 16777619u
        }
        return (hash % buckets.toUInt()).toInt()
    }

    /** Какой пресет показать тому, кто ничего не выбирал. */
    fun fallbackIndex(seed: String?): Int = bucket(seed, COUNT)

    private fun normalize(index: Int): Int = ((index % COUNT) + COUNT) % COUNT
}

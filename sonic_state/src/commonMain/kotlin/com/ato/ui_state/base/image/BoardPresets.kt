package com.ato.ui_state.base.image

/**
 * Встроенные картинки досок — двенадцать предметов, которые рисуются кодом.
 *
 * Доска опознавалась эмодзи на градиентном кружке: два произвольных цвета,
 * выбранных человеком, а у доски без выбора — серый диск из
 * `stringToColor(default = Color.Gray)`. Двухстоповый градиент из случайной
 * пары цветов почти всегда даёт грязь, а серый диск — это просто «не
 * настроено».
 *
 * Пресеты закрывают оба случая и устроены ровно как [AvatarPresets], чтобы
 * доски и профили выглядели сделанными одной рукой: ничего не грузится по
 * сети, картинка есть офлайн и до того, как ответит Firestore, и ни одна
 * запись в базе не меняется — доска без выбора получает картинку,
 * детерминированную от своего идентификатора.
 *
 * Осознанный выбор хранится в том же поле `emoji`, что и раньше, строкой
 * `board://preset/7`. Схема, а не `https`, выбрана намеренно — так значение
 * нельзя спутать ни с эмодзи, ни со ссылкой.
 */
object BoardPresets {

    /** Сколько пресетов нарисовано. Художественная часть — `board_preset_art.kt` в sonic_ui. */
    const val COUNT: Int = 12

    private const val PREFIX = "board://preset/"

    /** Значение для поля доски, означающее «выбран пресет [index]». */
    fun value(index: Int): String = PREFIX + normalize(index)

    /**
     * Номер пресета из сохранённого значения, или `null` если там что-то иное.
     *
     * Номер вне известного диапазона тоже даёт `null`: если пресетов когда-нибудь
     * станет больше, старый клиент нарисует [fallbackIndex], а не пустое место.
     */
    fun indexOf(value: String?): Int? {
        val raw = value ?: return null
        if (!raw.startsWith(PREFIX)) return null
        val index = raw.substring(PREFIX.length).toIntOrNull() ?: return null
        return index.takeIf { it in 0 until COUNT }
    }

    fun isPreset(value: String?): Boolean = indexOf(value) != null

    /**
     * Какую картинку показать доске, которой ничего не выбирали.
     *
     * FNV-1a, а не `String.hashCode()`: у одной и той же доски картинка должна
     * совпадать на Android, на iOS и в тестах, а гарантий на совпадение
     * `hashCode` между платформами Kotlin не даёт.
     *
     * Пустой [seed] всегда даёт нулевой пресет — доска без идентификатора
     * бывает ровно в момент создания, и лучше показать первую картинку, чем
     * ничего.
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
     * Что нарисовать для этой доски: выбранный пресет, иначе — от [seed].
     *
     * Старое значение поля (эмодзи) сюда попадает как «не пресет» и уходит в
     * [fallbackIndex]: доска не остаётся без картинки, а миграция не нужна.
     */
    fun indexFor(value: String?, seed: String?): Int = indexOf(value) ?: fallbackIndex(seed)

    private fun normalize(index: Int): Int = ((index % COUNT) + COUNT) % COUNT
}

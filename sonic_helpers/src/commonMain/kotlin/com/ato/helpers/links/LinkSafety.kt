package com.ato.helpers.links

/**
 * Что известно про ссылку до того, как по ней пойдут.
 *
 * ЗАЧЕМ ЭТО ЕСТЬ
 * --------------
 * Ссылку в желании набирает один человек, а открывает другой, и между ними нет
 * никого, кто бы её посмотрел. Раньше строка попадала в поле, сохранялась как
 * есть и по нажатию уходила прямо в `uriHandler.openUri`. Три вещи из этого
 * получались сами собой:
 *
 *   `javascript:` и `intent:`   `openUri` на Android — это `Intent(ACTION_VIEW)`.
 *                               Схема выбирает не браузер, а того, кто её
 *                               обработает: `intent://` умеет адресовать
 *                               компонент другого приложения, `file://` —
 *                               локальный файл. Ссылка в списке желаний должна
 *                               открывать страницу, и ничего кроме.
 *
 *   `https://ozon.ru@evil.com`  Всё до `@` — это имя пользователя, а не сайт.
 *                               Карточка показывала «ozon.ru@evil.com» в одну
 *                               строку с многоточием, то есть ровно то, на что
 *                               этот приём и рассчитан.
 *
 *   `оzon.ru` кириллической «о»  Выглядит как ozon.ru в любом шрифте.
 *
 * Плюс невидимые символы: U+202E переворачивает показ строки задом наперёд, а
 * нулевой ширины пробелы разрывают имя сайта там, где глаз его склеивает.
 *
 * ЧЕГО ЭТО НЕ ДЕЛАЕТ
 * ------------------
 * Не проверяет, что находится по адресу. Ни репутации, ни чёрных списков, ни
 * загрузки страницы — приложение не ходит по чужим ссылкам, ни ради превью, ни
 * ради проверки (это была бы и трата трафика человека, и рассказ чужому сайту о
 * том, что мы его открыли). Значит, «безопасная» здесь означает ровно одно:
 * ссылка ведёт в браузер, и написано в ней то, что видно. Остальное — на
 * жалобу: [com.ato.data_storage.firebase_database.moderation.ReportDao].
 *
 * И это не место, где запрет держится. Клиент можно обойти, написав в Firestore
 * напрямую; поэтому те же правила продублированы в `functions/links.mjs`,
 * который вычищает ссылки после записи с админскими правами. Здесь — чтобы
 * человек увидел ошибку в поле, а не после сохранения.
 */
object LinkSafety {

    /** Схемы, которые вообще открываются. Всё остальное — не наше дело. */
    private val ALLOWED_SCHEMES = setOf("http", "https")

    /**
     * Сколько ссылок можно повесить на желание и какой длины.
     *
     * Числа не про экран, а про то, чтобы желание не превратилось в рассылку.
     * Те же два предела стоят в `firestore.rules` и в `functions/links.mjs` —
     * если менять, то в трёх местах сразу.
     */
    const val MAX_LINKS_PER_WISH = 20
    const val MAX_LINK_LENGTH = 2048

    private val SCHEME = Regex("^([A-Za-z][A-Za-z0-9+.\\-]*):")

    /** `ozon.ru:8080/x` — это порт, а не схема `ozon.ru`. */
    private val PORT_FIRST = Regex("^\\d+([/\\\\?#].*)?$")

    /**
     * Символы, которых в адресе быть не может, потому что имя сайта с ними —
     * уже не имя сайта.
     */
    private val FORBIDDEN_IN_HOST = setOf(' ', '<', '>', '"', '{', '}', '|', '^', '`', '\'')

    /**
     * Проверяет строку и говорит, что с ней делать.
     *
     * Ничего не выбрасывает и на пустой строке: у пустого поля ввода тоже
     * спрашивают, можно ли его сохранить.
     */
    fun check(raw: String): CheckedLink {
        val text = sanitize(raw)

        if (text.isEmpty()) return refused(LinkRefusal.NotAnAddress, raw.trim())
        if (text.length > MAX_LINK_LENGTH) return refused(LinkRefusal.TooLong, text.take(80))

        val scheme = schemeOf(text)
        if (scheme != null && scheme !in ALLOWED_SCHEMES) {
            return refused(LinkRefusal.ForeignScheme, text)
        }

        // Без схемы человек набрал «ozon.ru/product/1», и имел в виду https.
        // Подставляется именно https, а не http: если сайт умеет только http,
        // он сам туда и перебросит, а обратное неверно.
        val afterScheme = when (scheme) {
            null -> text
            else -> text.substringAfter(':')
        }.trimStart('/', '\\')

        val authorityEnd = afterScheme.indexOfFirst { it == '/' || it == '\\' || it == '?' || it == '#' }
        val authority = if (authorityEnd == -1) afterScheme else afterScheme.take(authorityEnd)
        val tail = if (authorityEnd == -1) null else afterScheme.substring(authorityEnd)

        val hidesPrefix = authority.contains('@')
        val host = hostOf(authority.substringAfterLast('@'))

        if (!isPlausibleHost(host)) return refused(LinkRefusal.NotAnAddress, text)

        val warnings = buildSet {
            if (scheme == "http") add(LinkWarning.NotEncrypted)
            if (hidesPrefix) add(LinkWarning.HiddenPrefix)
            if (looksDisguised(host)) add(LinkWarning.DisguisedName)
        }

        return CheckedLink(
            // Собранный заново, а не набранный: «ozon.ru», «HTTPS://ozon.ru» и
            // «http:/ozon.ru» — один адрес, и дальше его читают правила
            // безопасности и чистильщик на сервере, которым нужен один вид, а
            // не три. Всё, что в адресе значит хоть что-то — порт, путь,
            // параметры, якорь, — остаётся на месте.
            url = "${scheme ?: "https"}://$authority${tail.orEmpty()}",
            // Показывается настоящее имя сайта — то, что после `@`, и без
            // `www.`, которое ни о чём не говорит и занимает место.
            host = host.removePrefix("www."),
            tail = tail?.takeIf { it.isNotBlank() && it != "/" },
            refusal = null,
            warnings = warnings,
        )
    }

    /**
     * Ссылка в том виде, в котором её стоит сохранить, или `null`, если
     * сохранять нечего.
     *
     * Хранится приведённая, а не набранная: «ozon.ru» и «https://ozon.ru»
     * — один и тот же адрес, и различать их потом придётся всем, кто эту
     * строку прочитает, включая правила безопасности.
     */
    fun normalizeForStorage(raw: String): String? = check(raw).url

    /**
     * Убирает то, что нельзя увидеть, но можно подсунуть.
     *
     * Управляющие символы, символы направления письма (U+202E показывает
     * строку справа налево — «moc.live/ur.nozo» читается как ozon.ru) и
     * нулевой ширины пробелы, которыми имя сайта разрывают в месте, где глаз
     * его склеивает обратно.
     */
    private fun sanitize(raw: String): String = raw.filterNot { it.isInvisible() }.trim()

    private fun Char.isInvisible(): Boolean = when (code) {
        in 0x00..0x1F, 0x7F -> true          // управляющие, включая перевод строки и табуляцию
        in 0x80..0x9F -> true                // C1
        0x00AD, 0x180E, 0xFEFF, 0x061C -> true
        in 0x200B..0x200F -> true            // нулевой ширины + метки направления
        in 0x202A..0x202E -> true            // встраивание и переопределение направления
        in 0x2060..0x2064, in 0x2066..0x2069 -> true
        else -> false
    }

    /**
     * Схема в начале строки, если она там есть.
     *
     * Отличить схему от имени сайта с портом нельзя по одному двоеточию:
     * `ozon.ru:8080` подходит под правила написания схемы целиком (точка в
     * имени схемы разрешена). Поэтому схемой считается то, за чем идёт `//`,
     * либо то, за чем идёт не порт — так `javascript:alert(1)`, у которого
     * никаких слэшей нет, всё равно оказывается схемой.
     */
    private fun schemeOf(text: String): String? {
        val match = SCHEME.find(text) ?: return null
        val rest = text.substring(match.value.length)

        if (!rest.startsWith("//") && PORT_FIRST.matches(rest)) return null

        return match.groupValues[1].lowercase()
    }

    /** Имя сайта без порта. IPv6 приходит в скобках и режется по ним. */
    private fun hostOf(hostPort: String): String = when {
        hostPort.startsWith("[") -> hostPort.substringBefore(']') + "]"
        else -> hostPort.substringBefore(':')
    }.lowercase()

    /**
     * Похоже ли это вообще на имя сайта.
     *
     * Точка обязательна, и это не придирка: поле ссылки заполняют и текстом —
     * «спросить у мамы», «озон» — а такая строка после подстановки схемы стала
     * бы ссылкой `https://озон`, которую приложение честно попыталось бы
     * открыть. Пусть лучше человек увидит ошибку под полем.
     */
    private fun isPlausibleHost(host: String): Boolean {
        if (host.isEmpty()) return false
        if (host.any { it in FORBIDDEN_IN_HOST }) return false
        if (host.startsWith("[")) return host.length > 2

        val labels = host.split('.')
        if (labels.size < 2) return false
        if (labels.any { it.isEmpty() }) return false

        return labels.last().length >= 2
    }

    /**
     * Имя сайта, которое читается не так, как выглядит.
     *
     * Три случая, и все три — про то, что глазу верить нельзя:
     *
     *   смешанные алфавиты  «оzon.ru» с кириллической «о» и латинским
     *                       остатком. В одном слове двух алфавитов не бывает
     *                       ни в одном языке — а вот подделка так и делается.
     *                       Целиком кириллическое имя («подарки.рф») при этом
     *                       не задевается: это обычный русский домен.
     *
     *   `xn--`              то же самое, но уже закодированное. Показывать
     *                       такое как есть — показывать бессмыслицу.
     *
     *   голый IP            адрес без имени. Магазины так не адресуют.
     */
    private fun looksDisguised(host: String): Boolean {
        if (host.startsWith("[")) return true

        val labels = host.split('.')
        if (labels.all { it.isNotEmpty() && it.all(Char::isDigit) }) return true

        return labels.any { label -> label.startsWith("xn--") || label.scripts().size > 1 }
    }

    /**
     * Алфавиты, встретившиеся в куске имени.
     *
     * Диапазонами, а не `Character.UnicodeScript`: в общем коде его нет, а
     * три алфавита, которыми пользуются подделки, — это три диапазона. Цифры,
     * дефис и всё прочее не в счёт: они одинаковы везде и ничего не выдают.
     */
    private fun String.scripts(): Set<Script> = mapNotNullTo(mutableSetOf()) { char ->
        when (char.code) {
            in 0x41..0x5A, in 0x61..0x7A -> Script.Latin
            in 0x0370..0x03FF, in 0x1F00..0x1FFF -> Script.Greek
            in 0x0400..0x04FF, in 0x0500..0x052F -> Script.Cyrillic
            else -> null
        }
    }

    private enum class Script { Latin, Greek, Cyrillic }

    private fun refused(reason: LinkRefusal, shown: String) = CheckedLink(
        url = null,
        host = shown,
        tail = null,
        refusal = reason,
        warnings = emptySet(),
    )
}

/** Почему ссылку нельзя открыть вовсе. */
enum class LinkRefusal {
    /** Не адрес: пусто, с пробелами, без точки в имени сайта. */
    NotAnAddress,

    /** Схема не `http` и не `https` — то есть открывается не страница. */
    ForeignScheme,

    /** Длиннее [LinkSafety.MAX_LINK_LENGTH]. */
    TooLong,
}

/** То, что про ссылку стоит сказать вслух до того, как по ней пойдут. */
enum class LinkWarning(
    /**
     * Спрашивать ли перед открытием.
     *
     * `http` не спрашивает: незашифрованных магазинов всё ещё много, и вопрос
     * на каждый из них — это вопрос, который перестают читать. Подделанное имя
     * спрашивает: там ошибка стоит дороже одного лишнего нажатия.
     */
    val asksFirst: Boolean,
) {
    /** `http://` — соединение читается по дороге. */
    NotEncrypted(asksFirst = false),

    /** Смешанные алфавиты, `xn--` или голый IP вместо имени. */
    DisguisedName(asksFirst = true),

    /** Всё до `@`: `https://ozon.ru@evil.com` ведёт на evil.com. */
    HiddenPrefix(asksFirst = true),
}

/**
 * Разобранная ссылка: что показать, что открыть и о чём предупредить.
 *
 * @param url что открывать, или `null` — открывать нельзя
 * @param host имя сайта для заголовка карточки (или сама строка, если это не
 *   ссылка: показать набранное честнее, чем стереть его)
 * @param tail остаток адреса, если он есть
 */
data class CheckedLink(
    val url: String?,
    val host: String,
    val tail: String?,
    val refusal: LinkRefusal?,
    val warnings: Set<LinkWarning>,
) {
    val isOpenable: Boolean get() = url != null

    /** Спросить перед открытием — см. [LinkWarning.asksFirst]. */
    val asksFirst: Boolean get() = warnings.any { it.asksFirst }

    /** Первая буква имени сайта, заглавная — вместо favicon. */
    val initial: String get() = host.take(1).uppercase().ifEmpty { "?" }
}

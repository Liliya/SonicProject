package com.ato.ui_state.wishlist

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class WishlistWish(
    var documentId: String? = null,
    var userDocumentId: String? = null,
    @Deprecated("use boardIds")
    var boardDocumentId: String? = "",
    var name: String? = null,
    var description: String? = "",
    /**
     * Первая картинка желания — и только она.
     *
     * Поле осталось от времени, когда картинка была одна, и продолжает
     * писаться: сборки, выпущенные до [imageUrls], читают желание через него, и
     * без него у них картинка пропала бы вовсе. Новый код читает [images], а не
     * это поле.
     */
    var imageUrl: String? = null,
    /**
     * Все картинки желания, до [MAX_IMAGES] штук, в порядке показа.
     *
     * `null` — желание из сборки до этого изменения: картинку надо брать из
     * [imageUrl]. Ровно это и делает [images], поэтому проверять здесь на
     * `null` за пределами модели не нужно.
     */
    var imageUrls: List<String>? = null,
    /**
     * Когда желание завели и когда его в последний раз меняли — миллисекунды
     * эпохи UTC.
     *
     * Числом, а не `dev.gitlive.firebase.firestore.Timestamp`: единственный
     * сериализатор для него в этом проекте
     * ([com.ato.ui_state.wishlist.CustomTimestampSerializer]) умеет работать
     * только с Json-кодировщиком, а Firestore пишет своим — из-за этого поле
     * даты и стояло здесь закомментированным. Число сериализуется без
     * оговорок, одинаково сортируется и одинаково читается на всех
     * платформах.
     *
     * `null` — желание из сборки до этого изменения. Экран в этом случае
     * ничего не показывает: выдумывать дату нельзя, а «дата неизвестна» — шум.
     */
    var createdAt: Long? = null,
    var updatedAt: Long? = null,
    var isCompleted: Boolean? = null,
    var boardIds: List<String>? = null,
    @Deprecated("reservations live in wish/{id}/picks/{uid}; this is kept only so builds released before that change keep working, and migration 004 removes it")
    var assignedUserDocumentIds: MutableList<String>? = mutableListOf(),
    /** Первая ссылка. Пишется дальше по той же причине, что и [imageUrl]. */
    var url: String? = null,
    /**
     * Все ссылки желания. Числом не ограничены: ссылка ничего не весит и
     * ничего не грузит, в отличие от картинки.
     *
     * `null` — желание из сборки до этого изменения, ссылку надо брать из
     * [url]; за это отвечает [links].
     */
    var urls: List<String>? = null,
    /**
     * How many people have reserved this wish, without saying who.
     *
     * `@Transient` on purpose, and it is not an oversight that this is never
     * serialized. Several call sites write the whole wish back to update one
     * field — a description, a board list — and if this rode along, one of
     * them holding a stale copy would silently reset the count. It is only
     * ever written by name, as an atomic increment, in the same batch as the
     * pick document it is counting. Read it back from the snapshot, the way
     * `documentId` is.
     */
    @Transient
    var pickedCount: Int = 0
) {
    /**
     * Картинки желания, как их показывают.
     *
     * Свойство без backing field, поэтому в Firestore не уезжает — сериализуются
     * только поля конструктора.
     *
     * Пустые строки отбрасываются: у желаний, которым картинку однажды убрали,
     * в базе остаётся `""`, и без фильтра загрузчик картинок получал бы пустой
     * адрес вместо честного «картинки нет».
     */
    val images: List<String>
        get() = imageUrls
            ?.filter { it.isNotBlank() }
            ?.takeIf { it.isNotEmpty() }
            ?: listOfNotNull(imageUrl?.takeIf { it.isNotBlank() })

    /** Ссылки желания. Разбираются так же, как [images]. */
    val links: List<String>
        get() = urls
            ?.filter { it.isNotBlank() }
            ?.takeIf { it.isNotEmpty() }
            ?: listOfNotNull(url?.takeIf { it.isNotBlank() })

    companion object {
        /**
         * Сколько картинок можно приложить к желанию.
         *
         * Три — это то, что помещается в ряд миниатюр на телефоне и не
         * превращает список желаний в ленту картинок. Ссылок столько же не
         * ограничено: ссылка — это строка текста, а картинка — файл в Storage,
         * за который платят и трафиком, и деньгами.
         */
        const val MAX_IMAGES: Int = 3
    }
}
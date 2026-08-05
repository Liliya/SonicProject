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
    var imageUrl: String? = null,
//    @Serializable(with = TimestampSerializer::class)
//    var creationDate: Timestamp? = null,
    var isCompleted: Boolean? = null,
    var boardIds: List<String>? = null,
    @Deprecated("reservations live in wish/{id}/picks/{uid}; this is kept only so builds released before that change keep working, and migration 004 removes it")
    var assignedUserDocumentIds: MutableList<String>? = mutableListOf(),
    var url: String? = null,
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
)
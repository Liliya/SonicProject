package com.ato.ui_state.wishlist

/**
 * Decides whether [viewerId] is allowed to see [this] board.
 *
 * The three availability fields are independent switches, any one of which
 * grants access:
 *  - [WishlistBoard.availableForAll] — anyone,
 *  - [WishlistBoard.availableForFollowing] — people who follow the owner,
 *  - [WishlistBoard.availableForUserIds] — an explicit list of people.
 *
 * A board with none of them set is treated as private. Boards created by the
 * app always get `availableForAll = true`, so this only affects documents that
 * predate those fields — and for those, hiding is the safe default.
 *
 * [viewerFollowsOwner] has to be resolved by the caller, since it needs a
 * lookup in the follow requests.
 */
fun WishlistBoard.isVisibleTo(
    viewerId: String?,
    viewerFollowsOwner: Boolean
): Boolean {
    if (viewerId != null && viewerId == userDocumentId) return true

    if (availableForAll == true) return true
    if (availableForFollowing == true && viewerFollowsOwner) return true
    if (viewerId != null && availableForUserIds?.contains(viewerId) == true) return true

    return false
}

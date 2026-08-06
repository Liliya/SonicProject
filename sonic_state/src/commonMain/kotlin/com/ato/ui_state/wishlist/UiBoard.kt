package com.ato.ui_state.wishlist

import com.ato.ui_state.Ui
import com.ato.ui_state.base.text.UiSimpleText

data class UiBoard(
    val board: WishlistBoard?,
    val boardName: UiSimpleText,
    val boardWishCount: UiSimpleText,
): Ui
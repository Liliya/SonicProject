package com.ato.ui_state.wishlist

import com.ato.ui_state.base.text.UiSimpleText

data class UiCommonInfo(
    val info: List<Pair<UiSimpleText, UiSimpleText>>,
)

data class UiBoardInfo(
    /**
     * Картинка доски: `board://preset/N` из
     * [com.ato.ui_state.base.image.BoardPresets] либо старое эмодзи.
     */
    val picture: String?,
    val info: List<Pair<UiSimpleText, UiSimpleText>>,
)
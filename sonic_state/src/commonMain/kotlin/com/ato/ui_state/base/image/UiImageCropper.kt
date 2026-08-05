package com.ato.ui_state.base.image

import com.ato.ui_state.base.button.UiButton
import com.ato.ui_state.base.text.UiSimpleText

/**
 * Фотография, которую ещё не приняли: её показывают в круглом кадрировании и
 * ждут «Готово».
 *
 * [image] `null` — значит кадрировать нечего и экран рисуется как обычно.
 *
 * `equals`/`hashCode` написаны руками ровно по той же причине, что и в
 * [UiImagePicker]: у `data class` с `ByteArray` они сравнивают ссылку, и
 * состояние с теми же байтами в другом массиве считалось бы изменившимся.
 */
data class UiImageCropper(
    val image: ByteArray? = null,
    val confirm: UiButton,
    val cancel: UiButton,
    val hint: UiSimpleText,
) {
    val isShown: Boolean get() = image != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UiImageCropper

        if (image != null) {
            if (other.image == null) return false
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) return false
        if (confirm != other.confirm) return false
        if (cancel != other.cancel) return false
        if (hint != other.hint) return false

        return true
    }

    override fun hashCode(): Int {
        var result = image?.contentHashCode() ?: 0
        result = 31 * result + confirm.hashCode()
        result = 31 * result + cancel.hashCode()
        result = 31 * result + hint.hashCode()
        return result
    }
}

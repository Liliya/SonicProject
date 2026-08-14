package com.ato.ui_state.base.image

/**
 * Одна картинка в наборе: либо уже лежит в хранилище, либо только что выбрана
 * из галереи и ещё никуда не уехала.
 *
 * Две вещи в одном типе, потому что для экрана это одна и та же картинка на
 * одном и том же месте: только что выбранная показывается сразу, а адрес у неё
 * появится после «Сохранить». Пока это были два параллельных списка — «адреса»
 * и «файлы» — порядок между ними приходилось поддерживать вручную, и он
 * разъезжался на первом же удалении из середины.
 *
 * `equals`/`hashCode` написаны руками: у `data class` с `ByteArray` они
 * сравнивают ссылку, и те же самые байты в другом массиве считались бы другой
 * картинкой — то есть состояние «изменилось» на каждой перерисовке.
 */
data class UiImageSlot(
    val url: String? = null,
    val file: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UiImageSlot

        if (url != other.url) return false
        if (file != null) {
            if (other.file == null) return false
            if (!file.contentEquals(other.file)) return false
        } else if (other.file != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url?.hashCode() ?: 0
        result = 31 * result + (file?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * Набор картинок с потолком по количеству.
 *
 * Потолок живёт здесь, а не на экране: его знают и выбор файлов (сколько ещё
 * можно взять за раз), и ряд миниатюр (показывать ли плитку «добавить»), и
 * сохранение. Три места с одним и тем же числом разошлись бы.
 *
 * @param showFileChooser поднятый флаг означает «открыть системный выбор
 *   файла»; экран опускает его сразу после запуска, как и у [UiImagePicker].
 */
data class UiImageGallery(
    val items: List<UiImageSlot> = emptyList(),
    val maxCount: Int = 3,
    val showFileChooser: Boolean = false,
) {
    val isEmpty: Boolean get() = items.isEmpty()

    /** Сколько картинок ещё влезет. Ноль — плитки «добавить» на экране нет. */
    val freeSlots: Int get() = (maxCount - items.size).coerceAtLeast(0)

    val canAddMore: Boolean get() = freeSlots > 0
}

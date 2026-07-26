package com.ato.helpers.messages

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.jetbrains.compose.resources.StringResource

/**
 * Сообщение пользователю: ошибка, подтверждение, отмена действия.
 *
 * @param text что показать
 * @param formatArg аргумент для строки с `%1$s`
 * @param actionLabel подпись кнопки в снекбаре («Повторить», «Отменить»)
 * @param onAction что сделать по нажатию на эту кнопку
 */
data class UiMessage(
    val text: StringResource,
    val formatArg: String? = null,
    val actionLabel: StringResource? = null,
    val onAction: (() -> Unit)? = null,
)

/**
 * Один поток сообщений на приложение.
 *
 * Глобальный объект здесь выбран сознательно. Экраны собраны из Decompose-
 * компонентов, вложенных на четыре-пять уровней, и чтобы дотащить колбэк
 * «покажи ошибку» от `AddWishGateways` до корневого `Scaffold`, пришлось бы
 * добавить параметр в конструктор каждому компоненту по дороге. Сообщения
 * ничего не хранят (`replay = 0`) и не переживают процесс, так что глобальность
 * тут не создаёт скрытого состояния.
 *
 * Показывает их [com.ato.sonic_ui.base.snackbar.AppMessageHost] в корневом
 * `Scaffold` — ровно один на приложение.
 */
object AppMessages {

    private val stream = MutableSharedFlow<UiMessage>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val messages: SharedFlow<UiMessage> = stream.asSharedFlow()

    fun show(message: UiMessage) {
        stream.tryEmit(message)
    }

    fun show(text: StringResource) {
        show(UiMessage(text = text))
    }
}

/**
 * Выполняет [block], а при падении показывает [message] и возвращает `null`.
 *
 * До этого сетевые вызовы шли голыми внутри `launch { }`: при обрыве связи
 * исключение улетало в скоуп компонента, флаг загрузки оставался включённым —
 * кнопка крутилась вечно, — а пользователь не видел ничего. [onFailure] нужен
 * как раз чтобы этот флаг снять.
 *
 * Отмена корутины пробрасывается дальше и сообщением не считается.
 */
suspend fun <T> reportingFailure(
    message: StringResource,
    onFailure: () -> Unit = {},
    block: suspend () -> T,
): T? {
    return try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Throwable) {
        onFailure()
        AppMessages.show(UiMessage(text = message))
        null
    }
}

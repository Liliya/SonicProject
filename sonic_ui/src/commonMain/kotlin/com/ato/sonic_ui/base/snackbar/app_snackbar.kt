package com.ato.sonic_ui.base.snackbar

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ato.helpers.messages.AppMessages
import org.jetbrains.compose.resources.getString

const val APP_SNACKBAR_TEST_TAG = "app_snackbar"

@Composable
fun rememberAppMessageHostState(): SnackbarHostState = remember { SnackbarHostState() }

/**
 * Единственная точка, где сообщения из [AppMessages] превращаются в снекбар.
 *
 * Ставится в `snackbarHost` корневого `Scaffold`. Пока сообщение показывается,
 * сбор приостановлен, следующие копятся в буфере потока — поэтому пачка ошибок
 * не мигает, а показывается по очереди.
 */
@Composable
fun AppMessageHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(hostState) {
        AppMessages.messages.collect { message ->
            val text = if (message.formatArg == null) {
                getString(message.text)
            } else {
                getString(message.text, message.formatArg!!)
            }
            val actionLabel = message.actionLabel?.let { getString(it) }

            val result = hostState.showSnackbar(
                message = text,
                actionLabel = actionLabel,
                withDismissAction = actionLabel == null,
                duration = if (actionLabel == null) {
                    SnackbarDuration.Short
                } else {
                    SnackbarDuration.Long
                },
            )

            if (result == SnackbarResult.ActionPerformed) {
                message.onAction?.invoke()
            }
        }
    }

    SnackbarHost(
        hostState = hostState,
        modifier = modifier.testTag(APP_SNACKBAR_TEST_TAG),
    ) { data ->
        Snackbar(
            snackbarData = data,
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            actionColor = MaterialTheme.colorScheme.inversePrimary,
        )
    }
}

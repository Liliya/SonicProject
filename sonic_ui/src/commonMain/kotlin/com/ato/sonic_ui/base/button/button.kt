package com.ato.sonic_ui.base.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ato.ui_state.base.button.UiButton
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

@Composable
fun UiButton.Display(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = isEnabled
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(ButtonDefaults.IconSize),
                strokeWidth = 2.dp
            )
        } else {
            Text(text = stringResource(title))
        }
    }
}

/**
 * @param contentPadding поля вокруг подписи. Свои нужны кнопке, которая стоит
 *   не отдельной строкой, а внутри карточки рядом с текстом: с обычными
 *   двадцатью четырьмя точками по бокам она отбирает у имени столько ширины,
 *   что от него остаётся многоточие.
 */
@Composable
fun DisplayButton(
    state: UiButton,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) {
    if (state.isVisible) {
        Button(
            colors = colors,
            modifier = modifier,
            onClick = onClick,
            enabled = state.isEnabled,
            contentPadding = contentPadding,
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(text = stringResource(state.title), maxLines = 1)
            }
        }
    }
}

@Preview()
@Composable
fun ButtonPreview(
    @PreviewParameter(UiButtonProvider::class) param: UiButton
) {
    param.Display({})
}

class UiButtonProvider : PreviewParameterProvider<UiButton> {
    override val values = listOf<UiButton>(
//        UiButton("Text"),
//        UiButton("Text", isLoading = true),
//        UiButton("Text", isEnabled = false),
//        UiButton("Text", isLoading = true, isEnabled = false),
    ).asSequence()
}

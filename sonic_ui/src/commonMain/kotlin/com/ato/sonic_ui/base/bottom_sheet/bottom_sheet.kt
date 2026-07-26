package com.ato.sonic_ui.base.bottom_sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.base.text.DisplaySingleLineText
import com.ato.ui_state.base.bottom_sheet.UiBottomSheet
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    state: UiBottomSheet,
    onDismissRequest: () -> Unit,
    minHeight: Dp = 300.dp,
    modifier: Modifier = Modifier,
    closeContentDescription: String? = null,
    content: @Composable () -> Unit
) {
    // Создаем состояние для управления `BottomSheet`
    val bottomSheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    // Показываем `BottomSheet`, если эмодзи не равны null
    LaunchedEffect(state.isShown) {
        if (state.isShown) {
            coroutineScope.launch {
                bottomSheetState.show()
            }
        } else {
            coroutineScope.launch {
                bottomSheetState.hide()
            }
        }
    }

    if (state.isShown) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            onDismissRequest = onDismissRequest,
            content = {
                Column(
                    modifier = modifier.defaultMinSize(minHeight = minHeight),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DisplaySingleLineText(
                            state = state.title,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                        // Крестик был иконкой 24dp и всей областью нажатия
                        // заодно — вдвое меньше минимума в 48dp.
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(48.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = onDismissRequest
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = closeContentDescription,
                            )
                        }
                    }

                    content()
                }
            }
        )
    }
}

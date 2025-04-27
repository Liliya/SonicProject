package com.ato.sonic_ui.ikirag

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ato.sonic_ui.base.Display
import com.ato.ui_state.base.UiIcon
import com.ato.ui_state.ikirag.IkiragUi
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
fun IkiragUi.Display(
    modifier: Modifier = Modifier,
) {
    var selectedText by remember { mutableStateOf<String?>(null) }
    if (selectedText != null) {
        openTranslation(selectedText!!)
        selectedText = null
    }
    Card(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { selectedText = text }
                )
            },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val icon = remember(isLiked) {
                when (isLiked) {
                    true -> Icons.Filled.Favorite
                    false -> Icons.Filled.Clear
                    else -> null
                }
            }

            SelectableTextWithTranslate(
                text = text,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState())
            )

            if (icon != null) {
                UiIcon(icon).Display(
                    Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }
    }
}

@Composable
fun SelectableTextWithTranslate(text: String, modifier: Modifier = Modifier) {
    var isMenuVisible by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val toolbar = remember {
        CustomTextToolbar(
            onShowMenu = {
                isMenuVisible = true
            },
            onHideMenu = {
                isMenuVisible = false
            },
            onTranslate = { selected ->
                selectedText = selected
            }
        )
    }

    // Стейт текстового поля
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(text = text)
        )
    }

    selectedText?.let { textToTranslate ->
        openTranslation(textToTranslate)
        selectedText = null
    }

    CompositionLocalProvider(LocalTextToolbar provides toolbar) {
        Box(modifier = modifier) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        // Фокус может влиять на показ меню
                    }
                    .background(Color.Transparent),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                ),
                readOnly = true
            )

            if (isMenuVisible) {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = {
                        textFieldValue = textFieldValue.copy(selection = TextRange(0, 0))
                        selectedText = null
                        toolbar.hide()
                        isMenuVisible = false
                        focusManager.clearFocus()
                    }
                ) {
                    DropdownMenuItem(
                        text = { Text("Translate") },
                        onClick = {
                            val start = textFieldValue.selection.start
                            val end = textFieldValue.selection.end
                            if (start != end && start >= 0 && end >= 0) {
                                val selected = text.substring(start until end)
                                toolbar.setSelectedText(selected)
                                toolbar.requestTranslate()
                                selectedText = selected
                            }
                            // Сбрасываем выделение
                            focusManager.clearFocus()
                            textFieldValue = textFieldValue.copy(selection = TextRange(0, 0))
                            toolbar.hide()
                        }
                    )
                }
            }
        }
    }
}

class CustomTextToolbar(
    private val onShowMenu: () -> Unit,
    private val onHideMenu: () -> Unit,
    private val onTranslate: (String) -> Unit
) : TextToolbar {
    private var selectedText: String? = null

    override var status: TextToolbarStatus = TextToolbarStatus.Hidden
        private set

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        if (status == TextToolbarStatus.Hidden) {

        } else {

        }
        status = TextToolbarStatus.Shown
        onShowMenu()
    }

    override fun hide() {
        status = TextToolbarStatus.Hidden
        onHideMenu()
    }

    fun setSelectedText(text: String) {
        selectedText = text
    }

    fun requestTranslate() {
        selectedText?.let(onTranslate)
    }
}

@Composable
expect fun openTranslation(
    text: String,
)

// ------------------------------------------------------------------------
// ------------------------------------------------------------------------
// ------------------------------------------------------------------------

@Preview()
@Composable
private fun Preview() {
    IkiragUi(
        text = """
            Не в силах нас ни смех,  ни грех 
            свернуть с пути отважного,
              мы  строим счастье сразу всех,  
            и нам плевать на каждого.
        """.trimIndent()
    ).Display()
}
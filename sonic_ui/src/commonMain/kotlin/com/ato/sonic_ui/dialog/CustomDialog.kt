package com.ato.sonic_ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview()
@Composable
fun CustomDialog(
    show: Boolean = false,
    onDismiss: (Boolean) -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    if (show) {
        Dialog(onDismissRequest = {
            onDismiss(false)
        }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                shadowElevation = 8.dp // Тень под диалогом
            ) {
                content()
            }
        }
    }
}

@Composable
fun SampleDialogContent(onShow: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Custom Dialog", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(20.dp))
        Text("This is a custom-designed dialog in Jetpack Compose.")
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = { onShow(false) }
            ) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onShow(false) }
            ) {
                Text("OK")
            }
        }
    }

}

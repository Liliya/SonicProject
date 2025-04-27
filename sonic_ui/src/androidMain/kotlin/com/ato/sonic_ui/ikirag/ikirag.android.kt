package com.ato.sonic_ui.ikirag

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun openTranslation(
    text: String,
) {
    val context = LocalContext.current
    openTranslateChooser(context, text)
}

fun openTranslateChooser(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_PROCESS_TEXT, text)
        putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
        setPackage("com.google.android.apps.translate") // Важно! Только Google Translate
    }
    context.startActivity(intent)
    println("ALEX:")

//    val intent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
//        type = "text/plain"
//        putExtra(Intent.EXTRA_PROCESS_TEXT, text)
//        putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
//        `package` = "com.google.android.apps.translate" // Специально указываем пакет
//    }
//    val intent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
//        type = "text/plain"
//        putExtra(Intent.EXTRA_PROCESS_TEXT, text)
//        putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
//    }
//    context.startActivity(Intent.createChooser(intent, "Перевести текст"))
}
package com.ato.sonic_ui.base


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.PickerResultLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

object LoadOptions {
    const val PLAIN_TEXT = "text/plain"
    const val ALL_TYPES = "*/*"
    const val EPUB_TEXT = "application/epub+zip"
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MyApp() {
    var fileContent by remember { mutableStateOf("Press button to load text") }
//    getLauncher(onStartLoading = {
//
//    }) { loadedText ->
//        fileContent = loadedText
//    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("File Picker Example") })
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
//            Button(onClick = { readFilesLauncher.launch(LoadOptions.PLAIN_TEXT) }) {
//                Text("Pick a Text File")
//            }
            Text(text = fileContent)
        }
    }
}

@Composable
fun getLauncher(
    onStartLoading: () -> Unit = {},
    onLoaded: (String) -> Unit = {},
): PickerResultLauncher {


    return rememberFilePickerLauncher(
        type = FileKitType.File(setOf("txt")),
        mode = FileKitMode.Single,
    ) { file: PlatformFile? ->
        onStartLoading()

        readFile(file, onLoaded)
    }
}

fun readFile(file: PlatformFile?, onLoaded: (String) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val fileBytes = file?.readBytes()
            val fileContent = fileBytes?.decodeToString()?: "can't parse file"

            onLoaded(fileContent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


//@Composable
//fun getLauncher(
//    onStartLoading: () -> Unit = {},
//    onLoaded: (String) -> Unit = {}
//): State<ActivityResultLauncher<String>> {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//
//    val launcher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let {
//
//            scope.launch {
//                onStartLoading()
//                val text = withContext(Dispatchers.IO) {
//                    readTextFromUri(context, it)
//                }
//
//                onLoaded(text)
//            }
//        }
//    }
//
//    return rememberUpdatedState(launcher)
//}
//
//fun readTextFromUri(context: Context, uri: Uri): String {
//    val contentResolver = context.contentResolver
//    val mime = contentResolver.getType(uri)
//    return when (mime) {
//        "text/plain" -> {
//            val stringBuilder = StringBuilder()
//            context.contentResolver.openInputStream(uri)?.use { inputStream ->
//                BufferedReader(InputStreamReader(inputStream)).use { reader ->
//                    var line: String?
//                    while (reader.readLine().also { line = it } != null) {
//                        stringBuilder.append(line).append('\n')
//                    }
//                }
//            }
//            stringBuilder.toString()
//        }
//
//        else -> {
//            "not supported"
//        }
//    }
//}

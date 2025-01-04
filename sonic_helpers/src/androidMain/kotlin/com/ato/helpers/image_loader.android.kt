package com.ato.helpers

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toOkioPath

actual fun getCacheDir(context: PlatformContext): Path {
    return context.cacheDir.toOkioPath()
}

@Composable
actual fun getPlatformContext(): PlatformContext {
    return LocalContext.current
}
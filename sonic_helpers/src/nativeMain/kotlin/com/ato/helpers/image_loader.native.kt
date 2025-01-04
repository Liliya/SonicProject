package com.ato.helpers

import androidx.compose.runtime.Composable
import coil3.PlatformContext
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUserDomainMask

actual fun getCacheDir(context: PlatformContext): okio.Path {
    val cachesDirectory: String = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory, NSUserDomainMask, true
    ).firstOrNull() as String? ?: NSTemporaryDirectory()

    return (cachesDirectory).toPath()
}

@Composable
actual fun getPlatformContext(): PlatformContext {
    return PlatformContext.INSTANCE
}
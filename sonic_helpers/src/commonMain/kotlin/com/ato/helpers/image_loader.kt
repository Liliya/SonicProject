package com.ato.helpers

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.util.DebugLogger
import okio.Path

fun getAsyncImageLoader(context: PlatformContext): ImageLoader {

    return ImageLoader.Builder(context)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .memoryCache {
            MemoryCache
                .Builder()
                .maxSizeBytes(50 * 1024 * 1024) // 50 MB
                .strongReferencesEnabled(true)
                .build()
        }
        .diskCachePolicy(CachePolicy.ENABLED)
        .diskCache {
            DiskCache.Builder()
                .directory(getCacheDir(context))
                .maxSizeBytes(1_000 * 1024 * 1024) // 1 GB
                .build()
        }
        .crossfade(true)
        .logger(DebugLogger())
        .build()
}

@Composable
expect fun getPlatformContext() :PlatformContext

expect fun getCacheDir(context: PlatformContext): Path
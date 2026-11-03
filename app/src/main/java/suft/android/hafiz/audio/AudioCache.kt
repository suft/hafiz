package suft.android.hafiz.audio

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * Disk cache for recitations, so an ayah heard once replays instantly and, until Android or the user clears the cache, offline.
 * Must be a process-wide singleton (SimpleCache locks its directory).
 */
@OptIn(UnstableApi::class)
class AudioCache(context: Context) {

    private val cache = SimpleCache(
        File(context.cacheDir, "recitations"),
        LeastRecentlyUsedCacheEvictor(MAX_BYTES),
        StandaloneDatabaseProvider(context),
    )

    val dataSourceFactory: CacheDataSource.Factory = CacheDataSource.Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(
            DefaultHttpDataSource.Factory()
                .setUserAgent("Hafiz-Android")
                .setConnectTimeoutMs(15_000)
                .setReadTimeoutMs(15_000),
        )
        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    private companion object {
        const val MAX_BYTES = 200L * 1024 * 1024
    }
}

package suft.android.hafiz.audio

import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.launch

enum class PlaybackStatus { Idle, Buffering, Playing, Paused }

/**
 * Plays one or more consecutive ayahs (a playlist of per-ayah MP3s) and pre-downloads the
 * next question's audio into [AudioCache]. Replaces the original 3-MediaPlayer juggling.
 *
 * Must be used from the main thread. Call [release] when done.
 *
 * @param trimEndMs stop each ayah this many ms before its end (the original did this in the
 *   "next ayah" mode so the recitation doesn't run into the answer).
 */
@OptIn(UnstableApi::class)
class RecitationPlayer(
    context: Context,
    private val audioCache: AudioCache,
    private val scope: CoroutineScope,
    private val trimEndMs: Long = 0,
) {
    private val player: ExoPlayer = ExoPlayer.Builder(context)
        .setMediaSourceFactory(DefaultMediaSourceFactory(audioCache.dataSourceFactory))
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .build(),
            /* handleAudioFocus = */ true,
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    private val _status = MutableStateFlow(PlaybackStatus.Idle)
    val status: StateFlow<PlaybackStatus> = _status.asStateFlow()

    private val _errors = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    /** Emits when a recitation could not be loaded (usually: no internet connection). */
    val errors: SharedFlow<Unit> = _errors.asSharedFlow()

    private var loadedUrls: List<String> = emptyList()
    private var trimJob: Job? = null
    private var prefetchJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) = updateStatus()

            // Idle releases audio focus, so other apps' audio resumes; play() restarts from the first ayah.
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) player.stop()
            }

            override fun onPlayerError(error: PlaybackException) {
                loadedUrls = emptyList()
                player.stop()
                player.clearMediaItems()
                updateStatus()
                _errors.tryEmit(Unit)
            }
        })
    }

    /** Plays [urls] from the start, or resumes them if they are already loaded and paused. */
    fun play(urls: List<String>) {
        if (urls.isEmpty()) return
        if (urls != loadedUrls) {
            player.setMediaItems(urls.map(MediaItem::fromUri))
            loadedUrls = urls
            player.prepare()
        } else if (player.playbackState == Player.STATE_ENDED || player.playbackState == Player.STATE_IDLE) {
            player.seekTo(0, 0)
            player.prepare()
        }
        player.play()
        updateStatus()
        startTrimWatcher()
    }

    fun toggle(urls: List<String>) {
        if (player.playWhenReady && urls == loadedUrls &&
            player.playbackState != Player.STATE_ENDED && player.playbackState != Player.STATE_IDLE
        ) {
            pause()
        } else {
            play(urls)
        }
    }

    fun pause() {
        player.pause()
        updateStatus()
    }

    fun stop() {
        trimJob?.cancel()
        player.stop()
        player.clearMediaItems()
        loadedUrls = emptyList()
        updateStatus()
    }

    /** Stops playback and releases audio focus but keeps the ayahs loaded; the next [play] of the same urls starts from the first one. */
    fun halt() {
        trimJob?.cancel()
        player.stop()
        updateStatus()
    }

    /** Downloads [urls] into the cache in the background (replaces any previous prefetch). */
    fun prefetch(urls: List<String>) {
        prefetchJob?.cancel()
        prefetchJob = scope.launch(Dispatchers.IO) {
            for (url in urls) {
                if (!isActive) break
                val writer = CacheWriter(
                    audioCache.dataSourceFactory.createDataSource(),
                    DataSpec(url.toUri()),
                    /* temporaryBuffer = */ null,
                    /* progressListener = */ null,
                )
                try {
                    cacheCancellable(writer)
                } catch (e: HttpDataSource.InvalidResponseCodeException) {
                    continue // this ayah is missing on the server; the next one may still exist
                } catch (e: IOException) {
                    break // offline; the real playback attempt will report it
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
    }

    /** Runs the blocking download so that cancelling the coroutine cancels it too — and cannot leak a hook. */
    private suspend fun cacheCancellable(writer: CacheWriter) = coroutineScope {
        val watcher = launch {
            try {
                awaitCancellation()
            } finally {
                writer.cancel()
            }
        }
        try {
            runInterruptible { writer.cache() }
        } finally {
            watcher.cancel()
        }
    }

    fun release() {
        trimJob?.cancel()
        prefetchJob?.cancel()
        player.release()
    }

    private fun updateStatus() {
        _status.value = when {
            player.isPlaying -> PlaybackStatus.Playing
            player.playWhenReady && player.playbackState == Player.STATE_BUFFERING -> PlaybackStatus.Buffering
            player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED -> PlaybackStatus.Idle
            else -> PlaybackStatus.Paused
        }
    }

    private fun startTrimWatcher() {
        if (trimEndMs <= 0 || trimJob?.isActive == true) return
        trimJob = scope.launch {
            while (isActive && player.playWhenReady) {
                val duration = player.duration
                if (player.isPlaying && duration != C.TIME_UNSET && duration - player.currentPosition < trimEndMs) {
                    if (player.hasNextMediaItem()) {
                        player.seekToNextMediaItem()
                    } else {
                        player.pause()
                        player.seekTo(0, 0)
                        player.stop() // -> Idle; the next play() starts from the first ayah again
                        updateStatus()
                    }
                }
                delay(TRIM_POLL_MS)
            }
        }
    }

    private companion object {
        const val TRIM_POLL_MS = 100L
    }
}

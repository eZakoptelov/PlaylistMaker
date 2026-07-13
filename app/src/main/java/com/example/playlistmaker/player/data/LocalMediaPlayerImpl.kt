package com.example.playlistmaker.player.data

import android.media.MediaPlayer
import android.util.Log
import com.example.playlistmaker.player.domain.MediaPlayerWrapper

class LocalMediaPlayerImpl(
    private val mediaPlayer: MediaPlayer
) : MediaPlayerWrapper {

    private var onCompletionCallback: (() -> Unit)? = null
    private var onPreparedCallback: (() -> Unit)? = null
    private var onErrorCallback: ((Exception) -> Unit)? = null

    override fun setOnCompletionListener(callback: () -> Unit) {
        onCompletionCallback = callback
    }

    override fun load(
        url: String,
        onPrepared: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            // reset() допустим, если мы осознанно хотим переиспользовать экземпляр.
            // Но помни: после reset() плеер в состоянии UNINITIALIZED.
            mediaPlayer.reset()
            mediaPlayer.setDataSource(url)

            onPreparedCallback = onPrepared
            onErrorCallback = onError

            mediaPlayer.setOnPreparedListener {
                onPreparedCallback?.invoke()
            }

            mediaPlayer.setOnErrorListener { _, what, extra ->
                onErrorCallback?.invoke(Exception("MediaPlayer error: what=$what, extra=$extra"))
                false
            }

            mediaPlayer.setOnCompletionListener {
                onCompletionCallback?.invoke()
            }

            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            Log.e("PlayerWrapper", "Error loading media", e)
            onError(e)
        }
    }

    override fun start() {
        try {
            mediaPlayer.start()
        } catch (e: IllegalStateException) {
            Log.w("PlayerWrapper", "Cannot start: player not prepared or released", e)
        }
    }

    override fun pause() {
        try {
            mediaPlayer.pause()
        } catch (e: IllegalStateException) {
            Log.w("PlayerWrapper", "Cannot pause: player not playing or released", e)
        }
    }

    override fun stop() {
        try {
            mediaPlayer.stop()
        } catch (e: IllegalStateException) {

            Log.w("PlayerWrapper", "Tried to stop in invalid state (already released or not prepared)", e)
        }
    }

    override fun release() {
        try {
            mediaPlayer.release()
        } catch (e: IllegalStateException) {
            Log.w("PlayerWrapper", "Release called on already released player", e)
        }
    }

    override fun isPlaying(): Boolean {
        return try {
            mediaPlayer.isPlaying
        } catch (_: IllegalStateException) {
            false
        }
    }

    override fun currentPosition(): Long {
        return try {
            mediaPlayer.currentPosition.toLong()
        } catch (_: IllegalStateException) {
            0L
        }
    }
}

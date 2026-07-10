package com.example.playlistmaker.player.data

import android.media.MediaPlayer
import com.example.playlistmaker.player.domain.MediaPlayerWrapper

class LocalMediaPlayerImpl : MediaPlayerWrapper {

    private var mediaPlayer: MediaPlayer? = null
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
        release()

        onPreparedCallback = onPrepared
        onErrorCallback = onError

        mediaPlayer = MediaPlayer().apply {
            setOnCompletionListener {
                onCompletionCallback?.invoke()
            }
            setOnPreparedListener {
                onPreparedCallback?.invoke()
            }
            setOnErrorListener { mp, what, extra ->
                release()
                onErrorCallback?.invoke(Exception("MediaPlayer error: what=$what, extra=$extra"))
                false
            }
        }

        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.prepareAsync()
        } catch (e: Exception) {
            release()
            onError(e)
        }
    }

    override fun start() {
        mediaPlayer?.start()
    }

    override fun pause() {
        mediaPlayer?.pause()
    }

    override fun stop() {
        if (mediaPlayer?.isPlaying == true) {
            try {
                mediaPlayer?.stop()
            } catch (_: Exception) {
            }
        }
    }

    override fun release() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer?.release()
            } catch (_: Exception) {
            } finally {
                mediaPlayer = null
            }
        }
    }

    override fun isPlaying() = mediaPlayer?.isPlaying == true

    override fun currentPosition(): Long = mediaPlayer?.currentPosition?.toLong() ?: 200L
}

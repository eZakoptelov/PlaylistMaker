package com.example.playlistmaker.player.domain

interface MediaPlayerWrapper {
    fun setOnCompletionListener(callback: () -> Unit)
    fun load(url: String, onPrepared: () -> Unit, onError: (Exception) -> Unit)
    fun start()
    fun pause()
    fun stop()
    fun release()
    fun isPlaying(): Boolean
    fun currentPosition(): Long
}

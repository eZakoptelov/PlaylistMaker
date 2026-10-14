package com.example.playlistmaker.player.domain.intaractor
interface PlayerInteractor {
    fun setOnCompletionListener(callback: () -> Unit)
    fun setPreparedListener(callback: () -> Unit)
    fun load(url: String, onError: (Exception) -> Unit)
    fun play()
    fun pause()
    fun stop()
    fun release()
    fun isPlaying(): Boolean
    fun getCurrentPosition(): Long

}


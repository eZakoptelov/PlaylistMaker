// PlayerInteractor.kt
package com.example.playlistmaker.player.domain
interface PlayerInteractor {
    fun setOnCompletionListener(callback: () -> Unit)
    fun setPreparedListener(callback: () -> Unit)

    // Этот метод нужен, чтобы ViewModel мог инициировать загрузку
    fun load(url: String, onError: (Exception) -> Unit)

    fun play()
    fun pause()
    fun stop()
    fun release()
    fun isPlaying(): Boolean
    fun getCurrentPosition(): Long

}


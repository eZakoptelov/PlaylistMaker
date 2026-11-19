package com.example.playlistmaker.player.domain

import kotlinx.coroutines.flow.StateFlow

data class PlayerPlaybackState(
    val trackId: Long? = null,
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val isReady: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val error: String? = null
)
interface PlayerServiceControl {
    fun getPlaybackState(): StateFlow<PlayerPlaybackState>

    fun play()
    fun pause()
    fun stopPlayback()
    fun showForegroundNotification()
    fun hideForegroundNotification()
}

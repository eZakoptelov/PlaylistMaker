package com.example.playlistmaker.player.ui.viewmodel

import com.example.playlistmaker.search.domain.model.TrackItem

data class PlayerUiState(
    val track: TrackItem? = null,
    val isPlaying: Boolean = false,
    val isReady: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val error: String? = null
)
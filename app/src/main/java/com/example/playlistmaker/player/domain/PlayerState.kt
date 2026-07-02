package com.example.playlistmaker.player.domain

import com.example.playlistmaker.search.domain.model.TrackItem

data class PlayerState(
    val track: TrackItem? = null,
    val isPlaying: Boolean = false,
    val isReady: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val error: String? = null
)


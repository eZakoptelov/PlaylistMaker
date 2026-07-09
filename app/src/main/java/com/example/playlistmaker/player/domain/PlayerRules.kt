package com.example.playlistmaker.player.domain

import com.example.playlistmaker.player.ui.viewmodel.PlayerUiState

interface PlayerRules {
    fun onTrackFinished(state: PlayerUiState): PlayerUiState
    fun formatDuration(millis: Long): String
}


package com.example.playlistmaker.player.domain.impl

import com.example.playlistmaker.player.domain.PlayerRules
import com.example.playlistmaker.player.ui.viewmodel.PlayerUiState

class PlayerRulesImpl : PlayerRules {

    override fun onTrackFinished(state: PlayerUiState): PlayerUiState {
        return state.copy(
            isPlaying = false,
            currentPosition = 0L
        )
    }

    override fun formatDuration(millis: Long): String {
        val totalSeconds = millis.coerceAtLeast(0L) / 1000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L

        return "%02d:%02d".format(minutes, seconds)
    }
}

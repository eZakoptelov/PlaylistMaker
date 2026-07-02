package com.example.playlistmaker.player.domain.impl

import com.example.playlistmaker.player.domain.PlayerRules
import com.example.playlistmaker.player.domain.PlayerState

class PlayerRulesImpl : PlayerRules {
    override fun onTrackFinished(state: PlayerState): PlayerState {
        return state.copy(isPlaying = false, currentPosition = 0)
    }

    override fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }
}

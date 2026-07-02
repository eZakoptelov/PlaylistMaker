package com.example.playlistmaker.player.domain
interface PlayerRules {
    fun onTrackFinished(state: PlayerState): PlayerState
    fun formatDuration(millis: Long): String
}


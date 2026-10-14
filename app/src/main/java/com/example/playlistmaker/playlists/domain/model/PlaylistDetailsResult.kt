package com.example.playlistmaker.playlists.domain.model

import com.example.playlistmaker.search.domain.model.TrackItem

sealed class PlaylistDetailsResult {
    data class Success(
        val playlist: Playlist,
        val tracks: List<TrackItem>,
        val totalDurationMs: Long
    ) : PlaylistDetailsResult()

    data class Error(val message: String) : PlaylistDetailsResult()
}

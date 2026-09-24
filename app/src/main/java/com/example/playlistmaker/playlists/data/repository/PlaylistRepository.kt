package com.example.playlistmaker.playlists.data.repository

import com.example.playlistmaker.playlists.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.TrackItem
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    suspend fun createPlaylist(playlist: Playlist): Long
    suspend fun updatePlaylist(playlist: Playlist)
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun addTrackToPlaylistWithTrack(playlist: Playlist, track: TrackItem)
}

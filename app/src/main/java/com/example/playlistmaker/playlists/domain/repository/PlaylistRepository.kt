package com.example.playlistmaker.playlists.domain.repository

import com.example.playlistmaker.playlists.data.db.entity.PlaylistWithTracks
import com.example.playlistmaker.playlists.data.db.entity.TrackInPlaylistEntity
import com.example.playlistmaker.playlists.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.TrackItem
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    suspend fun createPlaylist(playlist: Playlist): Long
    suspend fun updatePlaylist(playlist: Playlist)
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun addTrackToPlaylistWithTrack(playlist: Playlist, track: TrackItem)
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean
    suspend fun getPlaylistWithTracks(playlistId: Long): PlaylistWithTracks?
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    suspend fun deleteTrackFromPlaylist(playlistId: Long, trackId: Long)
    suspend fun getTracksByIds(trackIds: List<Long>): List<TrackInPlaylistEntity>
    suspend fun deletePlaylist(playlistId: Long)
}

package com.example.playlistmaker.playlists.domain.interactor

import com.example.playlistmaker.playlists.domain.model.Playlist
import com.example.playlistmaker.playlists.domain.model.PlaylistDetailsResult
import com.example.playlistmaker.search.domain.model.TrackItem
import kotlinx.coroutines.flow.Flow

interface PlaylistInteractor {
    suspend fun createPlaylist(
        name: String,
        description: String,
        coverPath: String
    ): Long

    suspend fun updatePlaylist(playlist: Playlist)

    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun getPlaylistById(playlistId: Long): Playlist?

    suspend fun addTrackToPlaylist(playlist: Playlist, track: TrackItem)

    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean

    suspend fun getPlaylistDetails(playlistId: Long): PlaylistDetailsResult

    suspend fun deleteTrackFromPlaylist(playlistId: Long, trackId: Long)

    suspend fun deletePlaylist(playlistId: Long)

}
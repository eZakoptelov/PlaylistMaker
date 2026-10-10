package com.example.playlistmaker.playlists.domain.interactor

import com.example.playlistmaker.playlists.domain.model.Playlist
import com.example.playlistmaker.playlists.domain.repository.PlaylistRepository
import com.example.playlistmaker.search.domain.model.TrackItem
import kotlinx.coroutines.flow.Flow

class PlaylistInteractor(
    private val repository: PlaylistRepository
) {
    suspend fun createPlaylist(name: String, description: String, coverPath: String): Long {
        return repository.createPlaylist(
            Playlist(
                name = name,
                description = description,
                coverPath = coverPath
            )
        )
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        repository.updatePlaylist(playlist)
    }

    fun getPlaylists(): Flow<List<Playlist>> {
        return repository.getPlaylists()
    }

    suspend fun addTrackToPlaylist(playlist: Playlist, track: TrackItem) {
        repository.addTrackToPlaylistWithTrack(playlist, track)
    }

    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return repository.isTrackInPlaylist(playlistId, trackId)
    }
}

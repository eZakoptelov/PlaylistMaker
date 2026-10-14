package com.example.playlistmaker.playlists.data.interactor.impl

import com.example.playlistmaker.playlists.data.mapper.PlaylistMapper
import com.example.playlistmaker.playlists.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlists.domain.model.Playlist
import com.example.playlistmaker.playlists.domain.model.PlaylistDetailsResult
import com.example.playlistmaker.playlists.domain.repository.PlaylistRepository
import com.example.playlistmaker.search.domain.model.TrackItem

class PlaylistInteractorImpl(
    private val repository: PlaylistRepository
) : PlaylistInteractor {

    override suspend fun createPlaylist(name: String, description: String, coverPath: String): Long =
        repository.createPlaylist(
            Playlist(name = name, description = description, coverPath = coverPath)
        )

    override suspend fun updatePlaylist(playlist: Playlist) =
        repository.updatePlaylist(playlist)

    override fun getPlaylists() = repository.getPlaylists()

    override suspend fun getPlaylistById(playlistId: Long): Playlist? =
        repository.getPlaylistById(playlistId)

    override suspend fun addTrackToPlaylist(playlist: Playlist, track: TrackItem) =
        repository.addTrackToPlaylistWithTrack(playlist, track)

    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean =
        repository.isTrackInPlaylist(playlistId, trackId)

    override suspend fun getPlaylistDetails(playlistId: Long): PlaylistDetailsResult {
        val result = repository.getPlaylistWithTracks(playlistId)
            ?: return PlaylistDetailsResult.Error("Плейлист не найден")

        val tracks = result.tracks
            .sortedByDescending { it.addedAt }
            .map { PlaylistMapper.mapToTrackItem(it) }
        val totalDurationMs = tracks.sumOf { it.trackTimeMillis }

        val playlist = PlaylistMapper.mapToPlaylist(result.playlist).copy(trackCount = tracks.size)

        return PlaylistDetailsResult.Success(
            playlist = playlist,
            tracks = tracks,
            totalDurationMs = totalDurationMs
        )
    }
    override suspend fun deleteTrackFromPlaylist(playlistId: Long, trackId: Long) {
        repository.deleteTrackFromPlaylist(playlistId, trackId)
    }
    override suspend fun deletePlaylist(playlistId: Long) {
        repository.deletePlaylist(playlistId)
    }
}

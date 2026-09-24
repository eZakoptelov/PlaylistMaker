package com.example.playlistmaker.playlists.data.repository.impl

import com.example.playlistmaker.playlists.data.converters.PlaylistTrackConverter
import com.example.playlistmaker.playlists.data.db.dao.PlaylistDao
import com.example.playlistmaker.playlists.data.db.dao.TrackInPlaylistDao
import com.example.playlistmaker.playlists.data.db.entity.PlaylistEntity
import com.example.playlistmaker.playlists.data.db.entity.TrackInPlaylistEntity
import com.example.playlistmaker.playlists.data.repository.PlaylistRepository
import com.example.playlistmaker.playlists.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.TrackItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val trackInPlaylistDao: TrackInPlaylistDao,
    private val converter: PlaylistTrackConverter
) : PlaylistRepository {

    override suspend fun createPlaylist(playlist: Playlist): Long {
        val entity = PlaylistEntity(
            name = playlist.name,
            description = playlist.description,
            coverPath = playlist.coverPath,
            trackIds = converter.trackIdsToJson(emptyList()),
            trackCount = 0
        )
        return playlistDao.insertPlaylist(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(
            PlaylistEntity(
                id = playlist.id,
                name = playlist.name,
                description = playlist.description,
                coverPath = playlist.coverPath,
                trackIds = playlist.trackIds,
                trackCount = playlist.trackCount
            )
        )
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.observePlaylists().map { entities ->
            entities.map { entity ->
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    description = entity.description,
                    coverPath = entity.coverPath,
                    trackIds = entity.trackIds,
                    trackCount = entity.trackCount
                )
            }
        }
    }

    override suspend fun addTrackToPlaylistWithTrack(playlist: Playlist, track: TrackItem) {
        val trackIds = converter.jsonToTrackIds(playlist.trackIds).toMutableList()
        if (track.trackId !in trackIds) {
            trackIds.add(track.trackId)
            playlistDao.updatePlaylist(
                PlaylistEntity(
                    id = playlist.id,
                    name = playlist.name,
                    description = playlist.description,
                    coverPath = playlist.coverPath,
                    trackIds = converter.trackIdsToJson(trackIds),
                    trackCount = trackIds.size
                )
            )
            trackInPlaylistDao.insertTrack(
                TrackInPlaylistEntity(
                    trackId = track.trackId,
                    trackName = track.trackName,
                    artistName = track.artistName,
                    trackTimeMillis = track.trackTimeMillis,
                    artworkUrl100 = track.artworkUrl100,
                    collectionName = track.collectionName,
                    releaseDate = track.releaseDate,
                    primaryGenreName = track.primaryGenreName,
                    country = track.country,
                    previewUrl = track.previewUrl,
                    addedAt = System.currentTimeMillis()
                )
            )
        }
    }
}

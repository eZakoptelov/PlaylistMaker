package com.example.playlistmaker.playlists.data.repository.impl

import com.example.playlistmaker.playlists.data.db.dao.PlaylistDao
import com.example.playlistmaker.playlists.data.db.dao.TrackInPlaylistDao
import com.example.playlistmaker.playlists.data.db.entity.PlaylistEntity
import com.example.playlistmaker.playlists.data.db.entity.PlaylistWithTracks
import com.example.playlistmaker.playlists.data.db.entity.TrackInPlaylistEntity
import com.example.playlistmaker.playlists.domain.model.Playlist
import com.example.playlistmaker.playlists.domain.repository.PlaylistRepository
import com.example.playlistmaker.search.domain.model.TrackItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val trackInPlaylistDao: TrackInPlaylistDao
) : PlaylistRepository {
    override suspend fun createPlaylist(playlist: Playlist): Long {
        val entity = PlaylistEntity(
            name = playlist.name,
            description = playlist.description,
            coverPath = playlist.coverPath
        )
        return playlistDao.insertPlaylist(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(
            PlaylistEntity(
                id = playlist.id,
                name = playlist.name,
                description = playlist.description,
                coverPath = playlist.coverPath
            )
        )
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.observePlaylists().map { playlistsWithTracks ->
            playlistsWithTracks.map { pwt ->
                Playlist(
                    id = pwt.playlist.id,
                    name = pwt.playlist.name,
                    description = pwt.playlist.description,
                    coverPath = pwt.playlist.coverPath,
                    trackCount = pwt.tracks.size
                )
            }
        }
    }

    override suspend fun addTrackToPlaylistWithTrack(playlist: Playlist, track: TrackItem) {
        trackInPlaylistDao.insertTrack(
            TrackInPlaylistEntity(
                playlistId = playlist.id,
                trackId = track.trackId,
                trackName = track.trackName,
                artistName = track.artistName,
                trackTimeMillis = track.trackTimeMillis,
                artworkUrl100 = track.artworkUrl100,
                collectionName = track.collectionName,
                releaseDate = track.releaseDate,
                primaryGenreName = track.primaryGenreName,
                country = track.country,
                previewUrl = track.previewUrl
            )
        )
    }

    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return trackInPlaylistDao.isTrackInPlaylist(playlistId, trackId) > 0
    }

    override suspend fun getPlaylistWithTracks(playlistId: Long): PlaylistWithTracks? {
        return playlistDao.getPlaylistWithTracks(playlistId)
    }
     override suspend fun getPlaylistById(playlistId: Long): Playlist? =
        playlistDao.getPlaylistById(playlistId)?.let { entity ->
            Playlist(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                coverPath = entity.coverPath,
                trackCount = 0
            )
        }
    override suspend fun deleteTrackFromPlaylist(
        playlistId: Long,
        trackId: Long
    ) {
        trackInPlaylistDao.removeTrackFromPlaylist(playlistId, trackId)
    }
    override suspend fun getTracksByIds(
        trackIds: List<Long>
    ): List<TrackInPlaylistEntity> {
        if (trackIds.isEmpty()) return emptyList()
        return trackInPlaylistDao.getTracksByIds(trackIds)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }
}

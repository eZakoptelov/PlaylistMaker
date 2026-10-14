package com.example.playlistmaker.playlists.data.mapper

import com.example.playlistmaker.playlists.data.db.entity.PlaylistEntity
import com.example.playlistmaker.playlists.data.db.entity.TrackInPlaylistEntity
import com.example.playlistmaker.playlists.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.TrackItem

object PlaylistMapper {

    fun mapToTrackItem(entity: TrackInPlaylistEntity): TrackItem {
        return TrackItem(
            trackName = entity.trackName,
            artistName = entity.artistName,
            trackTimeMillis = entity.trackTimeMillis,
            artworkUrl100 = entity.artworkUrl100,
            trackId = entity.trackId,
            collectionName = entity.collectionName,
            releaseDate = entity.releaseDate,
            primaryGenreName = entity.primaryGenreName,
            country = entity.country,
            previewUrl = entity.previewUrl,
            isFavorite = false,
            addedAt = entity.addedAt
        )
    }

    fun mapToPlaylist(entity: PlaylistEntity): Playlist {
        return Playlist(
            id = entity.id,
            name = entity.name,
            description = entity.description ,
            coverPath = entity.coverPath ,
            trackCount = 0
        )
    }
}

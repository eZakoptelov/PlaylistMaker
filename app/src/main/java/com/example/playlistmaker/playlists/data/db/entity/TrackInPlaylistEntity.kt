package com.example.playlistmaker.playlists.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_in_playlist_table")
data class TrackInPlaylistEntity(
    @PrimaryKey
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String,
    val country: String,
    val previewUrl: String?,
    val addedAt: Long = System.currentTimeMillis()
)

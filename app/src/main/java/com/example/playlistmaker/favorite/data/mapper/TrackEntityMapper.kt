package com.example.playlistmaker.favorite.data.mapper

import com.example.playlistmaker.favorite.data.TrackEntity
import com.example.playlistmaker.search.domain.model.TrackItem

fun TrackEntity.toDomain(): TrackItem = TrackItem(
    trackName = trackName,
    artistName = artistName,
    trackTimeMillis = trackTimeMillis,
    artworkUrl100 = artworkUrl100,
    trackId = trackId,
    collectionName = collectionName,
    releaseDate = releaseDate,
    primaryGenreName = primaryGenreName,
    country = country,
    previewUrl = previewUrl,
    isFavorite = true,
    addedAt = addedAt
)

fun TrackItem.toEntity(): TrackEntity = TrackEntity(
    trackId = trackId,
    trackName = trackName,
    artistName = artistName,
    trackTimeMillis = trackTimeMillis,
    artworkUrl100 = artworkUrl100,
    collectionName = collectionName,
    releaseDate = releaseDate,
    primaryGenreName = primaryGenreName,
    country = country,
    previewUrl = previewUrl,
    addedAt = System.currentTimeMillis()
)

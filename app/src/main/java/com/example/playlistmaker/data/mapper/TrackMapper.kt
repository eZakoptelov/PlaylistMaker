package com.example.playlistmaker.data.mapper

import com.example.playlistmaker.data.dto.TrackItemDto
import com.example.playlistmaker.data.dto.SearchResponseDto
import com.example.playlistmaker.domain.model.TrackItem
import com.example.playlistmaker.domain.model.SearchResponse

class TrackMapper {
    fun toDomain(dto: TrackItemDto): TrackItem = TrackItem(
        trackName = dto.trackName,
        artistName = dto.artistName,
        trackTimeMillis = dto.trackTimeMillis,
        artworkUrl100 = dto.artworkUrl100,
        trackId = dto.trackId,
        collectionName = dto.collectionName,
        releaseDate = dto.releaseDate,
        primaryGenreName = dto.primaryGenreName,
        country = dto.country,
        previewUrl = dto.previewUrl
    )

    fun toDomain(dto: SearchResponseDto): SearchResponse = SearchResponse(
        resultCount = dto.resultCount,
        results = dto.results.map { toDomain(it) }
    )
}

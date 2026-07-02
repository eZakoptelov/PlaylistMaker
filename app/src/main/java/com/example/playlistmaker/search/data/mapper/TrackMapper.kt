package com.example.playlistmaker.search.data.mapper

import com.example.playlistmaker.search.data.dto.SearchResponseDto
import com.example.playlistmaker.search.data.dto.TrackItemDto
import com.example.playlistmaker.search.domain.model.SearchResponse
import com.example.playlistmaker.search.domain.model.TrackItem

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
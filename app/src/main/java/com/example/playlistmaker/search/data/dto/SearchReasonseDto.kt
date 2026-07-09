package com.example.playlistmaker.search.data.dto

data class SearchResponseDto(
    val resultCount: Int,
    val results: List<TrackItemDto>
)
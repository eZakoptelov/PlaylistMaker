package com.example.playlistmaker.data.dto

data class SearchResponseDto(
    val resultCount: Int,
    val results: List<TrackItemDto>
)
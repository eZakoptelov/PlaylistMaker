package com.example.playlistmaker.domain.model
data class SearchResponse(
    val resultCount: Int,
    val results: List<TrackItem>
)
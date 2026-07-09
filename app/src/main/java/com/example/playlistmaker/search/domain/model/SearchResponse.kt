package com.example.playlistmaker.search.domain.model

data class SearchResponse(
    val resultCount: Int,
    val results: List<TrackItem>
)
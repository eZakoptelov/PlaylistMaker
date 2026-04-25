package com.example.playlistmaker


data class TrackItem(
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String,
    val trackId: Long
)

data class SearchResponse(
    val resultCount: Int,
    val results: List<TrackItem>
)
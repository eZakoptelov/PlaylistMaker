package com.example.playlistmaker

data class SearchQuery(
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

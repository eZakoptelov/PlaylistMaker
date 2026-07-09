package com.example.playlistmaker.search.data.repository

import com.example.playlistmaker.search.domain.model.TrackItem

interface SearchRepository {
    fun searchTracks(query: String, onResult: (Result<List<TrackItem>>) -> Unit)
    fun getSearchHistory(): List<TrackItem>
    fun addToHistory(track: TrackItem)
    fun clearHistory()
}

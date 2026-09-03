package com.example.playlistmaker.search.domain.repository

import com.example.playlistmaker.search.domain.model.TrackItem
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun searchTracks(query: String): Flow<Result<List<TrackItem>>>
    fun getSearchHistory(): List<TrackItem>
    fun addToHistory(track: TrackItem)
    fun clearHistory()
}

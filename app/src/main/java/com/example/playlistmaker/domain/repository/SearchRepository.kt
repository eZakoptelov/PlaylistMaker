package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.model.TrackItem


interface SearchRepository {
    fun searchTracks(query: String, callback: (List<TrackItem>?, Throwable?) -> Unit)
    fun getSearchHistory(): List<TrackItem>
    fun addToHistory(track: TrackItem)
    fun clearHistory()
}
package com.example.playlistmaker.search.data.repository

import com.example.playlistmaker.search.data.storage.HistoryStorage
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.utils.Constants

class SearchHistory(private val storage: HistoryStorage) {

    fun getHistory(): List<TrackItem> {
        return storage.loadHistory()
    }

    fun addToHistory(track: TrackItem) {
        val currentHistory = getHistory().toMutableList()

        currentHistory.removeIf { it.trackId == track.trackId }
        currentHistory.add(0, track)

        if (currentHistory.size > Constants.MAX_HISTORY_SIZE) {
            currentHistory.removeAt(currentHistory.lastIndex)
        }

        storage.saveHistory(currentHistory)
    }

    fun clearHistory() {
        storage.saveHistory(emptyList())
    }
}
package com.example.playlistmaker.search.data.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.utils.Constants
import com.google.gson.Gson

class HistoryStorage(private val sharedPreferences: SharedPreferences,
    private val gson: Gson){
    fun getHistory(): List<TrackItem> = loadHistory()

    fun addToHistory(track: TrackItem) {
        val currentHistory = loadHistory().toMutableList()

        currentHistory.removeAll { it.trackId == track.trackId }
        currentHistory.add(0, track)
        if (currentHistory.size > Constants.MAX_HISTORY_SIZE) {
            currentHistory.removeAt(currentHistory.lastIndex)
        }
        saveHistory(currentHistory)
    }
    fun clearHistory() {
        sharedPreferences.edit { clear() }
    }
    private fun saveHistory(history: List<TrackItem>) {
        sharedPreferences.edit {
            val json = gson.toJson(history)
            putString(Constants.HISTORY_KEY, json)
        }
    }

    private fun loadHistory(): List<TrackItem> {
        val json = sharedPreferences.getString(Constants.HISTORY_KEY, null)
        return if (json != null) {
            gson.fromJson(json, Array<TrackItem>::class.java).toList()
        } else {
            emptyList()
        }
    }
}

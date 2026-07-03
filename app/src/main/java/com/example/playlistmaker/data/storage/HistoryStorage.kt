package com.example.playlistmaker.data.storage

import android.content.SharedPreferences
import com.google.gson.Gson
import com.example.playlistmaker.domain.model.TrackItem
import com.example.playlistmaker.utils.Constants
import androidx.core.content.edit

class HistoryStorage(private val sharedPreferences: SharedPreferences) {
    private val gson = Gson()

    fun saveHistory(history: List<TrackItem>) {
        sharedPreferences.edit {
            val json = gson.toJson(history)
            putString(Constants.HISTORY_KEY, json)
        }
    }

    fun loadHistory(): List<TrackItem> {
        val json = sharedPreferences.getString(Constants.HISTORY_KEY, null)
        return if (json != null) {
            gson.fromJson(json, Array<TrackItem>::class.java).toList()
        } else {
            emptyList()
        }
    }
}

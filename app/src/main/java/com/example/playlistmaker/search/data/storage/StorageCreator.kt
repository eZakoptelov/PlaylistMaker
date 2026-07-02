package com.example.playlistmaker.search.data.storage

import android.content.SharedPreferences

class StorageCreator(private val sharedPreferences: SharedPreferences) {
    fun createHistoryStorage(): HistoryStorage {
        return HistoryStorage(sharedPreferences)
    }
}
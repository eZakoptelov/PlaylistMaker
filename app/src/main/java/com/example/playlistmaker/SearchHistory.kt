package com.example.playlistmaker

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import androidx.core.content.edit

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    private val gson = Gson()
    private var cachedHistory: MutableList<TrackItem>? = null


    fun getHistory(): List<TrackItem> {
        // Проверяем кэш
        cachedHistory?.let { return it }

        val json = sharedPreferences.getString(HISTORY_KEY, null)
        return try {
            if (json != null) {
                val history = gson.fromJson(json, Array<TrackItem>::class.java).toList().toMutableList()
                // Обрезаем до MAX_HISTORY_SIZE, если нужно
                if (history.size > MAX_HISTORY_SIZE) {
                    history.subList(0, MAX_HISTORY_SIZE).toMutableList().also { cachedHistory = it }
                } else {
                    cachedHistory = history
                }
                history
            } else {
                emptyList<TrackItem>().also { cachedHistory = it.toMutableList() }
            }
        } catch (e: Exception) {
            Log.e("SEARCH_HISTORY", "Ошибка загрузки истории: ${e.message}")
            emptyList<TrackItem>().also { cachedHistory = it.toMutableList() }
        }
    }

    fun addToHistory(track: TrackItem) {
        // Валидация трека
        if (track.trackName.isBlank()) {
            Log.w("SEARCH_HISTORY", "Попытка добавить трек с пустым названием")
            return
        }

        val currentHistory = getHistory().toMutableList()

        // Удаляем дубликаты по trackId
        currentHistory.removeIf { it.trackId == track.trackId }

        // Добавляем новый трек в начало
        currentHistory.add(0, track)

        // Ограничиваем размер истории
        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.removeAt(currentHistory.lastIndex)
        }

        // Обновляем кэш
        cachedHistory = currentHistory

        // Сохраняем в SharedPreferences
        val updatedJson = gson.toJson(currentHistory)
        sharedPreferences.edit {
            putString(HISTORY_KEY, updatedJson)
        }
    }

    fun clearHistory() {
        sharedPreferences.edit {
            remove(HISTORY_KEY)
        }
        cachedHistory = emptyList<TrackItem>().toMutableList()
    }

    companion object {
        private const val HISTORY_KEY = "search_query_history"
        private const val MAX_HISTORY_SIZE = 10
    }
}



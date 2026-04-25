package com.example.playlistmaker

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import androidx.core.content.edit

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    private val gson = Gson()
    private var cachedHistory: MutableList<TrackItem>? = null

    fun getHistory(): List<TrackItem> {
        cachedHistory?.let { return it }

        val json = sharedPreferences.getString(HISTORY_KEY, null)
        return try {
            if (json != null) {
                val history = gson.fromJson(json, Array<TrackItem>::class.java).toList().toMutableList()
                cachedHistory = history
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
        val currentHistory = getHistory().toMutableList()

        // Удаляем существующую запись, если трек уже есть в истории
        currentHistory.removeIf { it.trackId == track.trackId }

        // Добавляем трек в начало списка
        currentHistory.add(0, track)

        // Ограничиваем размер истории до 10 элементов
        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.removeAt(currentHistory.lastIndex)
        }

        cachedHistory = currentHistory

        // --- ИСПРАВЛЕННЫЙ БЛОК СОХРАНЕНИЯ ---
        val editor = sharedPreferences.edit()
        val updatedJson = gson.toJson(currentHistory)
        editor.putString(HISTORY_KEY, updatedJson)

        // commit() возвращает boolean (успех/неудача), что полезно для отладки
        val isSaved = editor.commit()
        Log.d("SEARCH_HISTORY", "Трек добавлен в историю. Сохранено успешно: $isSaved")
    }

    fun clearHistory() {
        // Здесь также лучше использовать явный commit для надежности
        val editor = sharedPreferences.edit()
        editor.remove(HISTORY_KEY)
        editor.commit()

        cachedHistory = emptyList<TrackItem>().toMutableList()
    }

    companion object {
        private const val HISTORY_KEY = "search_track_history"
        private const val MAX_HISTORY_SIZE = 10
    }
}


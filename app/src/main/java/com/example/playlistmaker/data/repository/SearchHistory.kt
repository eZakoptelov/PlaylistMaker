package com.example.playlistmaker.data.repository

import android.util.Log
import com.example.playlistmaker.data.storage.HistoryStorage
import com.example.playlistmaker.domain.model.TrackItem
import com.example.playlistmaker.utils.Constants.MAX_HISTORY_SIZE

class SearchHistory(private val storage: HistoryStorage) {

    private var cachedHistory: MutableList<TrackItem>? = null

    private fun loadFromStorage(): List<TrackItem> {
        val loadedHistory = storage.loadHistory()
        cachedHistory = loadedHistory.toMutableList()
        return loadedHistory
    }

    fun getHistory(): List<TrackItem> {
        return cachedHistory?.let { it.toList() } ?: loadFromStorage()
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

        // Сохраняем обновлённую историю в хранилище
        try {
            storage.saveHistory(currentHistory)
            Log.d("SEARCH_HISTORY", "Трек добавлен в историю и сохранён в хранилище")
        } catch (e: Exception) {
            Log.e("SEARCH_HISTORY", "Ошибка сохранения истории: ${e.message}")
        }
    }

    fun clearHistory() {
        try {
            // Очищаем хранилище
            storage.saveHistory(emptyList())
            // Обновляем кэш
            cachedHistory = mutableListOf()
            Log.d("SEARCH_HISTORY", "История поиска очищена")
        } catch (e: Exception) {
            Log.e("SEARCH_HISTORY", "Ошибка очистки истории: ${e.message}")
        }
    }
}

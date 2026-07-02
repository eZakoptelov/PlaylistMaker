package com.example.playlistmaker.search.domain.interactor

import com.example.playlistmaker.search.data.repository.SearchRepository
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.search.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.AddToHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.ClearHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.SearchUseCase

class SearchInteractor(
    private val repository: SearchRepository
) : SearchUseCase, GetSearchHistoryUseCase, AddToHistoryUseCase, ClearHistoryUseCase {

    override fun search(query: String, onResult: (Result<List<TrackItem>>) -> Unit) {
        repository.searchTracks(query, onResult)
    }
    override fun getHistory(): List<TrackItem> {
        return repository.getSearchHistory()
    }

    override fun addTrack(track: TrackItem) {
        repository.addToHistory(track)
    }

    override fun clearHistory() {
        repository.clearHistory()
    }
}

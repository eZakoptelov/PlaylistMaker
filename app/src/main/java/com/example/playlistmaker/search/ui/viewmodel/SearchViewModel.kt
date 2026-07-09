package com.example.playlistmaker.search.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.search.domain.usecase.AddToHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.ClearHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.SearchUseCase

class SearchViewModel(
    private val searchUseCase: SearchUseCase,
    private val getHistoryUseCase: GetSearchHistoryUseCase,
    private val addToHistoryUseCase: AddToHistoryUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<SearchUiState>(SearchUiState.Idle)
    val uiState: LiveData<SearchUiState> = _uiState

    fun search(query: String) {
        if (query.isBlank()) {
            _uiState.value = SearchUiState.History(getHistoryUseCase.getHistory())
            return
        }

        _uiState.value = SearchUiState.Loading

        searchUseCase.search(query) { result ->
            if (result.isSuccess) {
                _uiState.value = SearchUiState.Success(result.getOrNull() ?: emptyList())
            } else {
                val message = result.exceptionOrNull()?.message ?: "Ошибка поиска"
                _uiState.value = SearchUiState.Error(message)
            }
        }
    }


    fun addToHistory(track: TrackItem) {
        addToHistoryUseCase.addTrack(track)
    }

    fun clearHistory() {
        clearHistoryUseCase.clearHistory()
        _uiState.value = SearchUiState.History(emptyList())
    }

    fun getInitialHistory() {
        _uiState.value = SearchUiState.History(getHistoryUseCase.getHistory())
    }
}
package com.example.playlistmaker.search.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.search.domain.usecase.AddToHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.ClearHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.SearchUseCase
import com.example.playlistmaker.utils.Constants
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class SearchViewModel(
    private val searchUseCase: SearchUseCase,
    private val getHistoryUseCase: GetSearchHistoryUseCase,
    private val addToHistoryUseCase: AddToHistoryUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData<SearchUiState>(SearchUiState.Idle)
    val uiState: LiveData<SearchUiState> = _uiState

    private var searchJob: Job? = null

    // Отложенный поиск (при вводе текста)
    fun searchDebounce(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(Constants.SEARCH_DEBOUNCE_DELAY.milliseconds)
            performSearch(query)
        }
    }

    // Мгновенный поиск (при нажатии «Готово» или кнопки повтора)
    fun searchImmediately(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            performSearch(query)
        }
    }

    private suspend fun performSearch(query: String) {
        if (query.isBlank()) {
            _uiState.value = SearchUiState.History(getHistoryUseCase.getHistory())
            return
        }

        _uiState.value = SearchUiState.Loading

        searchUseCase.search(query).collect { result ->
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
        searchJob?.cancel()
        _uiState.value = SearchUiState.History(getHistoryUseCase.getHistory())
    }
}
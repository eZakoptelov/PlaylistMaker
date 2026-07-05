package com.example.playlistmaker.search.ui.viewmodel
import com.example.playlistmaker.search.domain.model.TrackItem

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val tracks: List<TrackItem>) : SearchUiState
    data class Error(val message: String) : SearchUiState
    data class History(val history: List<TrackItem>) : SearchUiState
}

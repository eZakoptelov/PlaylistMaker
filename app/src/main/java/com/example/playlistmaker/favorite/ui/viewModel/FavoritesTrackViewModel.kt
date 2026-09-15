package com.example.playlistmaker.favorite.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.favorite.domain.interactor.FavoriteTracksInteractor
import kotlinx.coroutines.launch

class FavoritesTrackViewModel(
    private val favoriteInteractor: FavoriteTracksInteractor
) : ViewModel() {

    private val _state = MutableLiveData<FavoritesState>(FavoritesState.Empty)
    val state: LiveData<FavoritesState> get() = _state

    init {
        viewModelScope.launch {
            favoriteInteractor.getFavoriteTracks().collect { tracks ->
                _state.value = if (tracks.isEmpty()) {
                    FavoritesState.Empty
                } else {
                    FavoritesState.Content(tracks)
                }
            }
        }
    }
}
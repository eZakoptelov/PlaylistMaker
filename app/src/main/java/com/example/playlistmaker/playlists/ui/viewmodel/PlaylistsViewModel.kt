package com.example.playlistmaker.playlists.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.playlists.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlists.domain.model.Playlist
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val interactor: PlaylistInteractor
) : ViewModel() {

    private val _playlistsState = MutableLiveData<List<Playlist>>()
    val playlistsState: LiveData<List<Playlist>> = _playlistsState

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            interactor.getPlaylists().collect { playlists ->
                _playlistsState.postValue(playlists)
            }
        }
    }
}

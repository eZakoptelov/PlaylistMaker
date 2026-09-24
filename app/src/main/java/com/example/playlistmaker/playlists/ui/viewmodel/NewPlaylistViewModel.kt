package com.example.playlistmaker.playlists.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.playlists.domain.interactor.PlaylistInteractor
import kotlinx.coroutines.launch

class NewPlaylistViewModel(
    private val interactor: PlaylistInteractor
) : ViewModel() {

    fun createPlaylist(name: String, description: String, coverPath: String?) {
        viewModelScope.launch {
            interactor.createPlaylist(
                name = name,
                description = description,
                coverPath = coverPath.orEmpty()
            )
        }
    }
}
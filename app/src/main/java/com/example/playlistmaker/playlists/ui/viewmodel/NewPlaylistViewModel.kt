package com.example.playlistmaker.playlists.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.playlists.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlists.domain.model.Playlist
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class NewPlaylistViewModel(
    private val interactor: PlaylistInteractor
) : ViewModel() {

    private val _playlistCreated = MutableSharedFlow<String>()
    val playlistCreated = _playlistCreated.asSharedFlow()

    private val _playlistLoaded = MutableSharedFlow<Playlist>()
    val playlistLoaded = _playlistLoaded.asSharedFlow()

    private val _playlistUpdated = MutableSharedFlow<Boolean>()
    val playlistUpdated = _playlistUpdated.asSharedFlow()

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                val playlist = interactor.getPlaylistById(playlistId)
                if (playlist != null) {
                    _playlistLoaded.emit(playlist)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun createPlaylist(name: String, description: String, coverPath: String?) {
        viewModelScope.launch {
            try {
                interactor.createPlaylist(
                    name = name,
                    description = description,
                    coverPath = coverPath.orEmpty()
                )
                _playlistCreated.emit(name)
            } catch (_: Exception) {
                _playlistCreated.emit("")
            }
        }
    }

    fun updatePlaylist(
        playlistId: Long,
        name: String,
        description: String,
        coverPath: String
    ) {
        viewModelScope.launch {
            try {
                val existingPlaylist = interactor.getPlaylistById(playlistId)

                if (existingPlaylist == null) {
                    _playlistUpdated.emit(false)
                    return@launch
                }

                interactor.updatePlaylist(
                    existingPlaylist.copy(
                        name = name,
                        description = description,
                        coverPath = coverPath
                    )
                )

                _playlistUpdated.emit(true)
            } catch (_: Exception) {
                _playlistUpdated.emit(false)
            }
        }
    }
}
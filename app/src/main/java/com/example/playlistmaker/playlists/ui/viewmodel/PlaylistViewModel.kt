package com.example.playlistmaker.playlists.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.playlists.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlists.domain.model.PlaylistDetailsResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val interactor: PlaylistInteractor
) : ViewModel() {

    private val _playlistDetails =
        MutableLiveData<PlaylistDetailsResult>()

    val playlistDetails: LiveData<PlaylistDetailsResult> =
        _playlistDetails

    private val _playlistDeleted = MutableSharedFlow<Unit>()
    val playlistDeleted = _playlistDeleted.asSharedFlow()

    private val _deletePlaylistError = MutableLiveData<String>()
    val deletePlaylistError: LiveData<String> = _deletePlaylistError

    fun loadPlaylistDetails(playlistId: Long) {
        viewModelScope.launch {
            try {
                _playlistDetails.value =
                    interactor.getPlaylistDetails(playlistId)
            } catch (e: Exception) {
                _playlistDetails.value =
                    PlaylistDetailsResult.Error(
                        e.message ?: "Не удалось загрузить плейлист"
                    )
            }
        }
    }

    fun deleteTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            try {
                interactor.deleteTrackFromPlaylist(playlistId, trackId)

                _playlistDetails.value =
                    interactor.getPlaylistDetails(playlistId)
            } catch (e: Exception) {
                _playlistDetails.value =
                    PlaylistDetailsResult.Error(
                        e.message ?: "Не удалось удалить трек"
                    )
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                interactor.deletePlaylist(playlistId)
                _playlistDeleted.emit(Unit)
            } catch (e: Exception) {
                _deletePlaylistError.value =
                    e.message ?: "Не удалось удалить плейлист"
            }
        }
    }
}
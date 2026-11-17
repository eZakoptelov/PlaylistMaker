package com.example.playlistmaker.player.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.favorite.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.player.domain.PlayerRules
import com.example.playlistmaker.player.domain.PlayerServiceControl
import com.example.playlistmaker.playlists.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlists.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.TrackItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PlayerViewModel(
    application: Application,
    val rules: PlayerRules,
    private val favoriteInteractor: FavoriteTracksInteractor,
    private val playlistInteractor: PlaylistInteractor
) : AndroidViewModel(application) {

    private val _state = MutableLiveData<PlayerUiState>()
    val state: LiveData<PlayerUiState> = _state
    private val _isFavorite = MutableLiveData(false)
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _playlistsState = MutableLiveData<List<Playlist>>()
    val playlistsState: LiveData<List<Playlist>> = _playlistsState

    private val _addTrackStatus = MutableLiveData<String>()
    val addTrackStatus: LiveData<String> = _addTrackStatus

    private var currentTrack: TrackItem? = null
    private var playerService: PlayerServiceControl? = null
    private var serviceStateJob: Job? = null
    private var playlistsJob: Job? = null
    private var pendingPlay = false

    fun attachPlayerService(service: PlayerServiceControl) {
        playerService = service

        serviceStateJob?.cancel()
        serviceStateJob = viewModelScope.launch {
            service.getPlaybackState().collect { playback ->
                val currentState = _state.value ?: return@collect

                if (
                    playback.trackId != null &&
                    currentState.track?.trackId != playback.trackId
                ) {
                    return@collect
                }

                val playbackError = playback.error?.let { message ->
                    getApplication<Application>().getString(
                        R.string.playback_error,
                        message
                    )
                }

                _state.value = currentState.copy(
                    isPlaying = playback.isPlaying,
                    isReady = playback.isReady,
                    currentPosition = playback.currentPosition,
                    duration = if (playback.duration > 0L) {
                        playback.duration
                    } else {
                        currentState.duration
                    },
                    error = playbackError
                )
            }
        }

        if (pendingPlay) {
            pendingPlay = false
            service.play()
        }
    }

    fun detachPlayerService() {
        serviceStateJob?.cancel()
        serviceStateJob = null
        playerService = null
    }

    fun loadPlaylists() {
        playlistsJob?.cancel()
        playlistsJob = viewModelScope.launch {
            playlistInteractor.getPlaylists().collect { playlists ->
                _playlistsState.postValue(playlists)
            }
        }
    }

    fun addTrackToPlaylist(playlist: Playlist) {
        val track = currentTrack ?: return

        viewModelScope.launch {
            if (playlistInteractor.isTrackInPlaylist(playlist.id, track.trackId)) {
                _addTrackStatus.postValue(
                    getApplication<Application>().getString(
                        R.string.track_already_in_playlist,
                        playlist.name
                    )
                )
            } else {
                playlistInteractor.addTrackToPlaylist(playlist, track)
                _addTrackStatus.postValue(
                    getApplication<Application>().getString(
                        R.string.track_added_to_playlist,
                        playlist.name
                    )
                )
            }
        }
    }

    fun setTrack(track: TrackItem) {
        currentTrack = track
        _isFavorite.value = track.isFavorite

        _state.value = PlayerUiState(
            track = track,
            isPlaying = false,
            isReady = false,
            currentPosition = 0L,
            duration = track.trackTimeMillis,
            error = null
        )
    }

    fun onFavoriteClicked() {
        val track = currentTrack ?: return
        val newFavorite = !track.isFavorite

        viewModelScope.launch {
            if (newFavorite) {
                favoriteInteractor.addTrack(
                    track.copy(
                        isFavorite = true,
                        addedAt = System.currentTimeMillis()
                    )
                )
            } else {
                favoriteInteractor.deleteTrack(track)
            }

            currentTrack = track.copy(isFavorite = newFavorite)
            _isFavorite.value = newFavorite
        }
    }

    fun loadTrack(track: TrackItem) {
        val isSameTrack = currentTrack?.trackId == track.trackId

        if (!isSameTrack || _state.value == null) {
            setTrack(track)
        } else {
            currentTrack = track
            _state.value = _state.value?.copy(track = track)
        }

        viewModelScope.launch {
            val favoriteTracks = favoriteInteractor.getFavoriteTracks().first()
            val isFav = favoriteTracks.any { it.trackId == track.trackId }

            currentTrack = currentTrack?.copy(isFavorite = isFav)
            _isFavorite.value = isFav
        }

        if (track.previewUrl.isNullOrEmpty()) {
            _state.value = _state.value?.copy(
                error = getApplication<Application>().getString(
                    R.string.no_preview_url
                )
            )
            return
        }

    }

    fun play() {
        val service = playerService
        if (service == null) {
            pendingPlay = true
            return
        }

        service.play()
    }

    fun pause() {
        pendingPlay = false
        playerService?.pause()
    }

    fun stop() {
        pendingPlay = false

        val service = playerService
        if (service != null) {
            service.stopPlayback()
        } else {
            _state.value = _state.value?.copy(
                isPlaying = false,
                isReady = false,
                currentPosition = 0L
            )
        }
    }

    fun onAppBackgrounded() {
        playerService?.showForegroundNotification()
    }

    fun onAppForegrounded() {
        playerService?.hideForegroundNotification()
    }

    override fun onCleared() {
        serviceStateJob?.cancel()
        playlistsJob?.cancel()
        super.onCleared()
    }
}

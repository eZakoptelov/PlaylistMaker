package com.example.playlistmaker.player.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.favorite.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.player.domain.PlayerRules
import com.example.playlistmaker.playlists.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlists.domain.model.Playlist
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class PlayerViewModel(
    application: Application,
    private val interactor: PlayerInteractor,
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
    private var progressJob: Job? = null
    private var playlistsJob: Job? = null
    private var pendingPlay = false

    init {
        interactor.setOnCompletionListener { onTrackFinished() }

        interactor.setPreparedListener {
            _state.value = _state.value?.copy(
                isPlaying = false,
                isReady = true
            )
            if (pendingPlay) {
                pendingPlay = false
                play()
            }
        }
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
                    getApplication<Application>().getString(R.string.track_already_in_playlist, playlist.name)
                )
            } else {
                playlistInteractor.addTrackToPlaylist(playlist, track)
                _addTrackStatus.postValue(
                    getApplication<Application>().getString(R.string.track_added_to_playlist, playlist.name)
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
            currentPosition = 0,
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
                    track.copy(isFavorite = true, addedAt = System.currentTimeMillis())
                )
            } else {
                favoriteInteractor.deleteTrack(track)
            }
            currentTrack = track.copy(isFavorite = newFavorite)
            _isFavorite.value = newFavorite
        }
    }

    fun loadTrack(track: TrackItem) {
        setTrack(track)

        viewModelScope.launch {
            val favoriteTracks = favoriteInteractor.getFavoriteTracks().first()
            val isFav = favoriteTracks.any { it.trackId == track.trackId }
            currentTrack = track.copy(isFavorite = isFav)
            _isFavorite.value = isFav
        }

        if (track.previewUrl.isNullOrEmpty()) {
            _state.value = _state.value?.copy(
                error = getApplication<Application>().getString(R.string.no_preview_url)
            )
            return
        }

        interactor.load(
            url = track.previewUrl,
            onError = { e ->
                _state.value = _state.value?.copy(
                    isPlaying = false,
                    isReady = false,
                    error = getApplication<Application>().getString(
                        R.string.playback_error,
                        e.message ?: getApplication<Application>().getString(R.string.unknown_error)
                    )
                )
            }
        )
    }

    fun play() {
        val isReady = _state.value?.isReady == true

        if (!isReady) {
            pendingPlay = true
            currentTrack?.let { track ->
                if (!track.previewUrl.isNullOrEmpty()) {
                    interactor.load(
                        url = track.previewUrl,
                        onError = { e ->
                            pendingPlay = false
                            _state.value = _state.value?.copy(
                                isPlaying = false,
                                isReady = false,
                                error = getApplication<Application>().getString(
                                    R.string.playback_error,
                                    e.message ?: getApplication<Application>().getString(R.string.unknown_error)
                                )
                            )
                        }
                    )
                }
            }
            return
        }

        interactor.play()
        _state.value = _state.value?.copy(isPlaying = true)
        startProgressUpdate()
    }

    fun pause() {
        val isPlaying = _state.value?.isPlaying == true
        if (!isPlaying) return

        interactor.pause()
        _state.value = _state.value?.copy(isPlaying = false)
        stopProgressUpdate()
    }

    fun stop() {
        interactor.stop()
        _state.value = _state.value?.copy(
            isPlaying = false,
            currentPosition = 0,
            isReady = false
        )
        stopProgressUpdate()
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (interactor.isPlaying()) {
                val position = interactor.getCurrentPosition()
                val duration = currentTrack?.trackTimeMillis ?: 0
                _state.value = _state.value?.copy(
                    currentPosition = position,
                    duration = duration
                )
                delay(Constants.UPDATE_INTERVAL_MS.milliseconds)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun onTrackFinished() {
        val currentState = _state.value ?: return
        val finishedState = rules.onTrackFinished(currentState)
        _state.value = finishedState
        stopProgressUpdate()
    }

    override fun onCleared() {
        super.onCleared()
        interactor.release()
        stopProgressUpdate()
        playlistsJob?.cancel()
    }
}

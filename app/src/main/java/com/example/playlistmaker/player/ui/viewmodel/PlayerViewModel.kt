package com.example.playlistmaker.player.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.player.domain.PlayerRules
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.milliseconds

class PlayerViewModel(
    private val interactor: PlayerInteractor,
    val rules: PlayerRules
) : ViewModel() {

    private val _state = MutableLiveData<PlayerUiState>()
    val state: LiveData<PlayerUiState> = _state

    private var currentTrack: TrackItem? = null
    private var progressJob: Job? = null
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

    fun setTrack(track: TrackItem) {
        currentTrack = track
        _state.value = PlayerUiState(
            track = track,
            isPlaying = false,
            isReady = false,
            currentPosition = 0,
            duration = track.trackTimeMillis,
            error = null
        )
    }

    fun loadTrack(track: TrackItem) {
        setTrack(track)

        if (track.previewUrl.isNullOrEmpty()) {
            _state.value = _state.value?.copy(
                error = "Нет URL для воспроизведения"
            )
            return
        }

        interactor.load(
            url = track.previewUrl!!,
            onError = { e ->
                _state.value = _state.value?.copy(
                    isPlaying = false,
                    isReady = false,
                    error = "Ошибка воспроизведения: ${e.message ?: "Неизвестная ошибка"}"
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
                        url = track.previewUrl!!,
                        onError = { e ->
                            pendingPlay = false
                            _state.value = _state.value?.copy(
                                isPlaying = false,
                                isReady = false,
                                error = "Ошибка воспроизведения: ${e.message ?: "Неизвестная ошибка"}"
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
    }
}

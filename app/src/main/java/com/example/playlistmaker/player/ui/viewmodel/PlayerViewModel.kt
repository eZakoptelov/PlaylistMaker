package com.example.playlistmaker.player.ui.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.player.domain.PlayerRules
import com.example.playlistmaker.player.domain.PlayerState
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.utils.Constants

class PlayerViewModel(
    private val interactor: PlayerInteractor,
    val rules: PlayerRules
) : ViewModel() {

    private val _state = MutableLiveData<PlayerState>()
    val state: LiveData<PlayerState> = _state

    private var currentTrack: TrackItem? = null

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            if (interactor.isPlaying()) {
                val position = interactor.getCurrentPosition()
                val duration = currentTrack?.trackTimeMillis ?: 0
                _state.value = _state.value?.copy(
                    currentPosition = position,
                    duration = duration
                )
                handler.postDelayed(this, Constants.UPDATE_INTERVAL_MS)
            } else {
                handler.removeCallbacks(this)
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())

    init {
        interactor.setOnCompletionListener { onTrackFinished() }

        interactor.setPreparedListener {
            _state.value = _state.value?.copy(
                isPlaying = false,
                isReady = true
            )
        }
    }

    fun setTrack(track: TrackItem) {
        currentTrack = track
        _state.value = PlayerState(
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
        if (!isReady) return

        interactor.play()
        _state.value = _state.value?.copy(isPlaying = true)
        handler.post(updateTimeRunnable)
    }

    fun pause() {
        val isPlaying = _state.value?.isPlaying == true
        if (!isPlaying) return

        interactor.pause()
        _state.value = _state.value?.copy(isPlaying = false)
        handler.removeCallbacks(updateTimeRunnable)
    }

    fun stop() {
        interactor.stop()
        _state.value = _state.value?.copy(
            isPlaying = false,
            currentPosition = 0
        )
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun onTrackFinished() {
        val currentState = _state.value ?: return
        val finishedState = rules.onTrackFinished(currentState)
        _state.value = finishedState
        handler.removeCallbacks(updateTimeRunnable)
    }

    override fun onCleared() {
        super.onCleared()
        interactor.release()
        handler.removeCallbacks(updateTimeRunnable)
    }
}

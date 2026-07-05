package com.example.playlistmaker.player.domain.impl

import com.example.playlistmaker.player.domain.MediaPlayerWrapper
import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.search.domain.model.TrackItem

class PlayerInteractorImpl(
    private val mediaPlayerWrapper: MediaPlayerWrapper
) : PlayerInteractor {

    private var currentTrack: TrackItem? = null
    private var preparedListener: (() -> Unit)? = null
    private var completionListener: (() -> Unit)? = null

    private var isLoading = false
    private var isTrackLoaded = false


    override fun load(url: String, onError: (Exception) -> Unit) {
        if (isLoading) {
            println("[$TAG] load() отклонён: уже идёт загрузка")
            return
        }
        isLoading = true
        isTrackLoaded = false
        println("[$TAG] PlayerInteractorImpl.load вызван, url=$url")

        mediaPlayerWrapper.load(
            url = url,
            onPrepared = {
                println("[$TAG] onPrepared в Interactor")
                isLoading = false
                isTrackLoaded = true
                preparedListener?.invoke()
            },
            onError = { e ->
                println("[$TAG] onError в Interactor: ${e.message}")
                isLoading = false
                isTrackLoaded = false
                onError(e)
            }
        )
    }

    override fun play() {
        if (!isTrackLoaded) {
            println("[$TAG] play() отклонён: трек ещё не загружен/подготовлен")
            return
        }
        mediaPlayerWrapper.start()
        // Не вызываем preparedListener здесь — он должен срабатывать только при onPrepared
    }

    override fun pause() {
        mediaPlayerWrapper.pause()
    }

    override fun stop() {
        mediaPlayerWrapper.stop()
        isTrackLoaded = false // Трек больше не «готов» — нужно снова подготовить
    }

    override fun release() {
        println("[$TAG] release() вызван")
        mediaPlayerWrapper.release()
        isLoading = false
        isTrackLoaded = false
        currentTrack = null
    }

    override fun getCurrentPosition(): Long = mediaPlayerWrapper.currentPosition()

    override fun isPlaying(): Boolean = mediaPlayerWrapper.isPlaying()

    override fun setOnCompletionListener(listener: () -> Unit) {
        completionListener = listener
        mediaPlayerWrapper.setOnCompletionListener {
            completionListener?.invoke()
        }
    }

    override fun setPreparedListener(listener: () -> Unit) {
        preparedListener = listener
    }
    companion object {
        private const val TAG = "PlayerDebug"
    }
}

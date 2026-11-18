package com.example.playlistmaker.player.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.main.ui.activity.MainActivity
import com.example.playlistmaker.player.data.LocalMediaPlayerImpl
import com.example.playlistmaker.player.domain.PlayerPlaybackState
import com.example.playlistmaker.player.domain.PlayerServiceControl
import com.example.playlistmaker.player.domain.impl.PlayerInteractorImpl
import com.example.playlistmaker.player.domain.intaractor.PlayerInteractor
import com.example.playlistmaker.utils.Constants
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerService : Service(), PlayerServiceControl {

    inner class LocalBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
    }

    private val binder = LocalBinder()

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    private lateinit var interactor: PlayerInteractor

    private val _playbackState = MutableStateFlow(PlayerPlaybackState())

    override fun getPlaybackState(): StateFlow<PlayerPlaybackState> =
        _playbackState.asStateFlow()

    private var progressJob: Job? = null
    private var pendingPlay = false
    private var isTrackCompleted = false
    private var isAppInBackground = false
    private var currentTrackUrl: String = ""
    private var currentArtistName: String = ""
    private var currentTrackName: String = ""
    private var currentTrackId: Long = Constants.INVALID_TRACK_ID
    private var currentTrackDuration: Long = 0L

    private var isForeground = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        interactor = PlayerInteractorImpl(
            LocalMediaPlayerImpl(MediaPlayer())
        )

        interactor.setPreparedListener {
            _playbackState.value = _playbackState.value.copy(
                isLoading = false,
                isReady = true,
                isPlaying = false,
                currentPosition = 0L,
                error = null
            )

            if (pendingPlay) {
                pendingPlay = false
                play()
            }
        }

        interactor.setOnCompletionListener {
            onTrackCompleted()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        currentTrackUrl = intent.getStringExtra(EXTRA_TRACK_URL).orEmpty()
        currentArtistName = intent.getStringExtra(EXTRA_ARTIST_NAME).orEmpty()
        currentTrackName = intent.getStringExtra(EXTRA_TRACK_NAME).orEmpty()
        currentTrackId = intent.getLongExtra(EXTRA_TRACK_ID, Constants.INVALID_TRACK_ID)
        currentTrackDuration = intent.getLongExtra(EXTRA_TRACK_DURATION, 0L)
        isTrackCompleted = false
        isAppInBackground = false

        if (currentTrackUrl.isNotBlank() && currentTrackId != Constants.INVALID_TRACK_ID) {
            prepareTrackFromBoundIntent()
        } else {
            _playbackState.value = PlayerPlaybackState(
                error = "Не удалось получить данные аудиотрека"
            )
        }

        return binder
    }

    private fun prepareTrackFromBoundIntent(autoPlay: Boolean = false) {
        pendingPlay = autoPlay
        isTrackCompleted = false
        stopProgressUpdates()

        _playbackState.value = PlayerPlaybackState(
            trackId = currentTrackId,
            isLoading = true,
            isPlaying = false,
            isReady = false,
            currentPosition = 0L,
            duration = currentTrackDuration,
            error = null
        )

        interactor.load(currentTrackUrl) { error ->
            pendingPlay = false
            _playbackState.value = _playbackState.value.copy(
                isLoading = false,
                isReady = false,
                isPlaying = false,
                currentPosition = 0L,
                error = error.message ?: "Не удалось загрузить аудио"
            )
            hideForegroundNotification()
        }
    }

    override fun play() {
        val current = _playbackState.value
        if (current.isPlaying) return

        if (isTrackCompleted) {
            prepareTrackFromBoundIntent(autoPlay = true)
            return
        }

        if (!current.isReady) {
            pendingPlay = true
            return
        }

        try {
            interactor.play()
            if (interactor.isPlaying()) {
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = true,
                    error = null
                )
                startProgressUpdates()
                updateForegroundNotification()
            }
        } catch (error: Exception) {
            Log.e(Constants.TAG, "Не удалось запустить воспроизведение", error)
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                error = error.message ?: "Не удалось запустить воспроизведение"
            )
        }
    }

    override fun pause() {
        pendingPlay = false
        if (!_playbackState.value.isPlaying) return

        try {
            interactor.pause()
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                currentPosition = interactor.getCurrentPosition()
            )
        } catch (error: Exception) {
            Log.e(Constants.TAG, "Не удалось приостановить воспроизведение", error)
        }

        stopProgressUpdates()
        hideForegroundNotification()
    }

    override fun stopPlayback() {
        pendingPlay = false
        isTrackCompleted = false
        stopProgressUpdates()

        try {
            interactor.stop()
        } catch (error: Exception) {
            Log.w(Constants.TAG, "Ошибка остановки плеера", error)
        }

        _playbackState.value = PlayerPlaybackState()
        currentTrackUrl = ""
        currentArtistName = ""
        currentTrackName = ""
        currentTrackId = Constants.INVALID_TRACK_ID
        currentTrackDuration = 0L
        hideForegroundNotification()
    }

    private fun onTrackCompleted() {
        pendingPlay = false
        isTrackCompleted = true
        stopProgressUpdates()

        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            isReady = true,
            isLoading = false,
            currentPosition = 0L
        )

        hideForegroundNotification()
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = serviceScope.launch {
            while (isActive && interactor.isPlaying()) {
                _playbackState.value = _playbackState.value.copy(
                    currentPosition = interactor.getCurrentPosition()
                )
                delay(Constants.UPDATE_INTERVAL_MS.milliseconds)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    override fun showForegroundNotification() {
        isAppInBackground = true
        updateForegroundNotification()
    }

    override fun hideForegroundNotification() {
        isAppInBackground = false
        removeForegroundNotification()
    }

    private fun updateForegroundNotification() {
        if (!isAppInBackground || !_playbackState.value.isPlaying) {
            removeForegroundNotification()
            return
        }

        if (currentArtistName.isBlank() || currentTrackName.isBlank()) return
        if (isForeground) return

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val contentIntent = PendingIntent.getActivity(
            this,
            Constants.NOTIFICATION_REQUEST_CODE,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Playlist Maker")
            .setContentText("$currentArtistName - $currentTrackName")
            .setContentIntent(contentIntent)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setAutoCancel(false)
            .build()

        try {
            ServiceCompat.startForeground(
                this,
                Constants.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
            isForeground = true
        } catch (error: Exception) {
            Log.e(Constants.TAG, "Не удалось показать foreground-уведомление", error)
        }
    }

    private fun removeForegroundNotification() {
        if (isForeground) {
            ServiceCompat.stopForeground(
                this,
                ServiceCompat.STOP_FOREGROUND_REMOVE
            )
            isForeground = false
        }

        getSystemService(NotificationManager::class.java)
            ?.cancel(Constants.NOTIFICATION_ID)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.CHANNEL_ID,
            "Воспроизведение аудио",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Уведомление о воспроизведении трека"
            setShowBadge(false)
        }

        getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        stopProgressUpdates()
        hideForegroundNotification()

        if (::interactor.isInitialized) {
            interactor.release()
        }

        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_TRACK_ID = "com.example.playlistmaker.extra.TRACK_ID"
        const val EXTRA_TRACK_URL = "com.example.playlistmaker.extra.TRACK_URL"
        const val EXTRA_ARTIST_NAME = "com.example.playlistmaker.extra.ARTIST_NAME"
        const val EXTRA_TRACK_NAME = "com.example.playlistmaker.extra.TRACK_NAME"
        const val EXTRA_TRACK_DURATION = "com.example.playlistmaker.extra.TRACK_DURATION"

    }
}

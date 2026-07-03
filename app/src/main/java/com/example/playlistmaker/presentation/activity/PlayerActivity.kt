package com.example.playlistmaker.presentation.activity

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.model.TrackItem
import com.example.playlistmaker.utils.Constants.EXTRA_TRACK
import com.example.playlistmaker.utils.Constants.UPDATE_INTERVAL_MS
import java.util.Locale

class PlayerActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var track: TrackItem
    private lateinit var backButton: Button
    private lateinit var playPauseButton: ToggleButton
    private lateinit var currentTimeText: TextView
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var searchDebounce: Runnable? = null
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            if (mediaPlayer?.isPlaying == true) {
                val currentPosition = mediaPlayer?.currentPosition ?: 0
                currentTimeText.text = formatDuration(currentPosition.toLong())
                handler.postDelayed(this, UPDATE_INTERVAL_MS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pleer_activity)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.pleerSong)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backButton = findViewById(R.id.ic_vector_buck)
        imageView = findViewById(R.id.imageViewPleerPlaceholder)
        val trackNameTv = findViewById<TextView>(R.id.trackNameTv)
        val artistNameTv = findViewById<TextView>(R.id.artistNameTv)
        val collectionNameLabel = findViewById<TextView>(R.id.collectionName)
        val collectionNameValue = findViewById<TextView>(R.id.collectionNameValue)
        val releaseDateLabel = findViewById<TextView>(R.id.releaseDate)
        val releaseDateValue = findViewById<TextView>(R.id.releaseDateValue)
        val primaryGenreValue = findViewById<TextView>(R.id.primaryGenreValue)
        val countryValue = findViewById<TextView>(R.id.countryValue)
        val trackDurationText = findViewById<TextView>(R.id.trackDurationText)
        playPauseButton = findViewById(R.id.button_pause)
        currentTimeText = findViewById(R.id.currentTimeText)
        setupPlayPauseButton()

        track = intent.getParcelableExtra(EXTRA_TRACK) ?: run {
            Toast.makeText(this, "Ошибка загрузки трека", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Установка данных
        trackNameTv.text = track.trackName
        artistNameTv.text = track.artistName
        primaryGenreValue.text = track.primaryGenreName
        countryValue.text = track.country
        trackDurationText.text = formatDuration(track.trackTimeMillis)

        // Условное отображение альбома
        if (!track.collectionName.isNullOrEmpty()) {
            collectionNameLabel.visibility = View.VISIBLE
            collectionNameValue.visibility = View.VISIBLE
            collectionNameValue.text = track.collectionName
        } else {
            collectionNameLabel.visibility = View.GONE
            collectionNameValue.visibility = View.GONE
        }

        // Условное отображение года релиза
        if (!track.releaseDate.isNullOrEmpty()) {
            releaseDateLabel.visibility = View.VISIBLE
            releaseDateValue.visibility = View.VISIBLE
            releaseDateValue.text = track.releaseDate
        } else {
            releaseDateLabel.visibility = View.GONE
            releaseDateValue.visibility = View.GONE
        }

        loadCoverImage()

        // Настройка поведения кнопки возврата
        backButton.setOnClickListener {
            navigateToSearchActivity()
        }
    }

    private fun loadCoverImage() {
        if (track.getCoverArtwork().isNotEmpty()) {
            Glide.with(this)
                .load(track.getCoverArtwork())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.Companion.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    private fun navigateToSearchActivity() {
        stopPlayback()
        val searchIntent = Intent(this, SearchActivity::class.java)
        startActivity(searchIntent)
        finish()
    }
    private fun setupPlayPauseButton() {
        playPauseButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startOrResumePlayback()
            } else {
                pausePlayback()
            }
        }
    }

    private fun startOrResumePlayback() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(track.previewUrl)
                    prepare()
                } catch (_: Exception) {
                    Toast.makeText(this@PlayerActivity, "Ошибка воспроизведения", Toast.LENGTH_SHORT).show()
                    return@apply
                }
            }
        }

        mediaPlayer?.setOnCompletionListener {
            isPlaying = false
            playPauseButton.isChecked = false
            currentTimeText.text = "00:00"
            handler.removeCallbacks(updateTimeRunnable)
        }

        mediaPlayer?.start()
        isPlaying = true
        playPauseButton.isChecked= true  // Показываем иконку паузы
        handler.post(updateTimeRunnable)
    }

    private fun pausePlayback() {
        mediaPlayer?.pause()
        isPlaying = false
        playPauseButton.isChecked = false  // Показываем иконку воспроизведения
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun stopPlayback() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        playPauseButton.isChecked = false
        currentTimeText.text = "00:00"
        handler.removeCallbacks(updateTimeRunnable)
    }
    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            pausePlayback()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
        handler.removeCallbacksAndMessages(null)
        searchDebounce = null
    }
}

package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import java.util.Locale

class PlayerActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var track: TrackItem
    private lateinit var backButton: Button

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
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    private fun navigateToSearchActivity() {
        val searchIntent = Intent(this, SearchActivity::class.java)
        startActivity(searchIntent)
        finish()
    }
    companion object {
        const val EXTRA_TRACK = "track"
    }
}


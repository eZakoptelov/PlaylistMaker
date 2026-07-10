package com.example.playlistmaker.player.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.player.ui.viewmodel.PlayerViewModel
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.search.ui.activity.SearchActivity
import com.example.playlistmaker.utils.Constants
import org.koin.androidx.viewmodel.ext.android.getViewModel

class PlayerActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var backButton: Button
    private lateinit var playPauseButton: ToggleButton
    private lateinit var currentTimeText: TextView
    private var track: TrackItem? = null

    private lateinit var viewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.player_activity)


        viewModel = getViewModel()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.playerSong)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backButton = findViewById(R.id.ic_vector_buck)
        imageView = findViewById(R.id.imageViewPlayerPlaceholder)
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

        track = intent.getParcelableExtra(Constants.EXTRA_TRACK)
        if (track == null) {
            Toast.makeText(this, "Ошибка загрузки трека", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.loadTrack(track!!)

        bindTrackData(
            trackNameTv, artistNameTv, collectionNameLabel, collectionNameValue,
            releaseDateLabel, releaseDateValue, primaryGenreValue, countryValue, trackDurationText
        )

        loadCoverImage()
        setupPlayPauseButton()

        backButton.setOnClickListener {
            viewModel.stop()
            navigateToSearchActivity()
        }

        observeState()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pause()
    }

    private fun observeState() {
        viewModel.state.observe(this) { state ->
            playPauseButton.isChecked = state.isPlaying
            currentTimeText.text = viewModel.rules.formatDuration(state.currentPosition)

            if (state.error != null) {
                Toast.makeText(this, state.error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun bindTrackData(
        trackNameTv: TextView,
        artistNameTv: TextView,
        collectionNameLabel: TextView,
        collectionNameValue: TextView,
        releaseDateLabel: TextView,
        releaseDateValue: TextView,
        primaryGenreValue: TextView,
        countryValue: TextView,
        trackDurationText: TextView
    ) {
        val t = track ?: return
        trackNameTv.text = t.trackName
        artistNameTv.text = t.artistName
        primaryGenreValue.text = t.primaryGenreName
        countryValue.text = t.country
        trackDurationText.text = viewModel.rules.formatDuration(t.trackTimeMillis)

        if (!t.collectionName.isNullOrEmpty()) {
            collectionNameLabel.visibility = View.VISIBLE
            collectionNameValue.visibility = View.VISIBLE
            collectionNameValue.text = t.collectionName
        } else {
            collectionNameLabel.visibility = View.GONE
            collectionNameValue.visibility = View.GONE
        }

        if (!t.releaseDate.isNullOrEmpty()) {
            releaseDateLabel.visibility = View.VISIBLE
            releaseDateValue.visibility = View.VISIBLE
            releaseDateValue.text = t.releaseDate
        } else {
            releaseDateLabel.visibility = View.GONE
            releaseDateValue.visibility = View.GONE
        }
    }

    private fun loadCoverImage() {
        val url = track?.getCoverArtwork()
        if (!url.isNullOrEmpty()) {
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun setupPlayPauseButton() {
        playPauseButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.play()
            } else {
                viewModel.pause()
            }
        }
    }

    private fun navigateToSearchActivity() {
        val searchIntent = Intent(this, SearchActivity::class.java)
        startActivity(searchIntent)
        finish()
    }
}

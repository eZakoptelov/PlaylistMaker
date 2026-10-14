package com.example.playlistmaker.player.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlayerBinding
import com.example.playlistmaker.playlists.ui.adapter.BottomSheetPlaylistAdapter
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.player.ui.viewmodel.PlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModel()
    private val track by lazy {
        arguments?.getParcelable<TrackItem>("track")
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback? = null
    private val bottomSheetAdapter = BottomSheetPlaylistAdapter { playlist ->
        viewModel.addTrackToPlaylist(playlist)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWindowInsets()
        setupBottomSheet()
        bindViews()

        if (track == null) {
            Toast.makeText(requireContext(), getString(R.string.track_load_error), Toast.LENGTH_SHORT).show()
            return
        }


        viewModel.loadTrack(track!!)
        bindTrackData()
        loadCoverImage()
        setupPlayPauseButton()
        setupFavoriteButton()
        setupAddToPlaylistButton()
        observeState()
        observeFavorite()
        observePlaylists()
        observeAddTrackStatus()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.playerSong) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.playlistsBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.rvPlaylistsBottomSheet.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlaylistsBottomSheet.adapter = bottomSheetAdapter

        bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlay.visibility = View.GONE
                    }

                    else -> {
                        binding.overlay.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.overlay.alpha = slideOffset.coerceIn(0f, 1f)
            }
        }

        bottomSheetCallback?.let { bottomSheetBehavior.addBottomSheetCallback(it) }

        binding.btnNewPlaylistBottomSheet.setOnClickListener {
            navigateToNewPlaylist()
        }
    }

    private fun bindViews() {
        binding.icVectorBuck.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupAddToPlaylistButton() {
        binding.buttonQueue.setOnClickListener {
            viewModel.loadPlaylists()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun bindTrackData() {
        val t = track ?: return

        binding.trackNameTv.text = t.trackName
        binding.artistNameTv.text = t.artistName
        binding.primaryGenreValue.text = t.primaryGenreName
        binding.countryValue.text = t.country
        binding.trackDurationText.text = viewModel.rules.formatDuration(t.trackTimeMillis)

        if (!t.collectionName.isNullOrEmpty()) {
            binding.collectionName.visibility = View.VISIBLE
            binding.collectionNameValue.visibility = View.VISIBLE
            binding.collectionNameValue.text = t.collectionName
        } else {
            binding.collectionName.visibility = View.GONE
            binding.collectionNameValue.visibility = View.GONE
        }

        if (!t.releaseDate.isNullOrEmpty()) {
            binding.releaseDate.visibility = View.VISIBLE
            binding.releaseDateValue.visibility = View.VISIBLE
            binding.releaseDateValue.text = t.releaseDate
        } else {
            binding.releaseDate.visibility = View.GONE
            binding.releaseDateValue.visibility = View.GONE
        }
    }

    private fun loadCoverImage() {
        val url = track?.getCoverArtwork()
        if (!url.isNullOrEmpty()) {
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(binding.imageViewPlayerPlaceholder)
        } else {
            binding.imageViewPlayerPlaceholder.setImageResource(R.drawable.ic_placeholder)
        }
    }

    private fun setupPlayPauseButton() {
        binding.buttonPause.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.play()
            } else {
                viewModel.pause()
            }
        }
    }

    private fun setupFavoriteButton() {
        binding.buttonBorder.setOnClickListener {
            viewModel.onFavoriteClicked()
        }
    }

    private fun observeFavorite() {
        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            binding.buttonBorder.setImageDrawable(
                if (isFavorite) {
                    androidx.core.content.ContextCompat.getDrawable(
                        requireContext(), R.drawable.button_favorite_border_like
                    )
                } else {
                    androidx.core.content.ContextCompat.getDrawable(
                        requireContext(), R.drawable.button_favorite_border_day
                    )
                }
            )
        }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.buttonPause.isChecked = state?.isPlaying == true
            binding.currentTimeText.text =
                viewModel.rules.formatDuration(state?.currentPosition ?: 0)

            if (!state?.error.isNullOrBlank()) {
                Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToNewPlaylist() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        findNavController().navigate(R.id.action_playerFragment_to_newPlaylistFragment)
    }

    private fun observePlaylists() {
        viewModel.playlistsState.observe(viewLifecycleOwner) { playlists ->
            bottomSheetAdapter.updatePlaylists(playlists)
        }
    }

    private fun observeAddTrackStatus() {
        viewModel.addTrackStatus.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            if (message.startsWith("Добавлено")) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    override fun onStop() {
        viewModel.stop()
        super.onStop()
    }

    override fun onDestroyView() {
        bottomSheetCallback?.let { bottomSheetBehavior.removeBottomSheetCallback(it) }
        bottomSheetCallback = null
        _binding = null
        super.onDestroyView()
    }
}
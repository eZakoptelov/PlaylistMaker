package com.example.playlistmaker.player.ui.fragment

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlayerBinding
import com.example.playlistmaker.player.service.PlayerService
import com.example.playlistmaker.player.ui.viewmodel.PlayerViewModel
import com.example.playlistmaker.playlists.ui.adapter.BottomSheetPlaylistAdapter
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.utils.NetworkUtils
import com.example.playlistmaker.utils.receiver.NetworkChangeReceiver
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayerViewModel by viewModel()

    private val track by lazy {
        arguments?.getParcelable<TrackItem>("track")
    }

    private val networkChangeReceiver by lazy {
        NetworkChangeReceiver { context ->
            NetworkUtils.isInternetAvailable(context)
        }
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback? = null

    private var isPlayerServiceBound = false
    private var notificationPermissionRequested = false

    private val bottomSheetAdapter = BottomSheetPlaylistAdapter { playlist ->
        viewModel.addTrackToPlaylist(playlist)
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted && isAdded) {
                Toast.makeText(
                    requireContext(),
                    "Разрешите уведомления, чтобы видеть трек в системной шторке",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private val playerServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            val binder = service as? PlayerService.LocalBinder ?: return
            viewModel.attachPlayerService(binder.getService())

            val host = activity
            if (
                host?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true
            ) {
                viewModel.onAppForegrounded()
            } else {
                viewModel.onAppBackgrounded()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            viewModel.detachPlayerService()
        }
    }

    private val activityLifecycleObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                viewModel.onAppForegrounded()
            }

            Lifecycle.Event.ON_STOP -> {
                val host = activity
                if (
                    host != null &&
                    !host.isFinishing &&
                    !host.isChangingConfigurations
                ) {
                    viewModel.onAppBackgrounded()
                }
            }

            else -> Unit
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().lifecycle.addObserver(activityLifecycleObserver)
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

        val currentTrack = track
        if (currentTrack == null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.track_load_error),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        viewModel.loadTrack(currentTrack)
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

    override fun onStart() {
        super.onStart()

        if (!isPlayerServiceBound) {
            val currentTrack = track ?: return
            val trackUrl = currentTrack.previewUrl ?: return

            val serviceIntent = Intent(
                requireContext(),
                PlayerService::class.java
            ).apply {
                putExtra(PlayerService.EXTRA_TRACK_ID, currentTrack.trackId)
                putExtra(PlayerService.EXTRA_TRACK_URL, trackUrl)
                putExtra(PlayerService.EXTRA_ARTIST_NAME, currentTrack.artistName)
                putExtra(PlayerService.EXTRA_TRACK_NAME, currentTrack.trackName)
                putExtra(
                    PlayerService.EXTRA_TRACK_DURATION,
                    currentTrack.trackTimeMillis
                )
            }

            isPlayerServiceBound = requireContext().bindService(
                serviceIntent,
                playerServiceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    override fun onResume() {
        super.onResume()

        ContextCompat.registerReceiver(
            requireContext(),
            networkChangeReceiver,
            IntentFilter(NetworkChangeReceiver.ACTION_CONNECTIVITY_CHANGE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        requireContext().unregisterReceiver(networkChangeReceiver)
        super.onPause()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.playerSong) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.playlistsBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.rvPlaylistsBottomSheet.layoutManager =
            LinearLayoutManager(requireContext())
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

        bottomSheetCallback?.let {
            bottomSheetBehavior.addBottomSheetCallback(it)
        }

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
        val currentTrack = track ?: return

        binding.trackNameTv.text = currentTrack.trackName
        binding.artistNameTv.text = currentTrack.artistName
        binding.primaryGenreValue.text = currentTrack.primaryGenreName
        binding.countryValue.text = currentTrack.country
        binding.trackDurationText.text =
            viewModel.rules.formatDuration(currentTrack.trackTimeMillis)

        if (!currentTrack.collectionName.isNullOrEmpty()) {
            binding.collectionName.visibility = View.VISIBLE
            binding.collectionNameValue.visibility = View.VISIBLE
            binding.collectionNameValue.text = currentTrack.collectionName
        } else {
            binding.collectionName.visibility = View.GONE
            binding.collectionNameValue.visibility = View.GONE
        }

        if (!currentTrack.releaseDate.isNullOrEmpty()) {
            binding.releaseDate.visibility = View.VISIBLE
            binding.releaseDateValue.visibility = View.VISIBLE
            binding.releaseDateValue.text = currentTrack.releaseDate
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
            binding.imageViewPlayerPlaceholder.setImageResource(
                R.drawable.ic_placeholder
            )
        }
    }

    private fun setupPlayPauseButton() {
        binding.buttonPause.setOnClickListener {
            if (viewModel.state.value?.isPlaying == true) {
                viewModel.pause()
            } else {
                requestNotificationPermissionIfNeeded()
                viewModel.play()
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val permissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted && !notificationPermissionRequested) {
            notificationPermissionRequested = true
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
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
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.button_favorite_border_like
                    )
                } else {
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.button_favorite_border_day
                    )
                }
            )
        }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->

            binding.buttonPause.setPlaying(
                state?.isPlaying == true
            )

            binding.currentTimeText.text =
                viewModel.rules.formatDuration(state?.currentPosition ?: 0L)

            if (!state?.error.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    state.error,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun navigateToNewPlaylist() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        findNavController().navigate(
            R.id.action_playerFragment_to_newPlaylistFragment
        )
    }

    private fun observePlaylists() {
        viewModel.playlistsState.observe(viewLifecycleOwner) { playlists ->
            bottomSheetAdapter.updatePlaylists(playlists)
        }
    }

    private fun observeAddTrackStatus() {
        viewModel.addTrackStatus.observe(viewLifecycleOwner) { message ->
            Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
            ).show()

            if (message.startsWith("Добавлено")) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    override fun onDestroy() {
        val host = activity
        host?.lifecycle?.removeObserver(activityLifecycleObserver)

        val shouldStopPlayback = isRemoving || host?.isFinishing == true

        if (shouldStopPlayback) {

            viewModel.stop()
        }

        viewModel.detachPlayerService()

        if (isPlayerServiceBound) {
            context?.unbindService(playerServiceConnection)
            isPlayerServiceBound = false
        }

        super.onDestroy()
    }

    override fun onDestroyView() {
        bottomSheetCallback?.let {
            bottomSheetBehavior.removeBottomSheetCallback(it)
        }
        bottomSheetCallback = null
        _binding = null
        super.onDestroyView()
    }
}
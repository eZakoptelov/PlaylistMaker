package com.example.playlistmaker.playlists.ui.fragment

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.playlists.domain.model.Playlist
import com.example.playlistmaker.playlists.domain.model.PlaylistDetailsResult
import com.example.playlistmaker.playlists.ui.adapter.TrackInPlaylistAdapter
import com.example.playlistmaker.playlists.ui.viewmodel.PlaylistViewModel
import com.example.playlistmaker.search.domain.model.TrackItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    private val args: PlaylistFragmentArgs by navArgs()
    private val viewModel: PlaylistViewModel by viewModel()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var adapter: TrackInPlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomSheet()
        setupAdapter()
        setupBackButton()
        setupOnBackPressedCallback()
        setupShareButton()
        setupMenuButton()
        observePlaylistDetails()
        observePlaylistDeletion()
        viewModel.loadPlaylistDetails(args.playlistId)
    }

    // --- Настройка BottomSheet ---
    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.clBottomSheet).apply {
            isHideable = false
            isDraggable = true
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.root.post {
            updateBottomSheetPosition()
        }
    }

    private fun updateBottomSheetPosition() {
        val root = binding.root
        val shareButton = binding.btnShare
        val menuButton = binding.btnMenu

        if (root.height == 0 ||
            shareButton.height == 0 ||
            menuButton.height == 0
        ) {
            return
        }

        val shareRect = Rect(
            0, 0, shareButton.width, shareButton.height
        )
        val menuRect = Rect(
            0, 0, menuButton.width, menuButton.height
        )

        root.offsetDescendantRectToMyCoords(shareButton, shareRect)
        root.offsetDescendantRectToMyCoords(menuButton, menuRect)

        val buttonsBottom = maxOf(shareRect.bottom, menuRect.bottom)
        val gapPx = (24 * resources.displayMetrics.density).toInt()
        val targetTop = buttonsBottom + gapPx

        val peekHeight = (root.height - targetTop)
            .coerceIn(1, root.height)

        bottomSheetBehavior.peekHeight = peekHeight
        binding.clBottomSheet.requestLayout()
    }
    private fun setupAdapter() {
        adapter = TrackInPlaylistAdapter(
            onItemClick = { track ->
                val bundle = Bundle().apply {
                    putLong("trackId", track.trackId)
                    putParcelable("track", track)
                }

                findNavController().navigate(
                    R.id.action_playlistFragment_to_playerFragment,
                    bundle
                )
            },
            onItemLongClick = { track ->
                showDeleteDialog(track)
            }
        )

        binding.rvTracks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTracks.adapter = adapter
    }
    private fun showDeleteDialog(track: TrackItem) {
        val dialog = MaterialAlertDialogBuilder(
            requireContext(),
            R.style.DeleteTrackDialog
        )
            .setTitle(R.string.delete_track_title)
            .setPositiveButton(R.string.yes_uppercase) { _, _ ->
                viewModel.deleteTrackFromPlaylist(
                    args.playlistId,
                    track.trackId
                )
            }
            .setNegativeButton(R.string.no_uppercase, null)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(
                R.drawable.bg_delete_dialog
            )

            dialog.findViewById<TextView>(
                com.google.android.material.R.id.alertTitle
            )?.apply {
                setTextColor(Color.BLACK)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }

            val blue = ContextCompat.getColor(
                requireContext(),
                R.color.blue
            )

            listOf(
                AlertDialog.BUTTON_POSITIVE,
                AlertDialog.BUTTON_NEGATIVE
            ).forEach { buttonType ->
                dialog.getButton(buttonType)?.apply {
                    setTextColor(blue)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                }
            }
        }
        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
    private fun setupMenuButton() {
        val menuSheet = binding.clMenuBottomSheet

        menuSheet.visibility = View.VISIBLE

        menuBottomSheetBehavior = BottomSheetBehavior.from(menuSheet).apply {
            isHideable = true
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        menuSheet.visibility = View.GONE
        binding.vMenuScrim.visibility = View.GONE

        menuBottomSheetBehavior.addBottomSheetCallback(
            object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        binding.vMenuScrim.visibility = View.GONE
                        bottomSheet.visibility = View.GONE
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
            }
        )

        binding.btnMenu.setOnClickListener {
            showPlaylistMenu()
        }

        binding.vMenuScrim.setOnClickListener {
            hidePlaylistMenu()
        }

        binding.llMenuShare.setOnClickListener {
            hidePlaylistMenu()
            sharePlaylist()
        }

        binding.llMenuEdit.setOnClickListener {
            hidePlaylistMenu()

            val bundle = Bundle().apply {
                putLong("playlistId", args.playlistId)
            }

            findNavController().navigate(
                R.id.newPlaylistFragment,
                bundle
            )
        }

        binding.llMenuDelete.setOnClickListener {
            hidePlaylistMenu()
            showDeletePlaylistDialog()
        }
    }

    private fun showPlaylistMenu() {
        binding.clMenuBottomSheet.visibility = View.VISIBLE
        binding.vMenuScrim.visibility = View.VISIBLE
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hidePlaylistMenu() {
        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun setupBackButton() {
        binding.icVectorBuck.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupOnBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }

    private fun observePlaylistDetails() {
        viewModel.playlistDetails.observe(viewLifecycleOwner) { result ->
            when (result) {
                is PlaylistDetailsResult.Success -> {
                    bindPlaylist(
                        result.playlist,
                        result.tracks,
                        result.totalDurationMs
                    )

                    adapter.submitList(result.tracks)
                    updateEmptyState(result.tracks.isEmpty())
                }

                is PlaylistDetailsResult.Error -> {
                    Toast.makeText(
                        requireContext(),
                        result.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun bindPlaylist(
        playlist: Playlist,
        tracks: List<TrackItem>,
        totalDurationMs: Long
    ) {
        binding.tvPlaylistName.text = playlist.name
        binding.tvPlaylistDescription.text = playlist.description
        binding.root.post {
            updateBottomSheetPosition()
        }

        val minutes = totalDurationMs / 60_000
        val durationText = getString(
            R.string.playlist_duration_minutes,
            minutes
        )
        val countText = resources.getQuantityString(
            R.plurals.playlist_track_count,
            tracks.size,
            tracks.size
        )

        binding.tvPlaylistMeta.text = getString(
            R.string.playlist_meta,
            durationText,
            countText
        )

        val radius = (8 * requireContext().resources.displayMetrics.density).toInt()

        if (playlist.coverPath.isNotBlank()) {
            binding.llHeaderContent.setBackgroundResource(R.color.white)

            Glide.with(requireContext())
                .load(playlist.coverPath)
                .transform(CenterCrop(), RoundedCorners(radius))
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(binding.ivPlaylistCover)
        } else {
            binding.llHeaderContent.setBackgroundResource(R.color.light_grey)

            Glide.with(requireContext())
                .load(R.drawable.ic_placeholder)
                .transform(CenterCrop(), RoundedCorners(radius))
                .into(binding.ivPlaylistCover)
        }

        binding.tvMenuPlaylistName.text = playlist.name
        binding.tvMenuTracksCount.text = resources.getQuantityString(
            R.plurals.playlist_track_count,
            tracks.size,
            tracks.size
        )

        val menuCoverRadius =
            (8 * requireContext().resources.displayMetrics.density).toInt()

        Glide.with(requireContext())
            .load(playlist.coverPath.takeIf { it.isNotBlank() } ?: R.drawable.ic_placeholder)
            .transform(CenterCrop(), RoundedCorners(menuCoverRadius))
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .into(binding.ivMenuCover)
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.llEmptyState.visibility = View.VISIBLE
            binding.rvTracks.visibility = View.GONE
        } else {
            binding.llEmptyState.visibility = View.GONE
            binding.rvTracks.visibility = View.VISIBLE
        }
    }
    private fun setupShareButton() {
        binding.btnShare.setOnClickListener {
            sharePlaylist()
        }
    }

    private fun sharePlaylist() {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = viewModel.playlistDetails.value

            if (result !is PlaylistDetailsResult.Success) {
                return@launch
            }

            if (result.tracks.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    R.string.share_playlist_empty,
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            val playlist = result.playlist
            val tracks = result.tracks

            val shareText = buildString {
                appendLine(playlist.name)
                appendLine(playlist.description)
                appendLine(
                    getString(R.string.playlist_tracks_count, tracks.size)
                )

                tracks.forEachIndexed { index, track ->
                    val duration = formatTrackDuration(track.trackTimeMillis)

                    appendLine(
                        "${index + 1}. ${track.artistName} - " +
                                "${track.trackName} ($duration)"
                    )
                }
            }.trimEnd()

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            startActivity(
                Intent.createChooser(
                    sendIntent,
                    getString(R.string.share_playlist_chooser)
                )
            )
        }
    }
    private fun formatTrackDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return "%02d:%02d".format(minutes, seconds)
    }
    private fun showDeletePlaylistDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.delete_playlist_message)
            .setNegativeButton(R.string.no_uppercase, null)
            .setPositiveButton(R.string.yes_uppercase) { _, _ ->
                viewModel.deletePlaylist(args.playlistId)
            }
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(
                R.drawable.bg_delete_dialog
            )
            dialog.findViewById<TextView>(
                com.google.android.material.R.id.alertTitle
            )?.apply {
                setTextColor(Color.BLACK)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }

            dialog.findViewById<TextView>(
                android.R.id.message
            )?.setTextColor(Color.BLACK)

            val blue = ContextCompat.getColor(
                requireContext(),
                R.color.blue
            )

            listOf(
                AlertDialog.BUTTON_NEGATIVE,
                AlertDialog.BUTTON_POSITIVE
            ).forEach { buttonType ->
                dialog.getButton(buttonType)?.apply {
                    setTextColor(blue)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                }
            }
        }

        dialog.show()

        val width = (resources.displayMetrics.widthPixels * 0.8).toInt()

        dialog.window?.setLayout(
            width,
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
    private fun observePlaylistDeletion() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlistDeleted.collect {
                findNavController().popBackStack()
            }
        }

        viewModel.deletePlaylistError.observe(viewLifecycleOwner) { message ->
            Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylistDetails(args.playlistId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

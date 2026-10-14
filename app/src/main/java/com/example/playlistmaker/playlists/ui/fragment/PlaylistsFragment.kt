package com.example.playlistmaker.playlists.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import com.example.playlistmaker.playlists.ui.adapter.PlaylistAdapter
import com.example.playlistmaker.playlists.ui.adapter.GridSpacingItemDecoration
import com.example.playlistmaker.playlists.ui.viewmodel.PlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistsViewModel by viewModel()
    private var adapter: PlaylistAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNewPlaylist.setOnClickListener {
            findNavController().navigate(
                R.id.action_mediaFragment_to_newPlaylistFragment,
                bundleOf("playlistId" to 0L)
            )
        }

        val spacing = (8 * resources.displayMetrics.density).toInt()
        binding.rvPlaylists.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)
        binding.rvPlaylists.addItemDecoration(GridSpacingItemDecoration(2, spacing))

        adapter = PlaylistAdapter(
            onItemClick = { playlistId ->
                parentFragmentManager.setFragmentResult(
                    "playlist_click",
                    Bundle().apply {
                        putLong("playlistId", playlistId)
                    }
                )
            },
            playlists = mutableListOf()
        )

        binding.rvPlaylists.adapter = adapter

        viewModel.playlistsState.observe(viewLifecycleOwner) { playlists ->
            if (playlists.isEmpty()) {
                binding.rvPlaylists.visibility = View.GONE
                binding.ivEmptyState.visibility = View.VISIBLE
                binding.tvEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvPlaylists.visibility = View.VISIBLE
                binding.ivEmptyState.visibility = View.GONE
                binding.tvEmptyState.visibility = View.GONE
                adapter?.updatePlaylists(playlists)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        adapter = null
    }
}

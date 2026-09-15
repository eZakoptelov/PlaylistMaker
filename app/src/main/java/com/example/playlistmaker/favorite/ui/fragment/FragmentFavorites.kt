package com.example.playlistmaker.favorite.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentFavoritesTrackBinding
import com.example.playlistmaker.favorite.ui.viewModel.FavoritesState
import com.example.playlistmaker.favorite.ui.viewModel.FavoritesTrackViewModel
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.search.ui.adapter.TrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentFavorites : Fragment() {

    private var _binding: FragmentFavoritesTrackBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoritesTrackViewModel by viewModel()
    private val adapter = TrackAdapter(emptyList())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewFavorites.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FragmentFavorites.adapter
        }
        adapter.setOnItemClickListener(object : TrackAdapter.OnItemClickListener {
            override fun onItemClick(track: TrackItem) {
                val bundle = Bundle().apply {
                    putParcelable("track", track)
                }
                findNavController()
                    .navigate(
                        R.id.action_mediaFragment_to_playerFragment,
                        bundle
                    )
            }
        })


        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoritesState.Empty -> {
                    binding.recyclerViewFavorites.visibility = View.GONE
                    binding.ivEmptyState.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.VISIBLE
                }
                is FavoritesState.Content -> {
                    binding.recyclerViewFavorites.visibility = View.VISIBLE
                    binding.ivEmptyState.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.GONE
                    adapter.submitList(state.tracks)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): FragmentFavorites = FragmentFavorites()
    }
}
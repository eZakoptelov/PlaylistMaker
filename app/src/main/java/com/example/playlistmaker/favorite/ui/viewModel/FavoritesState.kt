package com.example.playlistmaker.favorite.ui.viewModel

import com.example.playlistmaker.search.domain.model.TrackItem

sealed interface FavoritesState {
    data object Empty : FavoritesState
    data class Content(val tracks: List<TrackItem>) : FavoritesState
}
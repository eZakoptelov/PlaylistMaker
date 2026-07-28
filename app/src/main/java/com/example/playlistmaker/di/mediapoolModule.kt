package com.example.playlistmaker.di

import com.example.playlistmaker.media.ui.viewmodel.FavoritesTrackViewModel
import com.example.playlistmaker.media.ui.viewmodel.MediapoolViewModel
import com.example.playlistmaker.media.ui.viewmodel.PlaylistsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val mediapoolModule = module {
    viewModelOf(::MediapoolViewModel)
    viewModelOf(::FavoritesTrackViewModel)
    viewModelOf(::PlaylistsViewModel)
}

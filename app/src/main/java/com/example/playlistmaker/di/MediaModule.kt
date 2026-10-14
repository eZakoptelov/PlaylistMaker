package com.example.playlistmaker.di

import com.example.playlistmaker.db.AppDatabase
import com.example.playlistmaker.playlists.ui.viewmodel.PlaylistsViewModel
import com.example.playlistmaker.playlists.ui.viewmodel.NewPlaylistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mediaModule = module {
    single { get<AppDatabase>().playlistDao() }
    single { get<AppDatabase>().trackInPlaylistDao() }
    viewModel { NewPlaylistViewModel(get()) }
    viewModel { PlaylistsViewModel(get()) }
}

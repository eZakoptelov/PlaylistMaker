package com.example.playlistmaker.di

import com.example.playlistmaker.favorite.data.db.AppDatabase
import com.example.playlistmaker.playlists.ui.viewmodel.PlaylistsViewModel
import com.example.playlistmaker.playlists.data.converters.PlaylistTrackConverter
import com.example.playlistmaker.playlists.data.repository.PlaylistRepository
import com.example.playlistmaker.playlists.data.repository.impl.PlaylistRepositoryImpl
import com.example.playlistmaker.playlists.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlists.ui.viewmodel.NewPlaylistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mediaModule = module {
    single { get<AppDatabase>().playlistDao() }
    single { get<AppDatabase>().trackInPlaylistDao() }
    single { PlaylistTrackConverter() }
    single<PlaylistRepository> { PlaylistRepositoryImpl(get(), get(), get()) }
    single { PlaylistInteractor(get()) }
    viewModel { NewPlaylistViewModel(get()) }
    viewModel { PlaylistsViewModel(get()) }
}

package com.example.playlistmaker.di


import com.example.playlistmaker.playlists.data.interactor.impl.PlaylistInteractorImpl
import com.example.playlistmaker.playlists.data.repository.impl.PlaylistRepositoryImpl
import com.example.playlistmaker.playlists.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.playlists.domain.repository.PlaylistRepository
import com.example.playlistmaker.playlists.ui.viewmodel.PlaylistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val playlistsModule = module {

    single<PlaylistRepository> {
        PlaylistRepositoryImpl(
            playlistDao = get(),
            trackInPlaylistDao = get()
        )
    }
    single<PlaylistInteractor> {
        PlaylistInteractorImpl(repository = get())
    }
    viewModel<PlaylistViewModel> {
        PlaylistViewModel(interactor = get())
    }
}

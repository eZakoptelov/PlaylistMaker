package com.example.playlistmaker.di

import com.example.playlistmaker.player.data.LocalMediaPlayerImpl
import com.example.playlistmaker.player.domain.MediaPlayerWrapper
import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.player.domain.impl.PlayerInteractorImpl
import com.example.playlistmaker.player.domain.impl.PlayerRulesImpl
import com.example.playlistmaker.player.domain.PlayerRules
import com.example.playlistmaker.player.ui.viewmodel.PlayerViewModel
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

val playerModule = module {

    factory { android.media.MediaPlayer() }


    factory<MediaPlayerWrapper> {
        LocalMediaPlayerImpl(get())
    }

    single<PlayerRules> { PlayerRulesImpl() }

    factory<PlayerInteractor> {
        PlayerInteractorImpl(get())
    }

    viewModel {
        PlayerViewModel(
            interactor = get(),
            rules = get()
        )
    }
}


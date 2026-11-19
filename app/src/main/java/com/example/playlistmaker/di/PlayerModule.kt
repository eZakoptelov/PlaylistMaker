package com.example.playlistmaker.di

import com.example.playlistmaker.player.domain.PlayerRules
import com.example.playlistmaker.player.domain.impl.PlayerRulesImpl
import com.example.playlistmaker.player.ui.viewmodel.PlayerViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val playerModule = module {

    single<PlayerRules> {
        PlayerRulesImpl()
    }

    viewModel {
        PlayerViewModel(
            application = androidApplication(),
            rules = get(),
            favoriteInteractor = get(),
            playlistInteractor = get()
        )
    }
}

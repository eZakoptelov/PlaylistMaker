package com.example.playlistmaker.di

import android.content.Context
import com.example.playlistmaker.settings.data.repository.impl.SettingsRepositoryImpl
import com.example.playlistmaker.settings.domain.SettingsUseCase
import com.example.playlistmaker.settings.ui.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val settingsModule = module {
    single(named("settings_prefs")) {
        get<Context>().getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    factory<SettingsUseCase> {
        SettingsRepositoryImpl(
            prefs = get(named("settings_prefs"))
        )
    }
    viewModel {
        SettingsViewModel(
            settingsUseCase = get(),
            sharingUseCase = get()
        )
    }
}

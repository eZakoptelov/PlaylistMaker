package com.example.playlistmaker.di

import com.example.playlistmaker.settings.data.interactor.impl.SettingsInteractorImpl
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractor
import com.example.playlistmaker.sharing.data.interactor.impl.SharingInteractorImpl
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor
import com.example.playlistmaker.settings.ui.viewmodel.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel
import android.content.Context
import org.koin.core.qualifier.named

val settingsAndSharingModule = module {
    single(named("app_settings")) {
        androidContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

    single<SettingsInteractor> { SettingsInteractorImpl(get(named("app_settings"))) }
    single<SharingInteractor> { SharingInteractorImpl() }

    viewModel {
        SettingsViewModel(
            settingsInteractor = get(),
            sharingInteractor = get()
        )
    }
}

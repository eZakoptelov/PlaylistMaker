package com.example.playlistmaker.di

import com.example.playlistmaker.favorite.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.favorite.domain.interactor.impl.FavoriteTracksInteractorImpl
import org.koin.dsl.module

val interactorModule = module {
    single<FavoriteTracksInteractor> {
        FavoriteTracksInteractorImpl(get()) }
}

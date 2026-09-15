package com.example.playlistmaker.di

import com.example.playlistmaker.favorite.data.db.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single { AppDatabase.create(androidContext()) }
    single { get<AppDatabase>().favoriteTracksDao() }
}

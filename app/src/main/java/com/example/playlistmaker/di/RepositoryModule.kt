package com.example.playlistmaker.di

import com.example.playlistmaker.favorite.data.db.AppDatabase
import com.example.playlistmaker.favorite.data.dao.FavoriteTracksDao
import com.example.playlistmaker.favorite.data.repository.impl.FavoriteTracksRepositoryImpl
import com.example.playlistmaker.favorite.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.search.data.repository.impl.SearchRepositoryImpl
import com.example.playlistmaker.search.domain.repository.SearchRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<FavoriteTracksDao> { get<AppDatabase>().favoriteTracksDao() }

    single<SearchRepository> {
        SearchRepositoryImpl(
            api = get(),
            storage = get(),
            mapper = get(),
        )
    }

    single<FavoriteTracksRepository> {
        FavoriteTracksRepositoryImpl(get())
    }
}

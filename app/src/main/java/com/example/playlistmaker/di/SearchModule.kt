package com.example.playlistmaker.di

import com.example.playlistmaker.search.data.api.ItunesApi
import com.example.playlistmaker.search.data.api.ItunesApiFactory
import com.example.playlistmaker.search.data.mapper.TrackMapper
import com.example.playlistmaker.search.data.repository.SearchRepository
import com.example.playlistmaker.search.data.repository.impl.SearchRepositoryImpl
import com.example.playlistmaker.search.data.storage.HistoryStorage
import com.example.playlistmaker.search.domain.usecase.*
import com.example.playlistmaker.search.domain.usecase.impl.*
import com.example.playlistmaker.search.ui.viewmodel.SearchViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel
import android.content.Context
import org.koin.core.qualifier.named

val searchModule = module {

    single<ItunesApi> { ItunesApiFactory.create() }
    single { TrackMapper() }

    single(named("app_prefs")) {
        androidContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }
    single { HistoryStorage(get(named("app_prefs"))) }

    single<SearchRepository> { SearchRepositoryImpl(get(), get(), get()) }

    // UseCases
    single<SearchUseCase> { SearchUseCaseImpl(get()) }
    single<GetSearchHistoryUseCase> { GetSearchHistoryUseCaseImpl(get()) }
    single<AddToHistoryUseCase> { AddToHistoryUseCaseImpl(get()) }
    single<ClearHistoryUseCase> { ClearHistoryUseCaseImpl(get()) }

    viewModel {
        SearchViewModel(
            searchUseCase = get(),
            getHistoryUseCase = get(),
            addToHistoryUseCase = get(),
            clearHistoryUseCase = get()
        )
    }
}

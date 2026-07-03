package com.example.playlistmaker.presentation.creator

import android.content.Context
import com.example.playlistmaker.data.api.ItunesApi
import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.data.repository.SearchHistory
import com.example.playlistmaker.data.repository.SearchRepositoryImpl
import com.example.playlistmaker.data.storage.HistoryStorage
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.repository.SearchRepository
import com.example.playlistmaker.domain.usecase.AddToHistoryUseCase
import com.example.playlistmaker.domain.usecase.ClearHistoryUseCase
import com.example.playlistmaker.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.domain.usecase.SearchUseCase

class UseCaseCreator(
    private val api: ItunesApi,
    private val context: Context
) {
    private val trackMapper = TrackMapper()
    private val historyStorage =
        HistoryStorage(context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE))
    private val searchHistory = SearchHistory(historyStorage)
    private val searchRepository: SearchRepository =
        SearchRepositoryImpl(api, searchHistory, trackMapper)

    private val interactor = SearchInteractor(searchRepository)

    fun createSearchUseCase(): SearchUseCase = interactor
    fun createGetSearchHistoryUseCase(): GetSearchHistoryUseCase = interactor
    fun createAddToHistoryUseCase(): AddToHistoryUseCase = interactor
    fun createClearHistoryUseCase(): ClearHistoryUseCase = interactor
}

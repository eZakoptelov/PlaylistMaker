package com.example.playlistmaker.creator

import com.example.playlistmaker.search.data.api.ItunesApi
import com.example.playlistmaker.search.data.mapper.TrackMapper
import com.example.playlistmaker.search.data.repository.SearchHistory
import com.example.playlistmaker.search.data.repository.SearchRepository
import com.example.playlistmaker.search.data.repository.impl.SearchRepositoryImpl
import com.example.playlistmaker.search.data.storage.StorageCreator
import com.example.playlistmaker.search.domain.usecase.AddToHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.ClearHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.SearchUseCase
import com.example.playlistmaker.search.domain.usecase.impl.AddToHistoryUseCaseImpl
import com.example.playlistmaker.search.domain.usecase.impl.ClearHistoryUseCaseImpl
import com.example.playlistmaker.search.domain.usecase.impl.GetSearchHistoryUseCaseImpl
import com.example.playlistmaker.search.domain.usecase.impl.SearchUseCaseImpl
import com.example.playlistmaker.settings.data.interactor.impl.SettingsInteractorImpl
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractor
import com.example.playlistmaker.sharing.data.interactor.impl.SharingInteractorImpl
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor

class UseCaseCreator(
    private val api: ItunesApi,
    private val storageCreator: StorageCreator,
    private val settingsPrefs: android.content.SharedPreferences
) {
    private val trackMapper = TrackMapper()
    private val historyStorage = storageCreator.createHistoryStorage()
    private val searchHistory = SearchHistory(historyStorage)

    private val repository: SearchRepository = SearchRepositoryImpl(api, searchHistory, trackMapper)

    // Шаринг — без зависимостей
    private val sharingInteractor: SharingInteractor = SharingInteractorImpl()

    // Настройки — через prefs
    private val settingsInteractor: SettingsInteractor = SettingsInteractorImpl(settingsPrefs)

    fun createSearchUseCase(): SearchUseCase = SearchUseCaseImpl(repository)
    fun createGetSearchHistoryUseCase(): GetSearchHistoryUseCase =
        GetSearchHistoryUseCaseImpl(repository)
    fun createAddToHistoryUseCase(): AddToHistoryUseCase = AddToHistoryUseCaseImpl(repository)
    fun createClearHistoryUseCase(): ClearHistoryUseCase = ClearHistoryUseCaseImpl(repository)


    fun createSettingsInteractor(): SettingsInteractor = settingsInteractor
    fun createSharingInteractor(): SharingInteractor = sharingInteractor

}

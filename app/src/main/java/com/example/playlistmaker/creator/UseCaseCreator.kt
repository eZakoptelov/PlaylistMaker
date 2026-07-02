package com.example.playlistmaker.creator

import com.example.playlistmaker.search.data.api.ItunesApi
import com.example.playlistmaker.search.data.mapper.TrackMapper
import com.example.playlistmaker.search.data.repository.SearchHistory
import com.example.playlistmaker.search.data.storage.StorageCreator
import com.example.playlistmaker.search.domain.interactor.SearchInteractor
import com.example.playlistmaker.search.data.repository.SearchRepository
import com.example.playlistmaker.search.data.repository.impl.SearchRepositoryImpl
import com.example.playlistmaker.search.domain.usecase.*
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor
import com.example.playlistmaker.sharing.data.interactor.impl.SharingInteractorImpl
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractor
import com.example.playlistmaker.settings.data.interactor.impl.SettingsInteractorImpl

class UseCaseCreator(
    private val api: ItunesApi,
    private val storageCreator: StorageCreator,
    private val settingsPrefs: android.content.SharedPreferences  // <-- ДОБАВИТЬ ЭТО
) {
    private val trackMapper = TrackMapper()
    private val historyStorage = storageCreator.createHistoryStorage()
    private val searchHistory = SearchHistory(historyStorage)
    private val searchRepository: SearchRepository =
        SearchRepositoryImpl(api, searchHistory, trackMapper)

    private val interactor = SearchInteractor(searchRepository)

    // Шаринг — без зависимостей
    private val sharingInteractor: SharingInteractor = SharingInteractorImpl()

    // Настройки — через prefs
    private val settingsInteractor: SettingsInteractor = SettingsInteractorImpl(settingsPrefs)

    fun createSearchUseCase(): SearchUseCase = interactor
    fun createGetSearchHistoryUseCase(): GetSearchHistoryUseCase = interactor
    fun createAddToHistoryUseCase(): AddToHistoryUseCase = interactor
    fun createClearHistoryUseCase(): ClearHistoryUseCase = interactor

    fun createSharingInteractor(): SharingInteractor = sharingInteractor

    // НОВЫЙ МЕТОД
    fun createSettingsInteractor(): SettingsInteractor = settingsInteractor
}

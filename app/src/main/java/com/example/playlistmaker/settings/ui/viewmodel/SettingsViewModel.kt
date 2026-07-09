package com.example.playlistmaker.settings.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractor
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor

class SettingsViewModel(
    private val sharingInteractor: SharingInteractor,
    private val settingsInteractor: SettingsInteractor,
) : ViewModel() {

    private val _state = MutableLiveData<SettingsUiState>()
    val state: LiveData<SettingsUiState> = _state

    init {
        loadState()
    }

    private fun loadState() {

        val isDark = settingsInteractor.getDarkTheme()
        val shareText = settingsInteractor.getShareText()
        _state.value = SettingsUiState.Content(isDark, shareText)
    }

    fun processInput(intent: SettingsUiIntent) {
        when (intent) {
            is SettingsUiIntent.BackClicked -> _state.value = SettingsUiState.FinishActivity
            is SettingsUiIntent.ShareAppClicked -> shareApp()
            is SettingsUiIntent.SupportClicked -> openSupport()
            is SettingsUiIntent.AgreementClicked -> openAgreement()
            is SettingsUiIntent.ThemeToggled -> toggleTheme(intent.isChecked)
        }
    }
    private fun toggleTheme(isDark: Boolean) {
        settingsInteractor.setDarkTheme(isDark)
        val currentShareText = (_state.value as? SettingsUiState.Content)?.shareText ?: ""
        _state.value = SettingsUiState.Content(isDark, currentShareText)
    }

    fun shareApp() {
        _state.value = SettingsUiState.LaunchIntent(sharingInteractor.shareApp())
    }

    fun openSupport() {
        _state.value = SettingsUiState.LaunchIntent(sharingInteractor.openSupport())
    }

    fun openAgreement() {
        _state.value = SettingsUiState.LaunchIntent(sharingInteractor.openAgreement())
    }
}
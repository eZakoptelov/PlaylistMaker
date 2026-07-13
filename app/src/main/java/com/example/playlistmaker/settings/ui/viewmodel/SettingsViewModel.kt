package com.example.playlistmaker.settings.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.SettingsUseCase
import com.example.playlistmaker.sharing.domain.SharingUseCase

class SettingsViewModel(
    private val settingsUseCase: SettingsUseCase,
    private val sharingUseCase: SharingUseCase
) : ViewModel() {

    private val _state = MutableLiveData<SettingsUiState>()
    val state: LiveData<SettingsUiState> = _state

    init {
        loadState()
    }

    private fun loadState() {
        val isDark = settingsUseCase.getDarkTheme()
        val shareText = settingsUseCase.getShareText()
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
        settingsUseCase.setDarkTheme(isDark)

        // Сохраняем текущий shareText, чтобы не потерять его при обновлении состояния
        val currentShareText = (_state.value as? SettingsUiState.Content)?.shareText ?: ""
        _state.value = SettingsUiState.Content(isDark, currentShareText)
    }

    fun shareApp() {
        _state.value = SettingsUiState.LaunchIntent(sharingUseCase.shareApp())
    }

    fun openSupport() {
        _state.value = SettingsUiState.LaunchIntent(sharingUseCase.openSupport())
    }

    fun openAgreement() {
        _state.value = SettingsUiState.LaunchIntent(sharingUseCase.openAgreement())
    }
}

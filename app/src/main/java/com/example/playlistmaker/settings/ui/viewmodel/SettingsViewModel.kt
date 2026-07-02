package com.example.playlistmaker.settings.ui.viewmodel


import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractor
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor

class SettingsViewModel(
    private val sharingInteractor: SharingInteractor,
    private val settingsInteractor: SettingsInteractor,
    private val onThemeModeRequested: (Int) -> Unit
) : ViewModel(){

    private val _state = MutableLiveData<SettingsUiState>()
    val state: LiveData<SettingsUiState> = _state

    init {
        loadState()
    }

    private fun loadState() {
        val isDark = settingsInteractor.getDarkTheme()
        val shareText = settingsInteractor.getShareText()
        _state.value = SettingsUiState.Loaded(isDark, shareText)
    }

    fun toggleTheme(isDark: Boolean) {
        settingsInteractor.setDarkTheme(isDark)
        onThemeModeRequested(if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
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
    fun clearEvent() {
        val current = _state.value
        if (current is SettingsUiState.LaunchIntent || current is SettingsUiState.RestartActivity) {
            loadState()
        }
    }
}

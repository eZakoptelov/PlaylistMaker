package com.example.playlistmaker.settings.ui.viewmodel

sealed class SettingsUiIntent {
    data object BackClicked : SettingsUiIntent()
    data object ShareAppClicked : SettingsUiIntent()
    data object SupportClicked : SettingsUiIntent()
    data object AgreementClicked : SettingsUiIntent()
    data class ThemeToggled(val isChecked: Boolean) : SettingsUiIntent()
}
package com.example.playlistmaker.settings.ui.viewmodel

sealed class SettingsUiState {
    data class Loaded(
        val isDarkTheme: Boolean,
        val shareText: String
    ) : SettingsUiState()


    data class LaunchIntent(val intent: android.content.Intent) : SettingsUiState()
    object RestartActivity : SettingsUiState()
}

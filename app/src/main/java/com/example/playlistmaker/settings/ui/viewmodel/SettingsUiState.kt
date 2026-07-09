package com.example.playlistmaker.settings.ui.viewmodel

import android.content.Intent

sealed class SettingsUiState {
data class Content(
        val isDark: Boolean,
        val shareText: String,
        val applyLocalTheme: Boolean = false
) : SettingsUiState()

    data class LaunchIntent(val intent: Intent) : SettingsUiState()

    data object FinishActivity : SettingsUiState()
}

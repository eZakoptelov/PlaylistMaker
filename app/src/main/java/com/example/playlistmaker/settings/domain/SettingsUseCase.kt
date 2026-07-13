package com.example.playlistmaker.settings.domain

interface SettingsUseCase {
    fun getDarkTheme(): Boolean
    fun setDarkTheme(isDark: Boolean)
    fun getShareText(): String
}

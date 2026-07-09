package com.example.playlistmaker.settings.domain.interactor

interface SettingsInteractor {
    fun getDarkTheme(): Boolean
    fun setDarkTheme(isDark: Boolean)
    fun getShareText(): String
}
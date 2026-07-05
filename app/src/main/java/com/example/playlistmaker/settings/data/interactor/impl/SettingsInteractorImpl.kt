package com.example.playlistmaker.settings.data.interactor.impl

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.example.playlistmaker.settings.domain.interactor.SettingsInteractor
import com.example.playlistmaker.utils.Constants

class SettingsInteractorImpl(
    private val prefs: SharedPreferences
) : SettingsInteractor {

    override fun getDarkTheme(): Boolean {
        return prefs.getBoolean(Constants.KEY_DARK_THEME, false)
    }

    override fun setDarkTheme(isDark: Boolean) {
        prefs.edit { putBoolean(Constants.KEY_DARK_THEME, isDark) }
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    override fun getShareText(): String {
        return Constants.KEY_SHARE_TEXT
    }
}
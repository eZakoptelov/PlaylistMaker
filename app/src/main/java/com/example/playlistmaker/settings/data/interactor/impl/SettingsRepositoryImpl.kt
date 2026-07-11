package com.example.playlistmaker.settings.data.repository.impl

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.example.playlistmaker.settings.domain.SettingsUseCase
import com.example.playlistmaker.utils.Constants

class SettingsRepositoryImpl(
    private val prefs: SharedPreferences
) : SettingsUseCase {

    override fun getDarkTheme(): Boolean {
        return prefs.getBoolean(Constants.KEY_DARK_THEME, false)
    }

    override fun setDarkTheme(isDark: Boolean) {
        prefs.edit { putBoolean(Constants.KEY_DARK_THEME, isDark) }
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    override fun getShareText(): String = Constants.KEY_SHARE_TEXT
}

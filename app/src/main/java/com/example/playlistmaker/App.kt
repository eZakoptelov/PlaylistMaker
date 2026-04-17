package com.example.playlistmaker

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {
    private val prefs by lazy { getSharedPreferences("app_settings", MODE_PRIVATE) }

    var darkTheme: Boolean
        get() = prefs.getBoolean("dark_theme", false)
        set(value) {
            prefs.edit().putBoolean("dark_theme", value).apply()
            updateTheme(value)
        }

    override fun onCreate() {
        super.onCreate()
        // Явно загружаем и применяем сохранённую тему при старте
        applySavedTheme()
    }

    private fun applySavedTheme() {
        val savedTheme = darkTheme // автоматически загрузится из prefs
        updateTheme(savedTheme)
    }

    private fun updateTheme(darkThemeEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}

val Context.app: App
    get() = when (this) {
        is App -> this
        else -> (applicationContext as App)
    }

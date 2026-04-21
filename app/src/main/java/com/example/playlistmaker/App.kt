package com.example.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {
    private lateinit var prefs: SharedPreferences
    var darkTheme = false

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        // Загружаем сохранённую тему или используем значение по умолчанию (false)
        darkTheme = prefs.getBoolean("dark_theme", false)
        // Применяем тему при запуске приложения
        updateTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        // Сохраняем в SharedPreferences
        prefs.edit().putBoolean("dark_theme", darkThemeEnabled).apply()
        // Устанавливаем тему через AppCompatDelegate
        updateTheme(darkThemeEnabled)
    }

    private fun updateTheme(darkThemeEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}

package com.example.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.data.api.ItunesApi
import com.example.playlistmaker.presentation.creator.UseCaseCreator
import com.example.playlistmaker.utils.Constants.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {
    lateinit var useCaseCreator: UseCaseCreator
    var darkTheme = false
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        useCaseCreator = UseCaseCreator(
            api = provideItunesApi(),
            context = this
        )
        prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        darkTheme = prefs.getBoolean("dark_theme", false)
        updateTheme(darkTheme)

    }
    private fun provideItunesApi(): ItunesApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesApi::class.java)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        prefs.edit().putBoolean("dark_theme", darkThemeEnabled).apply()
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

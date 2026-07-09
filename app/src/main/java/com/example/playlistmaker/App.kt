package com.example.playlistmaker

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.creator.UseCaseCreator
import com.example.playlistmaker.search.data.api.ItunesApi
import com.example.playlistmaker.search.data.api.ItunesApiFactory
import com.example.playlistmaker.search.data.storage.StorageCreator

class App : Application() {
    lateinit var useCaseCreator: UseCaseCreator
    lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val darkTheme = prefs.getBoolean("dark_theme", false)
        applyTheme(darkTheme)

        val storageCreator = StorageCreator(getSharedPreferences("app_prefs", Context.MODE_PRIVATE))
        val itunesApi: ItunesApi = ItunesApiFactory.create()

        useCaseCreator = UseCaseCreator(itunesApi, storageCreator, prefs)
    }

    fun applyTheme(darkThemeEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}

package com.example.playlistmaker.settings.ui.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.App
import com.example.playlistmaker.R
import com.example.playlistmaker.settings.ui.viewmodel.SettingsUiState
import com.example.playlistmaker.settings.ui.viewmodel.SettingsViewModel
import com.example.playlistmaker.settings.ui.viewmodel.SettingsViewModelFactory
import com.google.android.material.switchmaterial.SwitchMaterial


class SettingsActivity : AppCompatActivity() {

    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        enableEdgeToEdge()

        val app = application as App
        val useCaseCreator = app.useCaseCreator
        val settingsInteractor = useCaseCreator.createSettingsInteractor()
        val isDark = settingsInteractor.getDarkTheme()

        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_product)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val sharingInteractor = useCaseCreator.createSharingInteractor()
        val factory = SettingsViewModelFactory(sharingInteractor, settingsInteractor) { mode: Int ->
            AppCompatDelegate.setDefaultNightMode(mode)
        }
        viewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]

        setupButtons()
        observeState()
    }

    private fun setupButtons() {
        findViewById<TextView>(R.id.shareApp).setOnClickListener {
            viewModel.shareApp()
        }
        findViewById<TextView>(R.id.supportButton).setOnClickListener {
            viewModel.openSupport()
        }
        findViewById<TextView>(R.id.userAgreementButton).setOnClickListener {
            viewModel.openAgreement()
        }

        // Кнопка переключения темы
        findViewById<SwitchMaterial>(R.id.switchDarkMode)?.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleTheme(isChecked)
        }
        //кнопка назад
        findViewById<Button>(R.id.ic_vector_buck)?.setOnClickListener {
           finish()
        }
    }

    private fun observeState() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is SettingsUiState.Loaded -> updateUi(state)
                is SettingsUiState.LaunchIntent -> {
                    startActivity(state.intent)
                    viewModel.clearEvent()
                }
                is SettingsUiState.RestartActivity -> {
                    viewModel.clearEvent()
                    recreate()
                }
            }
        }
    }


    private fun updateUi(state: SettingsUiState.Loaded) {
        findViewById<SwitchMaterial>(R.id.switchDarkMode)?.isChecked = state.isDarkTheme
    }
}
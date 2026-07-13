package com.example.playlistmaker.settings.ui.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.settings.ui.viewmodel.SettingsUiIntent
import com.example.playlistmaker.settings.ui.viewmodel.SettingsUiState
import com.example.playlistmaker.settings.ui.viewmodel.SettingsViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {
    private val viewModel: SettingsViewModel by viewModel()

    // UI Elements
    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var tvShareApp: TextView
    private lateinit var tvSupportButton: TextView
    private lateinit var tvUserAgreementButton: TextView
    private lateinit var btnBack: Button
    private var isUserToggling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        enableEdgeToEdge()

        setupWindowInsets()
        bindViews()
        observeState()
        setupButtons()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_product)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun observeState() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is SettingsUiState.Content -> updateUi(state)
                is SettingsUiState.LaunchIntent -> startActivity(state.intent)
                is SettingsUiState.FinishActivity -> finish()
            }
        }
    }

    private fun bindViews() {
        switchDarkMode = findViewById(R.id.switchDarkMode)
        tvShareApp = findViewById(R.id.shareApp)
        tvSupportButton = findViewById(R.id.supportButton)
        tvUserAgreementButton = findViewById(R.id.userAgreementButton)
        btnBack = findViewById(R.id.ic_vector_buck)
    }

    private fun setupButtons() {
        btnBack.setOnClickListener {
            viewModel.processInput(SettingsUiIntent.BackClicked)
        }

        tvShareApp.setOnClickListener {
            viewModel.processInput(SettingsUiIntent.ShareAppClicked)
        }
        tvSupportButton.setOnClickListener {
            viewModel.processInput(SettingsUiIntent.SupportClicked)
        }
        tvUserAgreementButton.setOnClickListener {
            viewModel.processInput(SettingsUiIntent.AgreementClicked)
        }

        switchDarkMode.setOnCheckedChangeListener(null)

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (!isUserToggling) {
                viewModel.processInput(SettingsUiIntent.ThemeToggled(isChecked))
            }
        }
    }

    private fun updateUi(state: SettingsUiState.Content) {
        isUserToggling = true
        try {
            switchDarkMode.isChecked = state.isDark
        } finally {
            isUserToggling = false
        }
    }
}
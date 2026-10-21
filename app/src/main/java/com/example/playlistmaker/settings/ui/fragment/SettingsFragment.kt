package com.example.playlistmaker.settings.ui.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSettingsBinding
import com.example.playlistmaker.settings.ui.viewmodel.SettingsUiIntent
import com.example.playlistmaker.settings.ui.viewmodel.SettingsUiState
import com.example.playlistmaker.settings.ui.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModel()

    private var isUserToggling = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWindowInsets()
        observeState()
        setupListeners()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.settingsRoot) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SettingsUiState.Content -> updateUi(state)

                is SettingsUiState.LaunchIntent -> {
                    val intent = if (state.intent.action == Intent.ACTION_SEND) {
                        Intent.createChooser(state.intent, null)
                    } else {
                        state.intent
                    }

                    try {
                        startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_open_app),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is SettingsUiState.FinishActivity -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.shareApp.setOnClickListener {
            viewModel.processInput(
                SettingsUiIntent.ShareAppClicked
            )
        }

        binding.supportButton.setOnClickListener {
            viewModel.processInput(
                SettingsUiIntent.SupportClicked
            )
        }

        binding.userAgreementButton.setOnClickListener {
            viewModel.processInput(
                SettingsUiIntent.AgreementClicked
            )
        }

        binding.switchDarkMode.setOnCheckedChangeListener(null)

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (!isUserToggling) {
                viewModel.processInput(
                    SettingsUiIntent.ThemeToggled(isChecked)
                )
            }
        }
    }

    private fun updateUi(state: SettingsUiState.Content) {
        isUserToggling = true

        try {
            binding.switchDarkMode.isChecked = state.isDark
        } finally {
            isUserToggling = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

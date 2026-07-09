package com.example.playlistmaker.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.player.data.LocalMediaPlayerImpl
import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.player.domain.PlayerRules
import com.example.playlistmaker.player.domain.impl.PlayerInteractorImpl
import com.example.playlistmaker.player.domain.impl.PlayerRulesImpl

class PlayerViewModelFactory private constructor(
    private val interactor: PlayerInteractor,
    private val rules: PlayerRules
) : ViewModelProvider.Factory {

    companion object {
        fun create(): PlayerViewModelFactory {
            // В реальном проекте это должно быть в Application / модуле DI
            val mediaPlayerWrapper = LocalMediaPlayerImpl()
            val interactor = PlayerInteractorImpl(mediaPlayerWrapper)
            val rules = PlayerRulesImpl()
            return PlayerViewModelFactory(interactor, rules)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            PlayerViewModel(interactor, rules) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

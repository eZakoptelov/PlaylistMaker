package com.example.playlistmaker.sharing.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor

class SharingViewModelFactory(private val interactor: SharingInteractor) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SharingViewModel(interactor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

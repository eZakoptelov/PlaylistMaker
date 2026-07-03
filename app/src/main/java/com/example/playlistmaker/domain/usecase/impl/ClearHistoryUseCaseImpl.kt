package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.repository.SearchRepository
import com.example.playlistmaker.domain.usecase.ClearHistoryUseCase

class ClearHistoryUseCaseImpl(
    private val repository: SearchRepository
) : ClearHistoryUseCase {

    override val clearHistory: () -> Unit = {
        repository.clearHistory()
    }
}
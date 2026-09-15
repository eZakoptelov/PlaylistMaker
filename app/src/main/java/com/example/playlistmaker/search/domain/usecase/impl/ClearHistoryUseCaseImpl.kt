package com.example.playlistmaker.search.domain.usecase.impl

import com.example.playlistmaker.search.domain.repository.SearchRepository
import com.example.playlistmaker.search.domain.usecase.ClearHistoryUseCase

class ClearHistoryUseCaseImpl(
    private val repository: SearchRepository
) : ClearHistoryUseCase {

    override suspend fun clearHistory() {
        repository.clearHistory()
    }
}
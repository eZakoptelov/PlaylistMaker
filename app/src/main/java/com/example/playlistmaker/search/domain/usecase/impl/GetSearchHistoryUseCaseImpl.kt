package com.example.playlistmaker.search.domain.usecase.impl

import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.search.domain.repository.SearchRepository
import com.example.playlistmaker.search.domain.usecase.GetSearchHistoryUseCase

class GetSearchHistoryUseCaseImpl(
    private val repository: SearchRepository
) : GetSearchHistoryUseCase {

    override fun getHistory(): List<TrackItem> {
        return repository.getSearchHistory()
    }
}
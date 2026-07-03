package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.TrackItem
import com.example.playlistmaker.domain.repository.SearchRepository
import com.example.playlistmaker.domain.usecase.GetSearchHistoryUseCase

class GetSearchHistoryUseCaseImpl(
    private val repository: SearchRepository
) : GetSearchHistoryUseCase {


    override val getHistory: () -> List<TrackItem> = {
        repository.getSearchHistory()
    }
}

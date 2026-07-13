package com.example.playlistmaker.search.domain.usecase.impl

import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.search.domain.repository.SearchRepository
import com.example.playlistmaker.search.domain.usecase.AddToHistoryUseCase

class AddToHistoryUseCaseImpl(
    private val repository: SearchRepository
) : AddToHistoryUseCase {

    override fun addTrack(track: TrackItem) {
        repository.addToHistory(track)
    }
}
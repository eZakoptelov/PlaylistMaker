package com.example.playlistmaker.search.domain.usecase.impl

import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.search.domain.repository.SearchRepository
import com.example.playlistmaker.search.domain.usecase.SearchUseCase

class SearchUseCaseImpl(
    private val repository: SearchRepository
) : SearchUseCase {

    override fun search(query: String, onResult: (Result<List<TrackItem>>) -> Unit) {
        repository.searchTracks(query, onResult)
    }
}
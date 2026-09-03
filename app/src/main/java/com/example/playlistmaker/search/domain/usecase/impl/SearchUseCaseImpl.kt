package com.example.playlistmaker.search.domain.usecase.impl

import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.search.domain.repository.SearchRepository
import com.example.playlistmaker.search.domain.usecase.SearchUseCase
import kotlinx.coroutines.flow.Flow

class SearchUseCaseImpl(
    private val repository: SearchRepository
) : SearchUseCase {

    override fun search(query: String): Flow<Result<List<TrackItem>>> =
        repository.searchTracks(query)
}
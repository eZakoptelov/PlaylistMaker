package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.TrackItem
import com.example.playlistmaker.domain.repository.SearchRepository
import com.example.playlistmaker.domain.usecase.SearchUseCase

class SearchUseCaseImpl(
    private val repository: SearchRepository
) : SearchUseCase {

    override val search: (String, (List<TrackItem>?, Throwable?) -> Unit) -> Unit =
        { query, callback ->
            repository.searchTracks(query, callback)
        }
}
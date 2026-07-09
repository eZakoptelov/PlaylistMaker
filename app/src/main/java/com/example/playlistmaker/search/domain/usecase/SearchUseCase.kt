package com.example.playlistmaker.search.domain.usecase

import com.example.playlistmaker.search.domain.model.TrackItem

interface SearchUseCase {
    fun search(query: String, onResult: (Result<List<TrackItem>>) -> Unit)
}


package com.example.playlistmaker.search.domain.usecase

import com.example.playlistmaker.search.domain.model.TrackItem
import kotlinx.coroutines.flow.Flow

interface SearchUseCase {
    fun search(query: String): Flow<Result<List<TrackItem>>>
}

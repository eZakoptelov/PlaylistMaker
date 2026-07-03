package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.model.TrackItem
import com.example.playlistmaker.domain.repository.SearchRepository
import com.example.playlistmaker.domain.usecase.AddToHistoryUseCase
import com.example.playlistmaker.domain.usecase.ClearHistoryUseCase
import com.example.playlistmaker.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.domain.usecase.SearchUseCase

class SearchInteractor(
    private val repository: SearchRepository
) : SearchUseCase, GetSearchHistoryUseCase, AddToHistoryUseCase, ClearHistoryUseCase {

    override val search: (String, (List<TrackItem>?, Throwable?) -> Unit) -> Unit =
        { query, callback ->
            repository.searchTracks(query, callback)
        }

    override val getHistory: () -> List<TrackItem> = {
        repository.getSearchHistory()
    }

    override val addTrack: (TrackItem) -> Unit = { track ->  // Имя изменено на addTrack
        repository.addToHistory(track)
    }

    override val clearHistory: () -> Unit = {
        repository.clearHistory()
    }
}

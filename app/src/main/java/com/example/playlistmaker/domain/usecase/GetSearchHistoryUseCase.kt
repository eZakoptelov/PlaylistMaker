package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.TrackItem
interface GetSearchHistoryUseCase {
    val getHistory: () -> List<TrackItem>
}

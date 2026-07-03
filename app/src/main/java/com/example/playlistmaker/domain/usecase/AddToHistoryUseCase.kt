package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.TrackItem

interface AddToHistoryUseCase {
    val addTrack: (TrackItem) -> Unit
}

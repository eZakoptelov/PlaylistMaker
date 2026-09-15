package com.example.playlistmaker.search.domain.usecase

import com.example.playlistmaker.search.domain.model.TrackItem

interface AddToHistoryUseCase {
  suspend  fun addTrack(track: TrackItem)
}

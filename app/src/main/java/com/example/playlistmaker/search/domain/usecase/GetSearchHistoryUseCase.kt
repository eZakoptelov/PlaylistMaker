package com.example.playlistmaker.search.domain.usecase

import com.example.playlistmaker.search.domain.model.TrackItem

interface GetSearchHistoryUseCase {
  suspend  fun getHistory(): List<TrackItem>
}

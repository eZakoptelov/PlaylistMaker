package com.example.playlistmaker.domain.usecase

import com.example.playlistmaker.domain.model.TrackItem

interface SearchUseCase {
    val search: (String, (List<TrackItem>?, Throwable?) -> Unit) -> Unit
}
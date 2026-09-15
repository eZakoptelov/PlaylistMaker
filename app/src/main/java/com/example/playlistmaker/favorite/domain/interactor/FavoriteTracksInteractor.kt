package com.example.playlistmaker.favorite.domain.interactor

import com.example.playlistmaker.search.domain.model.TrackItem
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksInteractor {
    suspend fun addTrack(track: TrackItem)
    suspend fun deleteTrack(track: TrackItem)
    fun getFavoriteTracks(): Flow<List<TrackItem>>
}

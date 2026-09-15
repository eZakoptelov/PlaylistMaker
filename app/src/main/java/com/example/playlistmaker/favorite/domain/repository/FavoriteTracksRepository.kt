package com.example.playlistmaker.favorite.domain.repository

import com.example.playlistmaker.search.domain.model.TrackItem
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksRepository {
    suspend fun addTrack(track: TrackItem)
    suspend fun deleteTrack(track: TrackItem)
    fun getFavoriteTracks(): Flow<List<TrackItem>>
}
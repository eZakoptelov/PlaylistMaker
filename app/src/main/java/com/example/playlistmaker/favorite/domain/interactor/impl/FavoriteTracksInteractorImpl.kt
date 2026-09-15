package com.example.playlistmaker.favorite.domain.interactor.impl

import com.example.playlistmaker.favorite.domain.interactor.FavoriteTracksInteractor
import com.example.playlistmaker.favorite.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.search.domain.model.TrackItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteTracksInteractorImpl(
    private val repository: FavoriteTracksRepository
) : FavoriteTracksInteractor {

    override suspend fun addTrack(track: TrackItem) {
        repository.addTrack(track)
    }

    override suspend fun deleteTrack(track: TrackItem) {
        repository.deleteTrack(track)
    }

    override fun getFavoriteTracks(): Flow<List<TrackItem>> {
        return repository.getFavoriteTracks()
            .map { tracks ->
                tracks.sortedByDescending { it.addedAt }
            }
    }
}

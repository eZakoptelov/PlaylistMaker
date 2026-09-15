package com.example.playlistmaker.favorite.data.repository.impl

import com.example.playlistmaker.favorite.data.mapper.toDomain
import com.example.playlistmaker.favorite.data.mapper.toEntity
import com.example.playlistmaker.favorite.data.dao.FavoriteTracksDao
import com.example.playlistmaker.favorite.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.search.domain.model.TrackItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(
    private val dao: FavoriteTracksDao
) : FavoriteTracksRepository {

    override suspend fun addTrack(track: TrackItem) {
        dao.addTrack(track.toEntity())
    }

    override suspend fun deleteTrack(track: TrackItem) {
        dao.deleteTrack(track.toEntity())
    }

    override fun getFavoriteTracks(): Flow<List<TrackItem>> {
        return dao.getFavoriteTracks()
            .distinctUntilChanged()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }
}

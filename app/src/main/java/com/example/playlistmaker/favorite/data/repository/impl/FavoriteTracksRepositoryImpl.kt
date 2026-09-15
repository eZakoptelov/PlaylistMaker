package com.example.playlistmaker.favorite.data.repository.impl

import com.example.playlistmaker.favorite.data.mapper.toDomain
import com.example.playlistmaker.favorite.data.mapper.toEntity
import com.example.playlistmaker.favorite.domain.repository.FavoriteTracksRepository
import com.example.playlistmaker.favorite.data.db.AppDatabase
import com.example.playlistmaker.search.domain.model.TrackItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(
    private val database: AppDatabase
) : FavoriteTracksRepository {

    override suspend fun addTrack(track: TrackItem) {
        database.favoriteTracksDao().addTrack(track.toEntity())
    }

    override suspend fun deleteTrack(track: TrackItem) {
        database.favoriteTracksDao().deleteTrack(track.toEntity())
    }

    override fun getFavoriteTracks(): Flow<List<TrackItem>> {
        return database.favoriteTracksDao().getFavoriteTracks()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }


}
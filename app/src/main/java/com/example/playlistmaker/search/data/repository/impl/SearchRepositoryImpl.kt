package com.example.playlistmaker.search.data.repository.impl

import com.example.playlistmaker.favorite.data.dao.FavoriteTracksDao
import com.example.playlistmaker.search.data.api.ItunesApi
import com.example.playlistmaker.search.data.mapper.TrackMapper
import com.example.playlistmaker.search.data.storage.HistoryStorage
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.search.domain.repository.SearchRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SearchRepositoryImpl(
    private val api: ItunesApi,
    private val storage: HistoryStorage,
    private val mapper: TrackMapper,
    private val favoriteDao: FavoriteTracksDao
) : SearchRepository {

    override fun searchTracks(query: String): Flow<Result<List<TrackItem>>> = flow {
        try {
            val response = api.searchSongs(query)
            if (response.isSuccessful && response.body() != null) {
                val domainResponse = mapper.toDomain(response.body()!!)
                val tracks = domainResponse.results

                val favoriteIds = favoriteDao.getFavoriteTrackIds().toSet()
                tracks.forEach { track ->
                    track.isFavorite = track.trackId in favoriteIds
                }

                emit(Result.success(tracks))
            } else {
                emit(Result.failure(Exception("API error: ${response.code()}")))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getSearchHistory(): List<TrackItem> {
        val tracks = storage.getHistory()
        val favoriteIds = favoriteDao.getFavoriteTrackIds().toSet()
        tracks.forEach { track ->
            track.isFavorite = track.trackId in favoriteIds
        }
        return tracks
    }

    override fun addToHistory(track: TrackItem) = storage.addToHistory(track)

    override fun clearHistory() = storage.clearHistory()
}

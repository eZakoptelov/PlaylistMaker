package com.example.playlistmaker.search.data.repository.impl

import com.example.playlistmaker.search.data.api.ItunesApi
import com.example.playlistmaker.search.data.mapper.TrackMapper
import com.example.playlistmaker.search.data.storage.HistoryStorage
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.search.domain.repository.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SearchRepositoryImpl(
    private val api: ItunesApi,
    private val storage: HistoryStorage,
    private val mapper: TrackMapper
) : SearchRepository {

    override fun searchTracks(query: String): Flow<Result<List<TrackItem>>> = flow {
        try {
            val response = api.searchSongs(query)
            if (response.isSuccessful && response.body() != null) {
                val domainResponse = mapper.toDomain(response.body()!!)
                emit(Result.success(domainResponse.results))
            } else {
                emit(Result.failure(Exception("API error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    override fun getSearchHistory(): List<TrackItem> = storage.getHistory()

    override fun addToHistory(track: TrackItem) = storage.addToHistory(track)

    override fun clearHistory() = storage.clearHistory()
}

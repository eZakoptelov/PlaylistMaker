package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.api.ItunesApi
import com.example.playlistmaker.data.dto.SearchResponseDto
import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.domain.model.TrackItem
import com.example.playlistmaker.domain.repository.SearchRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchRepositoryImpl(
    private val api: ItunesApi,
    private val searchHistory: SearchHistory,
    private val mapper: TrackMapper
) : SearchRepository {
    override fun searchTracks(query: String, callback: (List<TrackItem>?, Throwable?) -> Unit) {
        api.searchSongs(query).enqueue(object : Callback<SearchResponseDto> {
            override fun onResponse(call: Call<SearchResponseDto>, response: Response<SearchResponseDto>) {
                if (response.isSuccessful && response.body() != null) {
                    val domainResponse = mapper.toDomain(response.body()!!)
                    callback(domainResponse.results, null)
                } else {
                    callback(null, Exception("API error"))
                }
            }
            override fun onFailure(call: Call<SearchResponseDto>, t: Throwable) {
                callback(null, t)
            }
        })
    }

    override fun getSearchHistory(): List<TrackItem> = searchHistory.getHistory()

    override fun addToHistory(track: TrackItem) = searchHistory.addToHistory(track)

    override fun clearHistory() = searchHistory.clearHistory()
}
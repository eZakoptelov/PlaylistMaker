package com.example.playlistmaker.search.data.repository.impl
import com.example.playlistmaker.search.data.api.ItunesApi
import com.example.playlistmaker.search.data.dto.SearchResponseDto
import com.example.playlistmaker.search.data.mapper.TrackMapper
import com.example.playlistmaker.search.data.repository.SearchHistory
import com.example.playlistmaker.search.data.repository.SearchRepository
import com.example.playlistmaker.search.domain.model.TrackItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchRepositoryImpl(
    private val api: ItunesApi,
    private val searchHistory: SearchHistory,
private val mapper: TrackMapper
) : SearchRepository {

    override fun searchTracks(query: String, onResult: (Result<List<TrackItem>>) -> Unit) {
        api.searchSongs(query).enqueue(object : Callback<SearchResponseDto> {
            override fun onResponse(
                call: Call<SearchResponseDto>,
                response: Response<SearchResponseDto>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val domainResponse = mapper.toDomain(response.body()!!)
                    onResult(Result.success(domainResponse.results))
                } else {
                    onResult(Result.failure(Exception("API error: ${response.code()}")))
                }
            }

            override fun onFailure(call: Call<SearchResponseDto>, t: Throwable) {
                onResult(Result.failure(t))
            }
        })
    }

    override fun getSearchHistory(): List<TrackItem> = searchHistory.getHistory()

    override fun addToHistory(track: TrackItem) = searchHistory.addToHistory(track)

    override fun clearHistory() = searchHistory.clearHistory()
}

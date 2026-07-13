package com.example.playlistmaker.search.data.repository.impl
import com.example.playlistmaker.search.data.api.ItunesApi
import com.example.playlistmaker.search.data.dto.SearchResponseDto
import com.example.playlistmaker.search.data.mapper.TrackMapper
import com.example.playlistmaker.search.data.storage.HistoryStorage
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.search.domain.repository.SearchRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchRepositoryImpl(
    private val api: ItunesApi,
    private val storage: HistoryStorage,
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

    override fun getSearchHistory(): List<TrackItem> = storage.getHistory()

    override fun addToHistory(track: TrackItem) = storage.addToHistory(track)

    override fun clearHistory() = storage.clearHistory()
}

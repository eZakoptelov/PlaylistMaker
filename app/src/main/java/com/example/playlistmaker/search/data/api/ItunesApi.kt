package com.example.playlistmaker.search.data.api

import com.example.playlistmaker.search.data.dto.SearchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {

    @GET("search")
    suspend fun searchSongs(
        @Query("term") term: String,
        @Query("entity") entity: String = "song"
    ): Response<SearchResponseDto>
}

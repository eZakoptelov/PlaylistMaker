package com.example.playlistmaker.data.api

import com.example.playlistmaker.data.dto.SearchResponseDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {

    @GET("/search?entity=song")
    fun searchSongs(@Query("term") term: String): Call<SearchResponseDto>
}
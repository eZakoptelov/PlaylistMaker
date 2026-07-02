package com.example.playlistmaker.search.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ItunesApiFactory {
    private const val BASE_URL = "https://itunes.apple.com"

    fun create(): ItunesApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ItunesApi::class.java)
    }
}

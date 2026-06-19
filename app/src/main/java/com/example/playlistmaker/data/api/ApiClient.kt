package com.example.playlistmaker.data.api

import com.example.playlistmaker.utils.Constants.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val itunesApi: ItunesApi by lazy {
        retrofit.create(ItunesApi::class.java)
    }
}
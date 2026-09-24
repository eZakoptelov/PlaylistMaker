package com.example.playlistmaker.playlists.data.converters

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlaylistTrackConverter {
    private val gson = Gson()

    fun trackIdsToJson(trackIds: List<Long>): String {
        return gson.toJson(trackIds)
    }

    fun jsonToTrackIds(json: String): List<Long> {
        if (json.isEmpty()) return emptyList()
        val type = object : TypeToken<List<Long>>() {}.type
        return gson.fromJson(json, type)
    }
}

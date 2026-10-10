package com.example.playlistmaker.playlists.domain.model

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String,
    val coverPath: String,
    val trackCount: Int = 0
)
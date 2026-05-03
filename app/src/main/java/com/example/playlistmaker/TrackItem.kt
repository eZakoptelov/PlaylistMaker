package com.example.playlistmaker

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TrackItem(
    val trackName: String,
    val artistName: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String,
    val trackId: Long,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String,
    val country: String
) : Parcelable
{
    fun getCoverArtwork() = artworkUrl100.replaceAfterLast('/',"512x512bb.jpg")
}
data class SearchResponse(
    val resultCount: Int,
    val results: List<TrackItem>
)
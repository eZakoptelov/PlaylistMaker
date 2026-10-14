package com.example.playlistmaker.playlists.data.db.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface TracksDao {
    @Query("DELETE FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun deleteTrackById(trackId: Long)
}

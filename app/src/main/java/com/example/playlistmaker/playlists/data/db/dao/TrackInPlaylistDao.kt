package com.example.playlistmaker.playlists.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.playlistmaker.playlists.data.db.entity.TrackInPlaylistEntity

@Dao
interface TrackInPlaylistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: TrackInPlaylistEntity)

}

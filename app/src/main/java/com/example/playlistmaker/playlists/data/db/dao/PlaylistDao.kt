package com.example.playlistmaker.playlists.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.playlistmaker.playlists.data.db.entity.PlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long
    @Query("SELECT * FROM playlist_table WHERE id = :id")
    suspend fun getPlaylistById(id: Long): PlaylistEntity

    @Query("SELECT * FROM playlist_table ORDER BY id DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)
}
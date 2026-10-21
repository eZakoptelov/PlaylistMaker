package com.example.playlistmaker.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.playlistmaker.favorite.data.TrackEntity
import com.example.playlistmaker.favorite.data.db.dao.FavoriteTracksDao
import com.example.playlistmaker.playlists.data.db.dao.PlaylistDao
import com.example.playlistmaker.playlists.data.db.dao.TrackInPlaylistDao
import com.example.playlistmaker.playlists.data.db.dao.TracksDao
import com.example.playlistmaker.playlists.data.db.entity.PlaylistEntity
import com.example.playlistmaker.playlists.data.db.entity.TrackInPlaylistEntity

@Database(
    entities = [TrackEntity::class, PlaylistEntity::class,
        TrackInPlaylistEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteTracksDao(): FavoriteTracksDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun trackInPlaylistDao(): TrackInPlaylistDao
    abstract fun tracksDao(): TracksDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "playlistmaker_favorites.db"
            )
                .fallbackToDestructiveMigration(false)
                .build()
        }
    }
}
package com.example.playlistmaker.favorite.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.playlistmaker.favorite.data.TrackEntity
import com.example.playlistmaker.favorite.data.dao.FavoriteTracksDao

@Database(
    entities = [TrackEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteTracksDao(): FavoriteTracksDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "playlistmaker_favorites.db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
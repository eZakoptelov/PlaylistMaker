package com.example.playlistmaker.di

import com.example.playlistmaker.db.AppDatabase
import com.example.playlistmaker.playlists.data.db.dao.PlaylistDao
import com.example.playlistmaker.playlists.data.db.dao.TrackInPlaylistDao
import com.example.playlistmaker.playlists.data.db.dao.TracksDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single { AppDatabase.create(androidContext()) }

    single<PlaylistDao> { get<AppDatabase>().playlistDao() }
    single<TrackInPlaylistDao> { get<AppDatabase>().trackInPlaylistDao() }
    single { get<AppDatabase>().favoriteTracksDao() }
    single<TracksDao> { get<AppDatabase>().tracksDao() }

}

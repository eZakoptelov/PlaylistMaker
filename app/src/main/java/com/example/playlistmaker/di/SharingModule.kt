package com.example.playlistmaker.di

import com.example.playlistmaker.sharing.data.repository.impl.SharingRepositoryImpl
import com.example.playlistmaker.sharing.domain.SharingUseCase
import org.koin.dsl.module

val sharingModule = module {
    factory<SharingUseCase> {
        SharingRepositoryImpl(
            context = get()
        )
    }
}


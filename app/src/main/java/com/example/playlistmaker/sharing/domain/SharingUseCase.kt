package com.example.playlistmaker.sharing.domain

import android.content.Intent

interface SharingUseCase {
    fun shareApp(): Intent
    fun openSupport(): Intent
    fun openAgreement(): Intent
}
package com.example.playlistmaker.sharing.domain.interactor

import android.content.Intent

interface SharingInteractor {
    fun shareApp(): Intent
    fun openSupport():Intent
    fun openAgreement():Intent
}
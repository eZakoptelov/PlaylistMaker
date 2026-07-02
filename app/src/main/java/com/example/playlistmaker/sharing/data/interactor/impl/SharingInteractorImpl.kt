package com.example.playlistmaker.sharing.data.interactor.impl

import android.content.Intent
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor
import com.example.playlistmaker.utils.Constants
import androidx.core.net.toUri

class SharingInteractorImpl : SharingInteractor {

    override fun shareApp(): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getShareText())
        }
    }

    override fun openSupport(): Intent {
        val mailTo = "mailto:${Constants.SUPPORT_EMAIL}?subject=Сообщение разработчикам и разработчицам приложения Playlist Maker"
        return Intent(Intent.ACTION_VIEW, mailTo.toUri())
    }

    override fun openAgreement(): Intent {
        return Intent(Intent.ACTION_VIEW, Constants.AGREEMENT_URL.toUri())
    }

    private fun getShareText() =
        "Спасибо разработчикам и разработчицам за крутое приложение!"
}

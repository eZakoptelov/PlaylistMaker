package com.example.playlistmaker.sharing.data.repository.impl

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.example.playlistmaker.sharing.domain.SharingUseCase
import com.example.playlistmaker.utils.Constants

class SharingRepositoryImpl(
    private val context: Context
) : SharingUseCase {

    override fun shareApp(): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getShareText())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    override fun openSupport(): Intent {
        val mailTo = "mailto:${Constants.SUPPORT_EMAIL}?subject=Сообщение разработчикам и разработчицам приложения Playlist Maker"
        return Intent(Intent.ACTION_VIEW, mailTo.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    override fun openAgreement(): Intent {
        return Intent(Intent.ACTION_VIEW, Constants.AGREEMENT_URL.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun getShareText(): String =
        "Спасибо разработчикам и разработчицам за крутое приложение!"
}

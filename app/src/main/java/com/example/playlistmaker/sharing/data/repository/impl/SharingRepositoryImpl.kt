package com.example.playlistmaker.sharing.data.repository.impl

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.example.playlistmaker.sharing.domain.SharingUseCase
import com.example.playlistmaker.utils.Constants
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SharingRepositoryImpl(
    private val context: Context
) : SharingUseCase {

    override fun shareApp(): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getShareText())
        }
    }

    override fun openSupport(): Intent {
        val subject = URLEncoder.encode(
            "Сообщение разработчикам и разработчицам приложения Playlist Maker",
            StandardCharsets.UTF_8.name()
        )

        val mailUri = "mailto:${Constants.SUPPORT_EMAIL}?subject=$subject".toUri()

        return Intent(Intent.ACTION_SENDTO, mailUri)
    }

    override fun openAgreement(): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Constants.AGREEMENT_URL.toUri()
        )
    }

    private fun getShareText(): String =
        "Спасибо разработчикам и разработчицам за крутое приложение!"
}

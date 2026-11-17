package com.example.playlistmaker.utils.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NetworkChangeReceiver(
    private val isInternetAvailable: (Context) -> Boolean
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        if (intent?.action != ACTION_CONNECTIVITY_CHANGE) return

        if (!isInternetAvailable(context)) {
            Toast.makeText(
                context,
                "Отсутствует подключение к интернету",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        const val ACTION_CONNECTIVITY_CHANGE =
            "android.net.conn.CONNECTIVITY_CHANGE"
    }
}
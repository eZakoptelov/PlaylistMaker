package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        // Кнопка возврата назад
        val backButton = findViewById<Button>(R.id.ic_vector_buck)
        backButton.setOnClickListener {
            finish() // Завершить текущую активность
            Toast.makeText(this, getString(R.string.back), Toast.LENGTH_SHORT).show()
        }

        // Кнопка шаринга
        val shareApp = findViewById<TextView>(R.id.shareApp)
        shareApp.setOnClickListener {
            val courseLink = String.format(
                getString(R.string.android_course_url),
                getString(R.string.course_url)
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, courseLink)
            }
            startActivity(
                Intent.createChooser(
                    shareIntent,
                    getString(R.string.choose_app_to_share)
                )
            )
        }


        // Кнопка оферты
        val userAgreementButton = findViewById<TextView>(R.id.userAgreementButton)
        userAgreementButton.setOnClickListener {
            val agreementUrl = getString(R.string.agreement_url)
            val browserIntent = Intent(Intent.ACTION_VIEW, agreementUrl.toUri())
            startActivity(browserIntent)
        }
        // Кнопка поддержки
        val supportButton = findViewById<TextView>(R.id.supportButton)
        supportButton.setOnClickListener {
            openEmailClient()
        }

    }

    private fun openEmailClient() {
        val emailAddress = getString(R.string.email_address)
        val subject = getString(R.string.email_subject)
        val messageBody = getString(R.string.email_body)

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, messageBody)
        }
        startActivity(intent)
    }
}
package com.example.playlistmaker.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.App
import com.example.playlistmaker.R
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.core.net.toUri

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_product)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Кнопка переключения тем
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)
        themeSwitcher.isChecked = (applicationContext as App).darkTheme
        themeSwitcher.setOnCheckedChangeListener { _, checked ->
            (applicationContext as App).switchTheme(checked)
        }

        // Кнопка возврата
        val backButton = findViewById<Button>(R.id.ic_vector_buck)
        backButton.setOnClickListener {
            finish()
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

        if (emailAddress.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, messageBody)
            }

            try {
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(Intent.createChooser(intent, "Выберите почтовый клиент"))
                } else {
                    Toast.makeText(
                        this,
                        "На устройстве не установлен почтовый клиент",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (_: Exception) {
                Toast.makeText(
                    this,
                    "Ошибка при открытии почтового клиента",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this,
                "Не указан адрес электронной почты",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}
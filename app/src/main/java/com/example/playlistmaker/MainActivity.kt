package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupListeners()
    }

    private fun bindViews() {
        findViewById<Button>(R.id.button_search)
        findViewById<Button>(R.id.button_mediateka)
        findViewById<Button>(R.id.button_settings)
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.button_search).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
            toastMessage(getString(R.string.search))
        }

        findViewById<Button>(R.id.button_mediateka).setOnClickListener {
            startActivity(Intent(this, MediaActivity::class.java))
            toastMessage(getString(R.string.media_library))
        }

        findViewById<Button>(R.id.button_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            toastMessage(getString(R.string.settings))
        }
    }

    private fun toastMessage(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
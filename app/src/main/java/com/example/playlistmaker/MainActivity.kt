package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonSearch = findViewById<Button>(R.id.button_search)
        buttonSearch.setOnClickListener(View.OnClickListener { view ->
            val searchIntent = Intent(this, SearchActivity::class.java)
            startActivity(searchIntent)
            makeText(this@MainActivity, "Поиск", Toast.LENGTH_SHORT).show()
        })

        val buttonMediateka = findViewById<Button>(R.id.button_mediateka)
        buttonMediateka.setOnClickListener {
            val mediaIntent = Intent(this, MediaActivity::class.java)
            startActivity(mediaIntent)
            makeText(this@MainActivity, "Медиатека", Toast.LENGTH_SHORT).show()
        }


        val buttonSettings = findViewById<Button>(R.id.button_settings)
        buttonSettings.setOnClickListener {
            val settingIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingIntent)
                makeText(this@MainActivity, "Настройки", Toast.LENGTH_SHORT).show()

        }

    }
}


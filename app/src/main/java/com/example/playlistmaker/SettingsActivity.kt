package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val icVectorBuck = findViewById<Button>(R.id.ic_vector_buck)
        icVectorBuck.setOnClickListener {
            val icVector = Intent(this, MainActivity::class.java)
            startActivity(icVector)
            makeText(this@SettingsActivity, "Назад", Toast.LENGTH_SHORT).show()
            finish()
        }

    }


}
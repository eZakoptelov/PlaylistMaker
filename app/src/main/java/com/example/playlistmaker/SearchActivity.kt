package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SearchActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var backButton: Button

    companion object {
        const val SEARCH_TEXT = "search_text"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        editText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.imageButtonSearchClear)
        backButton = findViewById(R.id.ic_vector_buck)

        restoreSearchText(savedInstanceState)

        // Настройка поведения кнопки возврата
        backButton.setOnClickListener {
            navigateToMainActivity()
        }

        // Настройка кнопки очистки
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = if (s.isNullOrBlank()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Очистим текст при нажатии на кнопку
        clearButton.setOnClickListener {
            editText.text.clear()
        }
    }

    private fun navigateToMainActivity() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent)
        Toast.makeText(this, getString(R.string.back), Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun restoreSearchText(bundle: Bundle?) {
        bundle?.getString(SEARCH_TEXT)?.let { editText.setText(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT, editText.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreSearchText(savedInstanceState)
    }
}
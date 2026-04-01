package com.example.playlistmaker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var backButton: Button

    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        editText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.imageButtonSearchClear)
        backButton = findViewById(R.id.ic_vector_buck)
        tracksRecyclerView = findViewById(R.id.tracksRecyclerView)


        restoreSearchText(savedInstanceState)

        // Настройка поведения кнопки возврата
        backButton.setOnClickListener {
            navigateToMainActivity()
        }

        // Настройка кнопки очистки
        editText.doOnTextChanged { text, _, _, _ ->
            clearButton.visibility = if (text.isNullOrBlank()) View.GONE else View.VISIBLE
            updateSearchResults(text.toString())
        }

        // Очистим текст при нажатии на кнопку
        clearButton.setOnClickListener {
            editText.text.clear()
            hideKeyboard(editText)
            tracksRecyclerView.visibility = View.GONE
        }
        // Подключение адаптера
        trackAdapter = TrackAdapter(MockData.mockTracks as MutableList<Track>)
        tracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = trackAdapter
            tracksRecyclerView.visibility = View.GONE
        }
    }

    // Метод для обновления результатов поиска
    private fun updateSearchResults(query: String) {

        if (query.isNotEmpty()) {
            // Показываем список треков
            tracksRecyclerView.visibility = View.VISIBLE

            // Фильтруем треки по введённой строке
            val filteredTracks = MockData.mockTracks.filter {
                it.trackName.contains(query, ignoreCase = true) ||
                        it.artistName.contains(query, ignoreCase = true)
            }


            // Обновляем адаптер
            trackAdapter.submitList(filteredTracks)
        } else {
            // Если поле пустое, скроем список
            tracksRecyclerView.visibility = View.GONE
        }
    }

    // Возвращаемся в главную активность
    private fun navigateToMainActivity() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent)
        Toast.makeText(this, getString(R.string.back), Toast.LENGTH_SHORT).show()
        finish()
    }

    // Восстановление текста поиска
    private fun restoreSearchText(bundle: Bundle?) {
        bundle?.getString(SEARCH_TEXT)?.let { editText.setText(it) }
    }

    // Сохранение состояния поиска
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT, editText.text.toString())
    }

    // Восстановление состояния поиска
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreSearchText(savedInstanceState)
    }

    // Скрытие клавиатуры
    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    companion object {
        const val SEARCH_TEXT = "search_text_key"
    }
}
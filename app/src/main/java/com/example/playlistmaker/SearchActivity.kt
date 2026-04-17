package com.example.playlistmaker

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {
    private lateinit var editText: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var backButton: Button

    private lateinit var tracksRecyclerView: RecyclerView
    private lateinit var stateErrorConnection: LinearLayout
    private lateinit var stateNothingFound: LinearLayout

    private lateinit var buttonConnection: TextView

    // Адаптер и список данных
    private val tracks = mutableListOf<TrackItem>()
    private val trackAdapter = TrackAdapter(tracks)

    // Переменные для логики повтора запроса
    private var lastSearchQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MY_SEARCH", "--- SearchActivity ЗАПУЩЕН ---")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        editText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.imageButtonSearchClear)
        backButton = findViewById(R.id.ic_vector_buck)
        tracksRecyclerView = findViewById(R.id.tracksRecyclerView)
        stateErrorConnection = findViewById(R.id.state_error_connection)
        stateNothingFound = findViewById(R.id.state_nothing_found)
        buttonConnection = findViewById(R.id.button_connection)


        // Обработчик кнопки «Обновить»
        buttonConnection.setOnClickListener {
            Log.d("BUTTON", "Клик обработан!")  // Логируем нажатие
            lastSearchQuery?.let { query ->
                val apiService = ApiClient.itunesApi
                performSearch(apiService, query)
            } ?: run {
                Toast.makeText(this, "Нет предыдущего запроса для повтора", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val apiService = ApiClient.itunesApi

        // --- НАСТРОЙКА РЕКВЬЮ И АДАПТЕРА ---
        tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        tracksRecyclerView.adapter = trackAdapter

        // --- ЛОГИКА КНОПОК И СЛУШАТЕЛЕЙ ---

        // Настройка поведения кнопки возврата
        backButton.setOnClickListener {
            navigateToMainActivity()
        }

        // Настройка кнопки очистки
        editText.doOnTextChanged { text, _, _, _ ->
            clearButton.visibility = if (text.isNullOrBlank()) View.GONE else View.VISIBLE
        }

        // Очистим текст при нажатии на кнопку
        clearButton.setOnClickListener {
            editText.text.clear()
            hideKeyboard(editText)
            tracks.clear()
            trackAdapter.notifyDataSetChanged()
            // Скрываем все состояния
            tracksRecyclerView.visibility = View.GONE
            stateErrorConnection.visibility = View.GONE
            stateNothingFound.visibility = View.GONE
        }


        editText.setOnEditorActionListener { _, actionId, event ->
            // Проверяем, что нажата либо кнопка "Готово"
            val isActionDone = actionId == EditorInfo.IME_ACTION_DONE


            if (isActionDone) {
                val query = editText.text.toString().trim()
                lastSearchQuery = query
                if (query.isNotBlank()) {
                    performSearch(apiService, query)
                    hideKeyboard(editText)
                } else {
                    Toast.makeText(this, "Введите поисковый запрос", Toast.LENGTH_SHORT).show()
                }
                // Возвращаем true, так как мы обработали нажатие (в любом случае)
                return@setOnEditorActionListener true
            }
            // Если это было другое действие, возвращаем false
            return@setOnEditorActionListener false
        }


        // --- ВОССТАНОВЛЕНИЕ СОСТОЯНИЯ ПРИ ПОВОРОТЕ ЭКРАНА ---
        restoreSearchText(savedInstanceState)
    }


    private fun performSearch(apiService: ItunesApi, query: String) {
        saveSearchQueryAndLog(query)

        apiService.searchSongs(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                Log.d("SEARCH_API", "Ответ получен: ${response.code()}")
                if (response.isSuccessful && response.body() != null) {
                    handleSuccessfulResponse(response)
                } else {
                    showErrorState()
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                handleNetworkError(t)
            }
        })
    }

    //Обновление UI с результатами
    private fun updateUIWithResults(trackList: List<TrackItem>?) {
        if (!trackList.isNullOrEmpty()) {
            trackAdapter.submitList(trackList)
            Log.d(
                "MY_SEARCH",
                "Адаптер обновлён, текущий размер списка: ${trackList.size}"
            )
            showList()
        } else {
            showEmptyState()
        }
    }

    //Логирование результатов поиска
    private fun logSearchResults(trackList: List<TrackItem>?) {
        Log.d(
            "MY_SEARCH",
            "Успешный ответ API, найдено треков: ${trackList?.size ?: 0}"
        )

        trackList?.firstOrNull()?.let { firstTrack ->
            Log.d(
                "MY_SEARCH",
                "Первый трек: ${firstTrack.trackName} by ${firstTrack.artistName}"
            )
        }
    }

    //Обработка ошибки сети
    private fun handleNetworkError(t: Throwable) {
        Log.e("SEARCH_API", "Ошибка сети: ${t.message}")
        showErrorState()
    }

    //Обработка успешного ответа API
    private fun handleSuccessfulResponse(response: Response<SearchResponse>) {
        val searchResponse = response.body()
        val trackList = searchResponse?.results

        logSearchResults(trackList)
        updateUIWithResults(trackList)
    }

    //  Сохранение запроса и логирование
    private fun saveSearchQueryAndLog(query: String) {
        lastSearchQuery = query
        Log.d("SEARCH_API", "Запрос отправлен: $query")
    }


    private fun showList() {
        tracksRecyclerView.visibility = View.VISIBLE
        stateErrorConnection.visibility = View.GONE
        stateNothingFound.visibility = View.GONE
    }

    private fun showEmptyState() {
        tracksRecyclerView.visibility = View.GONE
        stateErrorConnection.visibility = View.GONE
        stateNothingFound.visibility = View.VISIBLE  // Плейсхолдер "нет результатов"
    }


    private fun showErrorState() {
        tracksRecyclerView.visibility = View.GONE
        stateErrorConnection.visibility = View.VISIBLE // Плейсхолдер "ошибка сервера"
        stateNothingFound.visibility = View.GONE
    }


    private fun navigateToMainActivity() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent)
        finish()
    }

    private fun restoreSearchText(bundle: Bundle?) {
        bundle?.getString(SEARCH_TEXT)?.let { editText.setText(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT, editText.text.toString())
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    companion object {
        const val SEARCH_TEXT = "search_text_key"
    }
}
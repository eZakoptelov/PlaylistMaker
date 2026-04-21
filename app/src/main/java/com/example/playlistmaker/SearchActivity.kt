package com.example.playlistmaker

import android.content.Intent
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
    private lateinit var historyContainer: LinearLayout
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var clearHistoryButton: TextView
    private lateinit var searchHistory: SearchHistory
    private lateinit var historyAdapter: TrackAdapter
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

        // Инициализация View-элементов
        editText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.imageButtonSearchClear)
        backButton = findViewById(R.id.ic_vector_buck)
        tracksRecyclerView = findViewById(R.id.tracksRecyclerView)
        stateErrorConnection = findViewById(R.id.state_error_connection)
        stateNothingFound = findViewById(R.id.state_nothing_found)
        buttonConnection = findViewById(R.id.button_connection)
        historyContainer = findViewById(R.id.historyContainer)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)

        // Инициализация истории поиска
        searchHistory = SearchHistory(getSharedPreferences("app_prefs", MODE_PRIVATE))

        // Настройка адаптера для истории
        historyAdapter = TrackAdapter(emptyList())
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
        historyAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(track: TrackItem) {
                performSearch(ApiClient.itunesApi, track.trackName)
            }
        })

// Настройка основного адаптера
        tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        tracksRecyclerView.adapter = trackAdapter

        setupHistoryDisplay()
        setupHistoryClearButton()
        setupSearchFieldListeners()

        // Обработчик кнопки «Обновить»
        buttonConnection.setOnClickListener {
            Log.d("BUTTON", "Клик обработан!")
            lastSearchQuery?.let { query ->
                val apiService = ApiClient.itunesApi
                performSearch(apiService, query)
            } ?: run {
                Toast.makeText(this, "Нет предыдущего запроса для повтора", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val apiService = ApiClient.itunesApi

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
                return@setOnEditorActionListener true
            }
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
                    val trackList = response.body()?.results
                    // Сохраняем запрос в историю вместо треков
                    trackList?.firstOrNull()?.let { firstTrack ->
                        searchHistory.addToHistory(firstTrack)
                    }
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

    // Показать историю при восстановлении текста
    private fun restoreSearchText(bundle: Bundle?) {
        bundle?.getString(SEARCH_TEXT)?.let {
            editText.setText(it)
            setupHistoryDisplay()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val searchText = editText.text.toString()
        if (searchText.isNotBlank()) {
            outState.putString(SEARCH_TEXT, searchText)
        }
    }
    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setupHistoryDisplay() {
        Log.d("HISTORY", "setupHistoryDisplay вызван")
        val history = searchHistory.getHistory()
        Log.d("HISTORY", "Размер истории: ${history.size}")

        if (history.isNotEmpty()) {
            historyAdapter.submitList(history)
            Log.d("HISTORY", "Адаптер истории обновлён")
            updateHistoryVisibility(true)
            Log.d("HISTORY", "История показана")
        } else {
            updateHistoryVisibility(false)
            Log.d("HISTORY", "История скрыта (пустая)")
        }
    }


    private fun updateHistoryVisibility(show: Boolean) {
        historyContainer.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setupSearchFieldListeners() {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                setupHistoryDisplay() // Показываем историю при фокусе
            }
        }

        editText.doOnTextChanged { text, _, _, _ ->
            clearButton.visibility = if (text.isNullOrBlank()) View.GONE else View.VISIBLE

            // Показываем историю, если:
            // 1) EditText в фокусе
            // 2) Текст пустой/null
            if (editText.hasFocus() && text.isNullOrBlank()) {
                setupHistoryDisplay()
            }
            // Скрываем историю, если текст введён
            else if (editText.hasFocus() && text.toString().isNotBlank()) {
                updateHistoryVisibility(false)
            }
        }
    }


    private fun setupHistoryClearButton() {
        clearHistoryButton.setOnClickListener {
            searchHistory.clearHistory()
            updateHistoryVisibility(false)
            Toast.makeText(this, "История очищена", Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        const val SEARCH_TEXT = "search_text_key"
    }
}
package com.example.playlistmaker.presentation.activity

import android.R.attr.track
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.App
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.adapter.TrackAdapter
import com.example.playlistmaker.data.dto.SearchResponseDto
import com.example.playlistmaker.data.mapper.TrackMapper
import com.example.playlistmaker.data.repository.SearchHistory
import com.example.playlistmaker.domain.model.TrackItem
import com.example.playlistmaker.domain.usecase.AddToHistoryUseCase
import com.example.playlistmaker.domain.usecase.ClearHistoryUseCase
import com.example.playlistmaker.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.domain.usecase.SearchUseCase
import com.example.playlistmaker.utils.Constants.EXTRA_TRACK
import com.example.playlistmaker.utils.Constants.SEARCH_DEBOUNCE_DELAY
import com.example.playlistmaker.utils.Constants.SEARCH_TEXT
import retrofit2.Response
import com.example.playlistmaker.presentation.adapter.OnItemClickListener


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
    private lateinit var searchProgressBar: ProgressBar
    private lateinit var searchUseCase: SearchUseCase
    private lateinit var getHistoryUseCase: GetSearchHistoryUseCase
    private lateinit var addToHistoryUseCase: AddToHistoryUseCase
    private lateinit var clearHistoryUseCase: ClearHistoryUseCase
    private val trackMapper = TrackMapper()


    // Адаптер и список данных
    private val tracks = mutableListOf<TrackItem>()
    private val trackAdapter = TrackAdapter(tracks)

    // Переменные для логики повтора запроса
    private var lastSearchQuery: String? = null
    private var searchDebounce: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())
    private var lastClickTime: Long = 0
    private val CLICK_DEBOUNCE_DELAY = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search_product)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Log.d("MY_SEARCH", "--- SearchActivity ЗАПУЩЕН ---")

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
        searchProgressBar = findViewById(R.id.progressBar)

        // Явно скрываем историю при старте активности
        updateHistoryVisibility(false)

        val useCaseCreator = (applicationContext as App).useCaseCreator
        searchUseCase = useCaseCreator.createSearchUseCase()
        getHistoryUseCase = useCaseCreator.createGetSearchHistoryUseCase()
        addToHistoryUseCase = useCaseCreator.createAddToHistoryUseCase()
        clearHistoryUseCase = useCaseCreator.createClearHistoryUseCase()

        // Настройка адаптера для истории
        historyAdapter = TrackAdapter(emptyList())
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        //Адаптер истории
        historyAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(track: TrackItem) {
                if (!isClickAllowed()) {
                    return
                }
                addToHistoryUseCase.addTrack(track)
                openPlayerActivity(track) // Переходим в плеер
            }
        })



// Настройка основного адаптера
        tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        tracksRecyclerView.adapter = trackAdapter

        trackAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(track: TrackItem) {
                if (!isClickAllowed()) {
                    return
                }
                addToHistoryUseCase.addTrack(track)
                openPlayerActivity(track) // Переходим в плеер
            }
        })

        setupHistoryClearButton()
        setupSearchFieldListeners()

        // Обработчик кнопки «Обновить»
        buttonConnection.setOnClickListener {
            Log.d("BUTTON", "Клик обработан!")
            lastSearchQuery?.let { query ->
                showSearchProgress()
                performSearch(query)
            } ?: run {
                Toast.makeText(
                    this,
                    "Нет предыдущего запроса для повтора",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        // Настройка поведения кнопки возврата
        backButton.setOnClickListener {
            navigateToMainActivity()
        }

        // Настройка кнопки очистки
        editText.doOnTextChanged { text, _, _, _ ->
            clearButton.visibility = if (text.isNullOrBlank()) View.GONE else View.VISIBLE
            val query = text.toString().trim()
            if (query.isNotBlank()) {
                // Отменяем предыдущий отложенный вызов

                searchDebounce?.let { handler.removeCallbacks(it) }

                // Создаём новый Runnable для выполнения поиска
                searchDebounce = Runnable {
                    lastSearchQuery = query
                    performSearch(query)
                }

                // Логируем создание нового Runnable
                Log.d("DEBOUNCE", "Создан новый Runnable для запроса: '$query'")

                // Запланируем выполнение через заданную задержку (безопасный вызов)

                searchDebounce?.let { handler.postDelayed(it,SEARCH_DEBOUNCE_DELAY) }
            } else {
                searchDebounce?.let { handler.removeCallbacks(it) }
                searchDebounce = null
            }
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

        editText.setOnEditorActionListener { _, actionId, _ ->
            // Проверяем, что нажата либо кнопка "Готово"
            val isActionDone = actionId == EditorInfo.IME_ACTION_DONE

            if (isActionDone) {
                searchDebounce?.let { handler.removeCallbacks(it) }
                searchDebounce = null
                val query = editText.text.toString().trim()
                lastSearchQuery = query
                if (query.isNotBlank()) {
                    performSearch(query)
                    hideKeyboard(editText)
                } else {
                    Toast.makeText(this, "Введите поисковый запрос", Toast.LENGTH_SHORT)
                        .show()
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        // --- ВОССТАНОВЛЕНИЕ СОСТОЯНИЯ ПРИ ПОВОРОТЕ ЭКРАНА ---
        restoreSearchText(savedInstanceState)
    }


    private fun performSearch(query: String) {
        saveSearchQueryAndLog(query)
        showSearchProgress()

        searchUseCase.search(query) { tracks, error ->
            hideSearchProgress()
            if (error != null) {
                showErrorState()
                return@search
            }

            tracks?.let {
                // Добавляем первый трек в историю, если он есть
                it.firstOrNull()?.let { track ->
                    addToHistoryUseCase.addTrack(track)
                }
                updateUIWithResults(it)
            } ?: run {
                showEmptyState()
            }
        }
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
        Log.d("MY_SEARCH", "Успешный ответ API, найдено треков: ${trackList?.size ?: 0}")
        trackList?.firstOrNull()?.let { firstTrack ->
            Log.d("MY_SEARCH", "Первый трек: ${firstTrack.trackName} by ${firstTrack.artistName}")
        }
    }

    private fun handleSuccessfulResponse(response: Response<SearchResponseDto>) {
        val searchResponse = response.body()
        val trackListDto = searchResponse?.results

        // Преобразуем DTO в доменные объекты
        val trackList = trackListDto?.map { trackMapper.toDomain(it) }

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
        val inputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setupHistoryDisplay() {
        val history = getHistoryUseCase.getHistory()
        historyAdapter.submitList(history)

        updateHistoryList(history)  // Ошибка: метода нет
        if (history.isNotEmpty()) {
            historyAdapter.submitList(history)
            updateHistoryVisibility(true)
        } else {
            updateHistoryVisibility(false)
        }
    }




    private fun updateHistoryVisibility(show: Boolean) {
        historyContainer.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setupSearchFieldListeners() {
        // Обработчик фокуса
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // При получении фокуса: показываем историю, только если текст пуст
                val currentText = editText.text?.toString() ?: ""
                if (currentText.isBlank()) {
                    setupHistoryDisplay()
                    hideSearchResults()
                }
            } else {
                // При потере фокуса: всегда скрываем историю
                updateHistoryVisibility(false)
            }
        }

        // Обработчик изменения текста
        editText.doOnTextChanged { text, _, _, _ ->
            val stringText = text?.toString() ?: ""

            // Показ/скрытие иконки очистки
            clearButton.visibility = if (stringText.isBlank()) View.GONE else View.VISIBLE

            // Если поле не в фокусе — не трогаем историю (она уже скрыта)
            if (!editText.hasFocus()) return@doOnTextChanged

            // Если в фокусе: при пустом тексте показываем историю, иначе скрываем
            if (stringText.isBlank()) {
                setupHistoryDisplay()
                hideSearchResults()
            } else {
                updateHistoryVisibility(false)
            }
        }
    }


    private fun setupHistoryClearButton() {
        clearHistoryButton.setOnClickListener {
            clearHistoryUseCase.clearHistory()
            updateHistoryVisibility(false)
            refreshHistoryDisplay() // Обновляем отображение истории
            Toast.makeText(this, "История очищена", Toast.LENGTH_SHORT).show()
        }
    }


    private fun hideSearchResults() {
        tracksRecyclerView.visibility = View.GONE
        stateErrorConnection.visibility = View.GONE
        stateNothingFound.visibility = View.GONE
    }

    private fun openPlayerActivity(track: TrackItem) {
        try {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra(EXTRA_TRACK, track)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun isClickAllowed(): Boolean {
        val currentTime = System.currentTimeMillis()
        return if (currentTime - lastClickTime < CLICK_DEBOUNCE_DELAY) {
            false
        } else {
            lastClickTime = currentTime
            true
        }
    }
    private fun showSearchProgress() {
        searchProgressBar.visibility = View.VISIBLE
        tracksRecyclerView.visibility = View.GONE
        stateErrorConnection.visibility = View.GONE
        stateNothingFound.visibility = View.GONE
    }

    private fun hideSearchProgress() {
        searchProgressBar.visibility = View.GONE
    }
    private fun refreshHistoryDisplay() {
        val history = getHistoryUseCase.getHistory()
        historyAdapter.submitList(history)

        historyAdapter.submitList(history)
        updateHistoryVisibility(history.isNotEmpty())
    }
    private fun updateHistoryList(history: List<TrackItem>) {
        historyAdapter.submitList(history)
    }
}
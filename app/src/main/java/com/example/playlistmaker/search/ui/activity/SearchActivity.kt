package com.example.playlistmaker.search.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.App
import com.example.playlistmaker.R
import com.example.playlistmaker.main.ui.activity.MainActivity
import com.example.playlistmaker.player.ui.activity.PlayerActivity
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.search.ui.adapter.OnItemClickListener
import com.example.playlistmaker.search.ui.adapter.TrackAdapter
import com.example.playlistmaker.search.ui.viewmodel.SearchUiState
import com.example.playlistmaker.search.ui.viewmodel.SearchViewModel
import com.example.playlistmaker.search.ui.viewmodel.SearchViewModelFactory
import com.example.playlistmaker.utils.Constants

class SearchActivity : AppCompatActivity() {

    private lateinit var viewModel: SearchViewModel
    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    private lateinit var editText: android.widget.EditText
    private lateinit var clearButton: ImageButton
    private lateinit var backButton: Button
    private lateinit var tracksRecyclerView: RecyclerView

    private lateinit var stateErrorConnection: LinearLayout
    private lateinit var stateNothingFound: LinearLayout
    private lateinit var buttonConnection: TextView

    private lateinit var searchProgressBar: ProgressBar
    private lateinit var historyContainer: LinearLayout
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var clearHistoryButton: TextView

    // Debounce
    private var searchDebounce: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())
    private val debounceDelay = Constants.SEARCH_DEBOUNCE_DELAY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        setupWindowInsets()
        bindViews()

        // Инициализация адаптеров
        adapter = TrackAdapter(emptyList())
        historyAdapter = TrackAdapter(emptyList())

        tracksRecyclerView.layoutManager = LinearLayoutManager(this)
        tracksRecyclerView.adapter = adapter

        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        // Скрываем историю по умолчанию
        historyContainer.visibility = View.GONE

        setupListeners()

        // Инициализация ViewModel через ViewModelProvider (фабрика)
        val useCaseCreator = (applicationContext as App).useCaseCreator
        val factory = SearchViewModelFactory(
            useCaseCreator.createSearchUseCase(),
            useCaseCreator.createGetSearchHistoryUseCase(),
            useCaseCreator.createAddToHistoryUseCase(),
            useCaseCreator.createClearHistoryUseCase()
        )
        viewModel = ViewModelProvider(this, factory)[SearchViewModel::class.java]


        viewModel.uiState.observe(this) { state ->
            when (state) {
                is SearchUiState.Idle -> {
                    // Ничего не делаем: состояние по умолчанию — пустая страница / история при фокусе
                }

                is SearchUiState.Loading -> {
                    searchProgressBar.visibility = View.VISIBLE
                    tracksRecyclerView.visibility = View.GONE
                    stateErrorConnection.visibility = View.GONE
                    stateNothingFound.visibility = View.GONE
                    historyContainer.visibility = View.GONE
                }

                is SearchUiState.Success -> {
                    searchProgressBar.visibility = View.GONE
                    adapter.submitList(state.tracks)
                    tracksRecyclerView.visibility = View.VISIBLE
                    stateErrorConnection.visibility = View.GONE
                    stateNothingFound.visibility = View.GONE
                    historyContainer.visibility = View.GONE
                }

                is SearchUiState.Error -> {
                    searchProgressBar.visibility = View.GONE
                    tracksRecyclerView.visibility = View.GONE
                    stateErrorConnection.visibility = View.VISIBLE
                    stateNothingFound.visibility = View.GONE
                    historyContainer.visibility = View.GONE

                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }

                is SearchUiState.History -> {
                    searchProgressBar.visibility = View.GONE
                    tracksRecyclerView.visibility = View.GONE
                    stateErrorConnection.visibility = View.GONE
                    stateNothingFound.visibility = View.GONE

                    if (state.history.isEmpty()) {
                        historyContainer.visibility = View.GONE
                    } else {
                        historyContainer.visibility = View.VISIBLE
                        historyAdapter.submitList(state.history)
                    }
                }
            }
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search_product)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun bindViews() {
        editText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.imageButtonSearchClear)
        backButton = findViewById(R.id.ic_vector_buck)
        tracksRecyclerView = findViewById(R.id.tracksRecyclerView)

        stateErrorConnection = findViewById(R.id.state_error_connection)
        stateNothingFound = findViewById(R.id.state_nothing_found)
        buttonConnection = findViewById(R.id.button_connection)

        searchProgressBar = findViewById(R.id.progressBar)
        historyContainer = findViewById(R.id.historyContainer)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
    }

    private fun setupListeners() {
        // Кнопка назад
        backButton.setOnClickListener {
            navigateToMainActivity()
        }

        // Очистка истории
        clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
            Toast.makeText(this, "История очищена", Toast.LENGTH_SHORT).show()
        }

        // Повтор запроса при ошибке
        buttonConnection.setOnClickListener {
            val text = editText.text.toString().trim()
            if (text.isNotBlank()) {
                viewModel.search(text)
            } else {
                Toast.makeText(this, "Нет запроса для повтора", Toast.LENGTH_SHORT).show()
            }
        }

        // Очистка текста и возврат к истории
        clearButton.setOnClickListener {
            editText.text.clear()
            hideKeyboard(editText)
            adapter.submitList(emptyList())
            viewModel.getInitialHistory()
        }

        // Debounce на изменение текста (Presentation-логика)
        editText.doOnTextChanged { text, _, _, _ ->
            clearButton.visibility = if (text.isNullOrBlank()) View.GONE else View.VISIBLE
            val query = text.toString().trim()

            // Отменяем предыдущий отложенный вызов
            searchDebounce?.let { handler.removeCallbacks(it) }

            if (query.isNotBlank()) {
                searchDebounce = Runnable {
                    viewModel.search(query)
                }
                handler.postDelayed(searchDebounce!!, debounceDelay)
            } else {
                // Пустой запрос — показываем историю
                searchDebounce = null
                viewModel.getInitialHistory()
            }
        }

        // Обработка Enter в поле ввода
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchDebounce?.let { handler.removeCallbacks(it) }
                val query = editText.text.toString().trim()
                if (query.isNotBlank()) {
                    viewModel.search(query)
                    hideKeyboard(editText)
                } else {
                    viewModel.getInitialHistory()
                }
                return@setOnEditorActionListener true
            }
            false
        }

        // Фокус на поле ввода — показываем историю
        // Потеря фокуса — принудительно скрываем историю (чтобы не мешала)
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewModel.getInitialHistory()
            } else {
                historyContainer.visibility = View.GONE
            }
        }

        // Обработчики кликов по трекам (в результатах и в истории)
        val onItemClick = object : OnItemClickListener {
            override fun onItemClick(track: TrackItem) {
                viewModel.addToHistory(track)
                openPlayerActivity(track)
            }
        }
        adapter.setOnItemClickListener(onItemClick)
        historyAdapter.setOnItemClickListener(onItemClick)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun openPlayerActivity(track: TrackItem) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(Constants.EXTRA_TRACK, track)
        }
        startActivity(intent)
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}

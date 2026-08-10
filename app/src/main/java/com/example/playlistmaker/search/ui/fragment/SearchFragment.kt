package com.example.playlistmaker.search.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.search.domain.model.TrackItem
import com.example.playlistmaker.search.ui.adapter.OnItemClickListener
import com.example.playlistmaker.search.ui.adapter.TrackAdapter
import com.example.playlistmaker.search.ui.viewmodel.SearchUiState
import com.example.playlistmaker.search.ui.viewmodel.SearchViewModel
import com.example.playlistmaker.utils.Constants
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModel()

    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    private var searchDebounce: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())
    private val debounceDelay = Constants.SEARCH_DEBOUNCE_DELAY

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TrackAdapter(emptyList())
        historyAdapter = TrackAdapter(emptyList())

        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksRecyclerView.adapter = adapter

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = historyAdapter

        // Изначально скрываем историю
        binding.historyContainer.visibility = View.GONE

        setupListeners()
        observeState()
    }

    private fun observeState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchUiState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                }

                is SearchUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tracksRecyclerView.visibility = View.GONE
                    binding.stateErrorConnection.visibility = View.GONE
                    binding.stateNothingFound.visibility = View.GONE
                }

                is SearchUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    adapter.submitList(state.tracks)

                    if (state.tracks.isEmpty()) {
                        // Нет результатов
                        binding.tracksRecyclerView.visibility = View.GONE
                        binding.stateNothingFound.visibility = View.VISIBLE
                        binding.stateErrorConnection.visibility = View.GONE

                        removeFocusAndHideHistory()
                    } else {
                        // Есть результаты
                        binding.tracksRecyclerView.visibility = View.VISIBLE
                        binding.stateNothingFound.visibility = View.GONE
                        binding.stateErrorConnection.visibility = View.GONE

                        addFirstTrackToHistoryAutomatically(state.tracks.first())
                    }
                }

                is SearchUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tracksRecyclerView.visibility = View.GONE
                    binding.stateErrorConnection.visibility = View.VISIBLE
                    binding.stateNothingFound.visibility = View.GONE

                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    removeFocusAndHideHistory()
                }

                is SearchUiState.History -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tracksRecyclerView.visibility = View.GONE
                    binding.stateErrorConnection.visibility = View.GONE
                    binding.stateNothingFound.visibility = View.GONE

                    historyAdapter.submitList(state.history)
                }
            }
        }
    }

    // Автоматически добавляем первый трек из списка в историю
    private fun addFirstTrackToHistoryAutomatically(track: TrackItem) {
        // Проверяем, что история ещё не содержит этот трек (опционально)
        viewModel.addToHistory(track)

    }

    private fun removeFocusAndHideHistory() {
        binding.inputEditText.clearFocus()
        binding.historyContainer.visibility = View.GONE
    }

    private fun setupListeners() {
        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
            Toast.makeText(requireContext(), "История очищена", Toast.LENGTH_SHORT).show()

            if (binding.inputEditText.hasFocus() && binding.inputEditText.text.isBlank()) {
                binding.historyContainer.visibility = View.VISIBLE
            }
        }

        binding.buttonConnection.setOnClickListener {
            val text = binding.inputEditText.text.toString().trim()
            if (text.isNotBlank()) {
                viewModel.search(text)
            } else {
                Toast.makeText(requireContext(), "Нет запроса для повтора", Toast.LENGTH_SHORT).show()
            }
        }

        binding.imageButtonSearchClear.setOnClickListener {
            binding.inputEditText.text.clear()
            hideKeyboard(binding.inputEditText)
            adapter.submitList(emptyList())
            viewModel.getInitialHistory()

            if (binding.inputEditText.hasFocus()) {
                binding.historyContainer.visibility = View.VISIBLE
            } else {
                binding.historyContainer.visibility = View.GONE
            }
        }

        binding.inputEditText.doOnTextChanged { text, _, _, _ ->
            binding.imageButtonSearchClear.visibility =
                if (text.isNullOrBlank()) View.GONE else View.VISIBLE

            val query = text.toString().trim()

            searchDebounce?.let { handler.removeCallbacks(it) }

            if (query.isNotBlank()) {
                searchDebounce = Runnable {
                    viewModel.search(query)
                }
                handler.postDelayed(searchDebounce!!, debounceDelay)

                // ЕСТЬ ТЕКСТ → СКРЫВАЕМ ИСТОРИЮ
                binding.historyContainer.visibility = View.GONE
            } else {
                searchDebounce = null
                viewModel.getInitialHistory()

                if (binding.inputEditText.hasFocus()) {
                    binding.historyContainer.visibility = View.VISIBLE
                } else {
                    binding.historyContainer.visibility = View.GONE
                }
            }
        }

        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchDebounce?.let { handler.removeCallbacks(it) }
                val query = binding.inputEditText.text.toString().trim()
                if (query.isNotBlank()) {
                    viewModel.search(query)
                    hideKeyboard(binding.inputEditText)
                    binding.historyContainer.visibility = View.GONE
                } else {
                    viewModel.getInitialHistory()
                    binding.historyContainer.visibility = View.GONE
                }
                return@setOnEditorActionListener true
            }
            false
        }

        binding.inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (binding.inputEditText.text.isBlank()) {
                    viewModel.getInitialHistory()
                    binding.historyContainer.visibility = View.VISIBLE
                }
            } else {
                binding.historyContainer.visibility = View.GONE
            }
        }

        val onItemClick = object : OnItemClickListener {
            override fun onItemClick(track: TrackItem) {
                // При клике на трек — сразу добавляем в историю (он окажется сверху)
                viewModel.addToHistory(track)
                openPlayerFragment(track)
            }
        }
        adapter.setOnItemClickListener(onItemClick)
        historyAdapter.setOnItemClickListener(onItemClick)
    }

    private fun openPlayerFragment(track: TrackItem) {
        val bundle = Bundle().apply {
            putParcelable("track", track)
        }
        findNavController().navigate(R.id.action_searchFragment_to_playerFragment, bundle)
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        _binding = null
        super.onDestroyView()
    }
}



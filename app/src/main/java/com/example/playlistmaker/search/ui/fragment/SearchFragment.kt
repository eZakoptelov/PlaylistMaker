package com.example.playlistmaker.search.ui.fragment

import android.os.Bundle
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
import com.example.playlistmaker.utils.debounce
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.lifecycle.lifecycleScope

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModel()

    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private var isSearchFieldFocused = false
    private lateinit var searchDebounce: (String) -> Unit
    private lateinit var clickDebounce: (TrackItem) -> Unit

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

        //  debounce для поиска
        searchDebounce = debounce(
            delayMillis = Constants.SEARCH_DEBOUNCE_DELAY,
            coroutineScope = viewLifecycleOwner.lifecycleScope,
            useLastParam = true,
        ) { query ->
            viewModel.search(query)
        }

        //  debounce для кликов по треку
        clickDebounce = debounce(
            delayMillis = Constants.CLICK_DEBOUNCE_DELAY,
            coroutineScope = viewLifecycleOwner.lifecycleScope,
            useLastParam = false,
        ) { track ->
            viewModel.addToHistory(track)
            openPlayerFragment(track)
        }

        adapter = TrackAdapter(emptyList())
        historyAdapter = TrackAdapter(emptyList())

        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksRecyclerView.adapter = adapter

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = historyAdapter
        binding.historyContainer.visibility = View.GONE

        setupListeners()
        observeState()
    }

    override fun onResume() {
        super.onResume()
        binding.historyContainer.visibility = View.GONE
    }

    private fun observeState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchUiState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.historyContainer.visibility = View.GONE
                }

                is SearchUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tracksRecyclerView.visibility = View.GONE
                    binding.stateErrorConnection.visibility = View.GONE
                    binding.stateNothingFound.visibility = View.GONE
                    binding.historyContainer.visibility = View.GONE
                }

                is SearchUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    adapter.submitList(state.tracks)
                    binding.historyContainer.visibility = View.GONE

                    if (state.tracks.isEmpty()) {
                        binding.tracksRecyclerView.visibility = View.GONE
                        binding.stateNothingFound.visibility = View.VISIBLE
                        binding.stateErrorConnection.visibility = View.GONE
                        removeFocusAndHideHistory()
                    } else {
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
                    binding.historyContainer.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    removeFocusAndHideHistory()
                }

                is SearchUiState.History -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tracksRecyclerView.visibility = View.GONE
                    binding.stateErrorConnection.visibility = View.GONE
                    binding.stateNothingFound.visibility = View.GONE

                    historyAdapter.submitList(state.history)

                    val shouldShowHistory = state.history.isNotEmpty() && isSearchFieldFocused
                    binding.historyContainer.visibility =
                        if (shouldShowHistory) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun addFirstTrackToHistoryAutomatically(track: TrackItem) {
        viewModel.addToHistory(track)
    }

    private fun removeFocusAndHideHistory() {
        binding.inputEditText.clearFocus()
    }

    private fun setupListeners() {
        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
            Toast.makeText(requireContext(), "История очищена", Toast.LENGTH_SHORT).show()
        }

        binding.buttonConnection.setOnClickListener {
            val text = binding.inputEditText.text.toString().trim()
            if (text.isNotBlank()) {
                viewModel.search(text)
            } else {
                Toast.makeText(requireContext(), "Нет запроса для повтора", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.imageButtonSearchClear.setOnClickListener {
            binding.inputEditText.text.clear()
            hideKeyboard(binding.inputEditText)
            adapter.submitList(emptyList())
            viewModel.getInitialHistory()
        }

        binding.inputEditText.doOnTextChanged { text, _, _, _ ->
            binding.imageButtonSearchClear.visibility =
                if (text.isNullOrBlank()) View.GONE else View.VISIBLE

            val query = text.toString().trim()

            if (query.isNotBlank()) {
                searchDebounce(query)
            } else {
                viewModel.getInitialHistory()
            }
        }

        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = binding.inputEditText.text.toString().trim()
                if (query.isNotBlank()) {
                    viewModel.search(query)
                    hideKeyboard(binding.inputEditText)
                } else {
                    viewModel.getInitialHistory()
                }
                return@setOnEditorActionListener true
            }
            false
        }

        binding.inputEditText.setOnFocusChangeListener { _, hasFocus ->
            isSearchFieldFocused = hasFocus
            if (hasFocus && binding.inputEditText.text.isBlank()) {
                viewModel.getInitialHistory()
            }
        }

        val onItemClick = object : OnItemClickListener {
            override fun onItemClick(track: TrackItem) {
                clickDebounce(track)
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
        _binding = null
        super.onDestroyView()
    }
}

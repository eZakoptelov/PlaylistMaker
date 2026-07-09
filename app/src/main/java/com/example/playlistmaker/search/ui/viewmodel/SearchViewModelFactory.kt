package com.example.playlistmaker.search.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.search.domain.usecase.AddToHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.ClearHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.GetSearchHistoryUseCase
import com.example.playlistmaker.search.domain.usecase.SearchUseCase

class SearchViewModelFactory(
    private val searchUseCase: SearchUseCase,
    private val getHistoryUseCase: GetSearchHistoryUseCase,
    private val addToHistoryUseCase: AddToHistoryUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(searchUseCase, getHistoryUseCase, addToHistoryUseCase, clearHistoryUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

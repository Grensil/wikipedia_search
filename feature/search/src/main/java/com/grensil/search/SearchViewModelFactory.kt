package com.grensil.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grensil.domain.usecase.GetMediaListUseCase
import com.grensil.domain.usecase.GetSummaryUseCase

class SearchViewModelFactory(
    private val getSummaryUseCase: GetSummaryUseCase,
    private val getMediaListUseCase: GetMediaListUseCase,
    private val initialSearchQuery: String? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(getSummaryUseCase, getMediaListUseCase, initialSearchQuery) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
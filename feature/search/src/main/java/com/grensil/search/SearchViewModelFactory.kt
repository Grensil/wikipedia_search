package com.grensil.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grensil.domain.usecase.GetMediaListUseCase
import com.grensil.domain.usecase.GetSummaryUseCase
import com.grensil.domain.usecase.SearchKeywordExtractorUseCase

class SearchViewModelFactory(
    private val getSummaryUseCase: GetSummaryUseCase,
    private val getMediaListUseCase: GetMediaListUseCase,
    private val searchKeywordExtractorUseCase: SearchKeywordExtractorUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(getSummaryUseCase, getMediaListUseCase,searchKeywordExtractorUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
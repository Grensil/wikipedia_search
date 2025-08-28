package com.grensil.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grensil.domain.usecase.GetDetailPageUrlUseCase
import com.grensil.domain.usecase.GetMediaListUseCase
import com.grensil.domain.usecase.GetSummaryUseCase
import com.grensil.domain.usecase.SearchKeywordExtractorUseCase


class DetailViewModelFactory(
    private val getDetailPageUrlUseCase: GetDetailPageUrlUseCase,
    private val searchKeywordExtractorUseCase: SearchKeywordExtractorUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel(getDetailPageUrlUseCase, searchKeywordExtractorUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.grensil.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.grensil.domain.usecase.GetDetailPageUrlUseCase


class DetailViewModelFactory(
    private val getDetailPageUrlUseCase: GetDetailPageUrlUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel(getDetailPageUrlUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.grensil.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.domain.usecase.GetMediaListUseCase
import com.grensil.domain.usecase.GetSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val getSummaryUseCase: GetSummaryUseCase,
    private val getMediaListUseCase: GetMediaListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState

    fun search(keyword: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val summary = getSummaryUseCase(keyword)
                val mediaList = getMediaListUseCase(keyword)
                _uiState.value = SearchUiState.Success(summary, mediaList)
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val summary: Summary, val mediaList: List<MediaItem>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}
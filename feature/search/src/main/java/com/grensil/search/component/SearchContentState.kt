package com.grensil.search.component

import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary

/**
 * Search 화면의 콘텐츠 상태를 나타내는 sealed interface
 * UI 상태 관리를 명확하게 하기 위해 분리
 */
sealed interface SearchContentState {
    object Idle : SearchContentState
    object Loading : SearchContentState
    data class Success(
        val summary: Summary, 
        val mediaList: List<MediaItem>
    ) : SearchContentState
    data class Error(val message: String) : SearchContentState
}

/**
 * SearchUiState를 SearchContentState로 변환하는 확장 함수
 */
fun com.grensil.search.SearchUiState.toContentState(): SearchContentState {
    return when (this) {
        is com.grensil.search.SearchUiState.Idle -> SearchContentState.Idle
        is com.grensil.search.SearchUiState.Loading -> SearchContentState.Loading
        is com.grensil.search.SearchUiState.Success -> SearchContentState.Success(summary, mediaList)
        is com.grensil.search.SearchUiState.Error -> SearchContentState.Error(message)
    }
}
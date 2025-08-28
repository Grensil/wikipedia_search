package com.grensil.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.domain.usecase.GetMediaListUseCase
import com.grensil.domain.usecase.GetSummaryUseCase
import com.grensil.domain.usecase.SearchKeywordExtractorUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class SearchViewModel(
    private val getSummaryUseCase: GetSummaryUseCase,
    private val getMediaListUseCase: GetMediaListUseCase,
    private val searchKeywordExtractorUseCase: SearchKeywordExtractorUseCase
) : ViewModel() {

    private var searchJob: Job? = null

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchedData = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchedData: StateFlow<SearchUiState> = _searchedData

    private val _extractedKeyword = MutableStateFlow<ExtractorUiState>(ExtractorUiState.Idle)
    val extractedKeyword: StateFlow<ExtractorUiState> = _extractedKeyword

    fun search(keyword: String) {
        updateSearchQuery(keyword)
        
        // 이전 검색 작업 취소
        searchJob?.cancel()
        
        // Handle blank or whitespace-only queries
        if (keyword.isBlank()) {
            _searchedData.value = SearchUiState.Idle
            return
        }
        
        searchJob = viewModelScope.launch {
            // 300ms 디바운싱 - 사용자가 빠르게 타이핑할 때 불필요한 요청 방지
            delay(300)
            
            _searchedData.value = SearchUiState.Loading
            try {
                Log.d("SearchViewModel", "Starting search for: $keyword")
                val summary = getSummaryUseCase(keyword)
                Log.d("SearchViewModel", "Summary received: ${summary.title}")
                val mediaList = getMediaListUseCase(keyword)
                Log.d("SearchViewModel", "MediaList received: ${mediaList.size} items")
                _searchedData.value = SearchUiState.Success(summary, mediaList)
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search error for '$keyword': ${e.message}", e)
                
                val errorMessage = when {
                    e.message?.contains("404") == true || e.message?.contains("Not Found") == true -> 
                        "'$keyword'에 대한 검색 결과가 없습니다. 다른 검색어를 시도해보세요."
                    e.message?.contains("timeout") == true || e.message?.contains("시간") == true ->
                        "연결 시간이 초과되었습니다. 다시 시도해주세요."
                    e.message?.contains("connection") == true || e.message?.contains("연결") == true ->
                        "인터넷 연결을 확인해주세요"
                    e is IllegalArgumentException -> 
                        e.message ?: "잘못된 검색어입니다"
                    else -> 
                        e.message ?: "알 수 없는 오류가 발생했습니다"
                }
                
                _searchedData.value = SearchUiState.Error(errorMessage)
            }
        }
    }

    fun updateSearchQuery(keyword: String) {
        _searchQuery.value = keyword
    }

    fun getExtractorKeyword(caption: String) = viewModelScope.launch {
        _extractedKeyword.value = ExtractorUiState.Loading
        try {

            val extractedKeyword = searchKeywordExtractorUseCase(caption)
            _extractedKeyword.value = ExtractorUiState.Success(extractedKeyword)
            search(extractedKeyword.joinToString(separator = " "))
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("404") == true || e.message?.contains("Not Found") == true ->
                    "'$caption'에 대한 결과가 없습니다.."
                e.message?.contains("timeout") == true || e.message?.contains("시간") == true ->
                    "연결 시간이 초과되었습니다. 다시 시도해주세요."
                e.message?.contains("connection") == true || e.message?.contains("연결") == true ->
                    "인터넷 연결을 확인해주세요"
                e is IllegalArgumentException ->
                    e.message ?: "잘못된 링크입니다"
                else ->
                    e.message ?: "알 수 없는 오류가 발생했습니다"
            }

            _extractedKeyword.value = ExtractorUiState.Error(errorMessage)


        }
    }
}

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val summary: Summary, val mediaList: List<MediaItem>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

/** 키워드 추출 상태 */
sealed interface ExtractorUiState {
    object Idle : ExtractorUiState
    object Loading : ExtractorUiState
    data class Success(val keywords: List<String>) : ExtractorUiState
    data class Error(val message: String) : ExtractorUiState
}
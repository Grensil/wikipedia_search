package com.grensil.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.domain.usecase.GetMediaListUseCase
import com.grensil.domain.usecase.GetSummaryUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val getSummaryUseCase: GetSummaryUseCase,
    private val getMediaListUseCase: GetMediaListUseCase
) : ViewModel() {

    private var searchJob: Job? = null // 키워드 추출 및 즉시 검색용

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    init {
        // 디바운싱된 검색 처리
        searchQuery
            .debounce(300) // 300ms 디바운싱
            .distinctUntilChanged() // 동일한 값 연속 입력 방지
            .onEach { keyword ->
                performSearch(keyword)
            }
            .launchIn(viewModelScope)
    }

    // 통합된 상태 관리
    private val _searchedData = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchedData: StateFlow<SearchUiState> = _searchedData.asStateFlow()

    // Pull to Refresh 상태 관리
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _extractedKeyword = MutableStateFlow<ExtractorUiState>(ExtractorUiState.Idle)
    val extractedKeyword: StateFlow<ExtractorUiState> = _extractedKeyword

    fun search(keyword: String) {
        updateSearchQuery(keyword)
    }
    
    private fun performSearch(keyword: String) {
        // Handle blank or whitespace-only queries
        if (keyword.isBlank()) {
            _searchedData.value = SearchUiState.Idle
            return
        }
        
        // Flow가 자동으로 취소 처리하므로 searchJob 불필요
        viewModelScope.launch {
            _searchedData.value = SearchUiState.Loading
            
            try {
                // Summary 먼저 로드
                Log.d("SearchViewModel", "Starting summary search for: $keyword")
                val summary = getSummaryUseCase(keyword)
                Log.d("SearchViewModel", "Summary received: ${summary.title}")
                
                // Summary만 있는 상태로 업데이트
                _searchedData.value = SearchUiState.PartialSuccess(
                    summary = summary,
                    mediaList = emptyList()
                )
                
                // MediaList 로드
                Log.d("SearchViewModel", "Starting media list search for: $keyword")
                val mediaList = getMediaListUseCase(keyword)
                Log.d("SearchViewModel", "MediaList received: ${mediaList.size} items")
                
                // 둘 다 완료된 상태로 업데이트
                _searchedData.value = SearchUiState.Success(
                    summary = summary,
                    mediaList = mediaList
                )
                
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Search error for '$keyword': ${e.message}", e)
                val errorMessage = getErrorMessage(e, keyword)
                _searchedData.value = SearchUiState.Error(errorMessage)
            }
        }
    }


    private fun getErrorMessage(e: Exception, keyword: String): String {
        return when {
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
    }

    fun updateSearchQuery(keyword: String) {
        _searchQuery.value = keyword
    }
    
    fun refreshSearch(keyword: String) {
        searchJob?.cancel()
        
        _isRefreshing.value = true
        
        searchJob = viewModelScope.launch {
            try {
                // Summary 먼저 로드
                val summary = getSummaryUseCase(keyword)
                
                // Summary만 있는 상태로 업데이트
                _searchedData.value = SearchUiState.PartialSuccess(
                    summary = summary,
                    mediaList = emptyList()
                )
                
                // MediaList 로드
                val mediaList = getMediaListUseCase(keyword)
                
                // 둘 다 완료된 상태로 업데이트
                _searchedData.value = SearchUiState.Success(
                    summary = summary,
                    mediaList = mediaList
                )
                
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Refresh search error for '$keyword': ${e.message}", e)
                val errorMessage = getErrorMessage(e, keyword)
                _searchedData.value = SearchUiState.Error(errorMessage)
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class PartialSuccess(val summary: Summary, val mediaList: List<MediaItem>) : SearchUiState
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
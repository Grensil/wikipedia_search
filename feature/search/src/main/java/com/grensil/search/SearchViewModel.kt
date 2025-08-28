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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted

class SearchViewModel(
    private val getSummaryUseCase: GetSummaryUseCase,
    private val getMediaListUseCase: GetMediaListUseCase,
    private val searchKeywordExtractorUseCase: SearchKeywordExtractorUseCase
) : ViewModel() {

    private var searchJob: Job? = null

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // 개별 상태 관리
    private val _summaryState = MutableStateFlow<SummaryUiState>(SummaryUiState.Idle)
    private val _mediaListState = MutableStateFlow<MediaListUiState>(MediaListUiState.Idle)

    // combine으로 전체 상태 조합
    val searchedData: StateFlow<SearchUiState> = combine(
        _summaryState, 
        _mediaListState
    ) { summaryState, mediaListState ->
        when {
            // 둘 다 성공
            summaryState is SummaryUiState.Success && mediaListState is MediaListUiState.Success -> {
                SearchUiState.Success(summaryState.data, mediaListState.data)
            }
            // 둘 다 에러
            summaryState is SummaryUiState.Error && mediaListState is MediaListUiState.Error -> {
                SearchUiState.Error("검색 결과를 불러올 수 없습니다")
            }
            // 일부 성공, 일부 에러
            (summaryState is SummaryUiState.Success || summaryState is SummaryUiState.Error) &&
            (mediaListState is MediaListUiState.Success || mediaListState is MediaListUiState.Error) -> {
                SearchUiState.PartialSuccess(
                    summary = if (summaryState is SummaryUiState.Success) summaryState.data else null,
                    mediaList = if (mediaListState is MediaListUiState.Success) mediaListState.data else null,
                    summaryError = if (summaryState is SummaryUiState.Error) summaryState.message else null,
                    mediaListError = if (mediaListState is MediaListUiState.Error) mediaListState.message else null
                )
            }
            // 하나라도 로딩 중
            summaryState is SummaryUiState.Loading || mediaListState is MediaListUiState.Loading -> {
                SearchUiState.Loading
            }
            // 둘 다 Idle
            else -> SearchUiState.Idle
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState.Idle
    )

    private val _extractedKeyword = MutableStateFlow<ExtractorUiState>(ExtractorUiState.Idle)
    val extractedKeyword: StateFlow<ExtractorUiState> = _extractedKeyword

    fun search(keyword: String) {
        updateSearchQuery(keyword)
        
        // 이전 검색 작업 취소
        searchJob?.cancel()
        
        // Handle blank or whitespace-only queries
        if (keyword.isBlank()) {
            _summaryState.value = SummaryUiState.Idle
            _mediaListState.value = MediaListUiState.Idle
            return
        }
        
        searchJob = viewModelScope.launch {
            // 300ms 디바운싱 - 사용자가 빠르게 타이핑할 때 불필요한 요청 방지
            delay(300)
            
            // 병렬로 API 호출 시작
            launch { loadSummary(keyword) }
            launch { loadMediaList(keyword) }
        }
    }

    private suspend fun loadSummary(keyword: String) {
        _summaryState.value = SummaryUiState.Loading
        try {
            Log.d("SearchViewModel", "Starting summary search for: $keyword")
            val summary = getSummaryUseCase(keyword)
            Log.d("SearchViewModel", "Summary received: ${summary.title}")
            _summaryState.value = SummaryUiState.Success(summary)
        } catch (e: Exception) {
            Log.e("SearchViewModel", "Summary error for '$keyword': ${e.message}", e)
            val errorMessage = getErrorMessage(e, keyword)
            _summaryState.value = SummaryUiState.Error(errorMessage)
        }
    }

    private suspend fun loadMediaList(keyword: String) {
        _mediaListState.value = MediaListUiState.Loading
        try {
            Log.d("SearchViewModel", "Starting media list search for: $keyword")
            val mediaList = getMediaListUseCase(keyword)
            Log.d("SearchViewModel", "MediaList received: ${mediaList.size} items")
            _mediaListState.value = MediaListUiState.Success(mediaList)
        } catch (e: Exception) {
            Log.e("SearchViewModel", "MediaList error for '$keyword': ${e.message}", e)
            val errorMessage = getErrorMessage(e, keyword)
            _mediaListState.value = MediaListUiState.Error(errorMessage)
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

    fun getExtractorKeyword(caption: String) = viewModelScope.launch {
        // 이전 검색 작업 먼저 취소
        searchJob?.cancel()
        
        _extractedKeyword.value = ExtractorUiState.Loading
        try {
            val extractedKeyword = searchKeywordExtractorUseCase(caption)
            _extractedKeyword.value = ExtractorUiState.Success(extractedKeyword)
            
            // 추출된 키워드로 새로운 검색 시작 - 기존 상태 초기화
            val newSearchQuery = extractedKeyword.joinToString(separator = " ")
            updateSearchQuery(newSearchQuery)
            
            // 상태 초기화 후 검색 시작
            _summaryState.value = SummaryUiState.Idle
            _mediaListState.value = MediaListUiState.Idle
            
            // 디바운싱 없이 즉시 검색 (사용자 액션이므로)
            searchJob = viewModelScope.launch {
                launch { loadSummary(newSearchQuery) }
                launch { loadMediaList(newSearchQuery) }
            }
            
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
    data class PartialSuccess(
        val summary: Summary? = null,
        val mediaList: List<MediaItem>? = null,
        val summaryError: String? = null,
        val mediaListError: String? = null
    ) : SearchUiState
}

/** Summary 개별 상태 */
sealed interface SummaryUiState {
    object Idle : SummaryUiState
    object Loading : SummaryUiState
    data class Success(val data: Summary) : SummaryUiState
    data class Error(val message: String) : SummaryUiState
}

/** MediaList 개별 상태 */
sealed interface MediaListUiState {
    object Idle : MediaListUiState
    object Loading : MediaListUiState
    data class Success(val data: List<MediaItem>) : MediaListUiState
    data class Error(val message: String) : MediaListUiState
}

/** 키워드 추출 상태 */
sealed interface ExtractorUiState {
    object Idle : ExtractorUiState
    object Loading : ExtractorUiState
    data class Success(val keywords: List<String>) : ExtractorUiState
    data class Error(val message: String) : ExtractorUiState
}
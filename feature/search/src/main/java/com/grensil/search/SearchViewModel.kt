package com.grensil.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.domain.usecase.GetMediaListUseCase
import com.grensil.domain.usecase.GetSummaryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val getSummaryUseCase: GetSummaryUseCase,
    private val getMediaListUseCase: GetMediaListUseCase,
    private val initialSearchQuery: String? = null
) : ViewModel() {

    private val _searchQuery = MutableStateFlow(initialSearchQuery ?: "")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchedData = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchedData: StateFlow<SearchUiState> = _searchedData.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _scrollToTopEvent = MutableSharedFlow<Unit>()
    val scrollToTopEvent = _scrollToTopEvent.asSharedFlow()

    init {
        // 검색어 변경 시 자동 검색 & 이전 검색 취소
        _searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { keyword ->
                emitScrollToTop(keyword)
                performSearchFlow(keyword)
            }
            .onEach { result ->
                _searchedData.value = result
            }
            .launchIn(viewModelScope)
    }

    private fun emitScrollToTop(keyword: String): Flow<Unit> = flow {
        emit(_scrollToTopEvent.emit(Unit))
    }

    fun search(keyword: String) {
        _searchQuery.value = keyword
    }

    fun refreshSearch(keyword: String) {
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                val summary = getSummaryUseCase(keyword)
                val mediaList = getMediaListUseCase(keyword)
                _searchedData.value = SearchUiState.Success(summary, mediaList)
            } catch (e: Exception) {
                _searchedData.value = SearchUiState.Error(getErrorMessage(e, keyword))
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun performSearchFlow(keyword: String): Flow<SearchUiState> = flow {
        if (keyword.isBlank()) {
            emit(SearchUiState.Idle)
            return@flow
        }

        emit(SearchUiState.Loading)

        val summary = getSummaryUseCase(keyword)
        val mediaList = getMediaListUseCase(keyword)

        emit(SearchUiState.Success(summary, mediaList))

    }.catch { throwable ->
        val e = throwable as? Exception ?: Exception(throwable)
        emit(SearchUiState.Error(getErrorMessage(e, keyword)))
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
}

// UI 상태 정의
sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val summary: Summary, val mediaList: List<MediaItem>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

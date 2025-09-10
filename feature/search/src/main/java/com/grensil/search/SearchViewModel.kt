package com.grensil.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.domain.error.DomainError
import com.grensil.domain.error.NetworkErrorType
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
                performSearchFlow(keyword)
            }
            .onEach { result ->
                _searchedData.value = result
                // 검색 결과가 성공적으로 로드된 후 스크롤
                if (result is SearchUiState.Success) {
                    _scrollToTopEvent.emit(Unit)
                }
            }
            .launchIn(viewModelScope)
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
                val domainError = if (e is DomainError) e else DomainError.UnknownError(e)
                _searchedData.value = SearchUiState.Error(getErrorMessage(domainError, keyword))
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
        val domainError = if (e is DomainError) e else DomainError.UnknownError(e)
        emit(SearchUiState.Error(getErrorMessage(domainError, keyword)))
    }

    private fun getErrorMessage(error: DomainError, keyword: String): String {
        return when (error) {
            is DomainError.NetworkError -> when (error.type) {
                NetworkErrorType.NOT_FOUND -> 
                    "'$keyword'에 대한 검색 결과가 없습니다. 다른 검색어를 시도해보세요."
                NetworkErrorType.TIMEOUT -> 
                    "연결 시간이 초과되었습니다. 다시 시도해주세요."
                NetworkErrorType.CONNECTION_FAILED -> 
                    "인터넷 연결을 확인해주세요"
                NetworkErrorType.SERVER_ERROR -> 
                    "서버에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요."
                NetworkErrorType.SSL_ERROR -> 
                    "보안 연결에 문제가 발생했습니다. 다시 시도해주세요."
                NetworkErrorType.PARSE_ERROR -> 
                    "데이터 처리 중 오류가 발생했습니다."
                NetworkErrorType.INVALID_REQUEST, NetworkErrorType.INVALID_URL -> 
                    "잘못된 요청입니다. 검색어를 확인해주세요."
            }
            is DomainError.ValidationError -> 
                error.reason
            is DomainError.UnknownError -> 
                "알 수 없는 오류가 발생했습니다"
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

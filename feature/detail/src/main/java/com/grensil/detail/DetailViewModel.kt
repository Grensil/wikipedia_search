package com.grensil.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grensil.domain.usecase.GetDetailPageUrlUseCase
import com.grensil.domain.usecase.SearchKeywordExtractorUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val getDetailPageUrlUseCase: GetDetailPageUrlUseCase,
    private val searchKeywordExtractorUseCase: SearchKeywordExtractorUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Idle)
    val uiState: StateFlow<DetailUiState> = _uiState

    fun getDetailPageUrl(keyword: String) = viewModelScope.launch {

        // Handle blank or whitespace-only queries
        if (keyword.isBlank()) {
            _uiState.value = DetailUiState.Idle
            return@launch
        }
        _uiState.value = DetailUiState.Loading
        try {
            val url = getDetailPageUrlUseCase(keyword)
            _uiState.value = DetailUiState.Success(url)
        } catch (e: Exception) {
            Log.e("SearchViewModel", "Search error for '$keyword': ${e.message}", e)

            val errorMessage = when {
                e.message?.contains("404") == true || e.message?.contains("Not Found") == true ->
                    "'$keyword'에 대한 결과가 없습니다."

                e.message?.contains("timeout") == true || e.message?.contains("시간") == true ->
                    "연결 시간이 초과되었습니다. 다시 시도해주세요."

                e.message?.contains("connection") == true || e.message?.contains("연결") == true ->
                    "인터넷 연결을 확인해주세요"

                e is IllegalArgumentException ->
                    e.message ?: "잘못된 링크입니다"

                else ->
                    e.message ?: "알 수 없는 오류가 발생했습니다"
            }

            _uiState.value = DetailUiState.Error(errorMessage)
        }

    }

}

sealed interface DetailUiState {
    object Idle : DetailUiState
    object Loading : DetailUiState
    data class Success(val webUrl: String) : DetailUiState
    data class Error(val message: String) : DetailUiState
}
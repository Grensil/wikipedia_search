package com.grensil.nhn_gmail.di

import com.grensil.data.datasource.WikipediaRemoteDataSource
import com.grensil.data.repository.WikipediaRepositoryImpl
import com.grensil.domain.usecase.GetDetailPageUrlUseCase
import com.grensil.domain.usecase.GetMediaListUseCase
import com.grensil.domain.usecase.GetSummaryUseCase
import com.grensil.domain.usecase.SearchKeywordExtractorUseCase
import com.grensil.network.HttpClient

class AppModule(httpClient: HttpClient) {
    private val dataSource = WikipediaRemoteDataSource(httpClient)
    private val repository = WikipediaRepositoryImpl(dataSource)

    // Lazy 초기화로 한 번만 생성
    private val _getSummaryUseCase by lazy { GetSummaryUseCase(repository) }
    private val _getMediaListUseCase by lazy { GetMediaListUseCase(repository) }
    private val _getDetailPageUrlUseCase by lazy { GetDetailPageUrlUseCase(repository) }
    private val _searchKeywordExtractorUseCase by lazy { SearchKeywordExtractorUseCase() }

    fun getSummaryUseCase() = _getSummaryUseCase
    fun getMediaListUseCase() = _getMediaListUseCase
    fun getDetailPageUrlUseCase() = _getDetailPageUrlUseCase
    fun getSearchKeywordExtractorUseCase() = _searchKeywordExtractorUseCase
}
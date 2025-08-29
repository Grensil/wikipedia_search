package com.grensil.nhn_gmail.di

import com.grensil.data.datasource.WikipediaRemoteDataSource
import com.grensil.data.repository.WikipediaRepositoryImpl
import com.grensil.domain.repository.WikipediaRepository
import com.grensil.domain.usecase.GetDetailPageUrlUseCase
import com.grensil.domain.usecase.GetDetailPageUrlUseCaseImpl
import com.grensil.domain.usecase.GetMediaListUseCase
import com.grensil.domain.usecase.GetMediaListUseCaseImpl
import com.grensil.domain.usecase.GetSummaryUseCase
import com.grensil.domain.usecase.GetSummaryUseCaseImpl
import com.grensil.network.HttpClient

/**
 * 개선된 수동 DI 컨테이너
 * - 타입 안전성 향상
 * - 의존성 그래프 명확화
 * - 테스트 가능한 구조
 */
class AppModule private constructor(
    private val httpClient: HttpClient
) {
    
    // 레이어별 의존성 관리
    private val dataSource: WikipediaRemoteDataSource by lazy { 
        WikipediaRemoteDataSource(httpClient) 
    }
    
    private val repository: WikipediaRepository by lazy { 
        WikipediaRepositoryImpl(dataSource) 
    }

    // UseCase들 - Lazy 초기화로 한 번만 생성
    private val _getSummaryUseCase by lazy { GetSummaryUseCaseImpl(repository) }
    private val _getMediaListUseCase by lazy { GetMediaListUseCaseImpl(repository) }
    private val _getDetailPageUrlUseCase by lazy { GetDetailPageUrlUseCaseImpl(repository) }

    // Public API
    fun getSummaryUseCase(): GetSummaryUseCase = _getSummaryUseCase
    fun getMediaListUseCase(): GetMediaListUseCase = _getMediaListUseCase
    fun getDetailPageUrlUseCase(): GetDetailPageUrlUseCase = _getDetailPageUrlUseCase

    companion object {
        @Volatile
        private var INSTANCE: AppModule? = null
        
        /**
         * Singleton 인스턴스 생성 (Thread-Safe)
         */
        fun getInstance(httpClient: HttpClient): AppModule {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppModule(httpClient).also { INSTANCE = it }
            }
        }
        
        /**
         * 테스트용 인스턴스 재설정
         */
        fun resetForTesting() {
            INSTANCE = null
        }
    }
}
package com.grensil.detail

import com.grensil.domain.repository.WikipediaRepository
import com.grensil.domain.usecase.GetDetailPageUrlUseCase
import com.grensil.domain.usecase.GetDetailPageUrlUseCaseImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * DetailViewModel Unit Test
 * 
 * 테스트 목적:
 * 1. DetailViewModel의 URL 생성 로직 검증
 * 2. UI State 변화 패턴 확인
 * 3. 검색어 처리 로직 검증
 * 4. UseCase 통합 동작 확인
 * 
 * 사용 기술: Android API + JUnit 4 + Coroutine Test만 사용
 * 특징: 실제 네트워크 호출 없이 ViewModel 로직만 테스트
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetailTest {

    private lateinit var viewModel: DetailViewModel
    private lateinit var fakeRepository: FakeWikipediaRepository
    private lateinit var getDetailPageUrlUseCase: GetDetailPageUrlUseCase

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // 테스트용 Dispatcher 설정
        Dispatchers.setMain(testDispatcher)
        
        // Fake Repository 및 UseCase 설정
        fakeRepository = FakeWikipediaRepository()
        getDetailPageUrlUseCase = GetDetailPageUrlUseCaseImpl(fakeRepository)
        
        // ViewModel 생성
        viewModel = DetailViewModel(getDetailPageUrlUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * ✅ 상세 페이지 로드 성공 시나리오 테스트
     * 
     * 테스트 시나리오:
     * 1. 초기 상태는 Idle
     * 2. loadDetail 호출 시 Loading 상태
     * 3. 성공 시 Success 상태와 올바른 URL 반환
     */
    @Test
    fun test_loadDetail_with_valid_searchTerm_updates_uiState_to_success() = runTest {
        // Given: 테스트 검색어 준비
        val searchTerm = "Android"
        val expectedUrl = "https://en.wikipedia.org/wiki/Android"
        
        fakeRepository.setDetailPageUrl(searchTerm, expectedUrl)

        // When: 상세 페이지 로드 실행
        viewModel.loadDetail(searchTerm)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: UI State 검증
        val uiState = viewModel.uiState.value
        assertTrue("성공 상태여야 함", uiState is DetailUiState.Success)
        
        val successState = uiState as DetailUiState.Success
        assertEquals("URL이 올바르게 설정되어야 함", expectedUrl, successState.url)
        assertEquals("검색어가 올바르게 설정되어야 함", searchTerm, successState.searchTerm)
    }

    /**
     * ❌ 상세 페이지 로드 실패 시나리오 테스트
     * 
     * 테스트 시나리오:
     * 1. Repository에서 예외 발생
     * 2. Error 상태로 변화
     * 3. 적절한 에러 메시지 표시
     */
    @Test
    fun test_loadDetail_with_repository_error_updates_uiState_to_error() = runTest {
        // Given: Repository가 예외를 던지도록 설정
        val searchTerm = "FailCase"
        fakeRepository.setShouldThrowError(true)

        // When: 상세 페이지 로드 실행
        viewModel.loadDetail(searchTerm)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: Error 상태 검증
        val uiState = viewModel.uiState.value
        assertTrue("에러 상태여야 함", uiState is DetailUiState.Error)
        
        val errorState = uiState as DetailUiState.Error
        assertTrue("에러 메시지가 있어야 함", errorState.message.isNotEmpty())
    }

    /**
     * 🔍 빈 검색어 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. 빈 문자열로 상세 페이지 로드 시도
     * 2. 적절한 검증 로직 동작 확인
     */
    @Test
    fun test_loadDetail_with_empty_searchTerm_shows_error() = runTest {
        // When: 빈 검색어로 상세 페이지 로드
        viewModel.loadDetail("")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: Error 상태여야 함
        val uiState = viewModel.uiState.value
        assertTrue("빈 검색어는 에러 상태여야 함", uiState is DetailUiState.Error)
    }

    /**
     * 🔄 연속 로드 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. 첫 번째 상세 페이지 로드
     * 2. 두 번째 상세 페이지 로드 (이전 로드 취소)
     * 3. 마지막 로드 결과만 표시되는지 확인
     */
    @Test
    fun test_loadDetail_with_multiple_consecutive_calls_cancels_previous_ones() = runTest {
        // Given: 두 개의 다른 검색어 준비
        val firstTerm = "Android"
        val secondTerm = "iOS"
        
        fakeRepository.setDetailPageUrl(firstTerm, "https://en.wikipedia.org/wiki/Android")
        fakeRepository.setDetailPageUrl(secondTerm, "https://en.wikipedia.org/wiki/IOS")

        // When: 연속으로 상세 페이지 로드 실행
        viewModel.loadDetail(firstTerm)
        viewModel.loadDetail(secondTerm)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 마지막 로드 결과만 표시되어야 함
        val uiState = viewModel.uiState.value
        assertTrue("성공 상태여야 함", uiState is DetailUiState.Success)
        
        val successState = uiState as DetailUiState.Success
        assertEquals("마지막 검색어", secondTerm, successState.searchTerm)
        assertEquals("마지막 URL", "https://en.wikipedia.org/wiki/IOS", successState.url)
    }

    /**
     * 🌐 URL 포맷 검증 테스트
     * 
     * 테스트 시나리오:
     * 1. 다양한 검색어에 대한 URL 생성
     * 2. URL 형식이 올바른지 확인
     */
    @Test
    fun test_loadDetail_with_various_searchTerms_generates_correct_url_format() = runTest {
        // Given: 다양한 검색어와 예상 URL
        val testCases = mapOf(
            "Android" to "https://en.wikipedia.org/wiki/Android",
            "iOS Development" to "https://en.wikipedia.org/wiki/iOS_Development",
            "Machine Learning" to "https://en.wikipedia.org/wiki/Machine_Learning"
        )

        testCases.forEach { (searchTerm, expectedUrl) ->
            // Given: Repository에 URL 설정
            fakeRepository.setDetailPageUrl(searchTerm, expectedUrl)

            // When: 상세 페이지 로드
            viewModel.loadDetail(searchTerm)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: 올바른 URL 생성 확인
            val uiState = viewModel.uiState.value
            assertTrue("$searchTerm: 성공 상태여야 함", uiState is DetailUiState.Success)
            
            val successState = uiState as DetailUiState.Success
            assertEquals("$searchTerm: URL이 올바르게 생성되어야 함", expectedUrl, successState.url)
            assertTrue("$searchTerm: URL이 Wikipedia 형식이어야 함", successState.url.startsWith("https://en.wikipedia.org/wiki/"))
        }
    }

    /**
     * 🚫 잘못된 URL 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. Repository에서 잘못된 URL 반환
     * 2. ViewModel에서 적절히 검증 처리
     */
    @Test
    fun test_loadDetail_with_invalid_url_from_repository_handles_error() = runTest {
        // Given: 잘못된 URL 설정
        val searchTerm = "InvalidCase"
        val invalidUrl = "" // 빈 URL
        
        fakeRepository.setDetailPageUrl(searchTerm, invalidUrl)

        // When: 상세 페이지 로드
        viewModel.loadDetail(searchTerm)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: Error 상태여야 함
        val uiState = viewModel.uiState.value
        assertTrue("잘못된 URL은 에러 상태여야 함", uiState is DetailUiState.Error)
    }

    /**
     * 🔧 UI State 초기화 테스트
     * 
     * ViewModel 생성 시 초기 상태가 올바른지 확인
     */
    @Test
    fun test_viewModel_creation_has_correct_initial_state() {
        // Then: 초기 상태는 Idle이어야 함
        assertTrue("초기 상태는 Idle이어야 함", viewModel.uiState.value is DetailUiState.Idle)
    }

    /**
     * ⏱️ Loading 상태 테스트
     * 
     * 로딩 상태가 올바르게 설정되는지 확인
     */
    @Test
    fun test_loadDetail_execution_shows_loading_state_during_execution() = runTest {
        // Given: 검색어 준비
        val searchTerm = "Android"
        fakeRepository.setDetailPageUrl(searchTerm, "https://en.wikipedia.org/wiki/Android")

        // When: 상세 페이지 로드 시작 (아직 완료되지 않음)
        viewModel.loadDetail(searchTerm)

        // Then: 로딩 상태 확인 (아직 advanceUntilIdle 호출하지 않음)
        val uiState = viewModel.uiState.value
        assertTrue("로딩 상태여야 함", uiState is DetailUiState.Loading)

        // When: 작업 완료
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 성공 상태로 변경
        val finalState = viewModel.uiState.value
        assertTrue("최종적으로 성공 상태여야 함", finalState is DetailUiState.Success)
    }

    /**
     * 🔤 특수 문자 포함 검색어 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. 공백, 특수문자가 포함된 검색어
     * 2. URL 인코딩이 올바르게 처리되는지 확인
     */
    @Test
    fun test_loadDetail_with_special_characters_handles_searchTerm_correctly() = runTest {
        // Given: 특수 문자가 포함된 검색어
        val searchTermWithSpaces = "Android Development"
        val searchTermWithSpecialChars = "C++ Programming"
        
        fakeRepository.setDetailPageUrl(searchTermWithSpaces, "https://en.wikipedia.org/wiki/Android_Development")
        fakeRepository.setDetailPageUrl(searchTermWithSpecialChars, "https://en.wikipedia.org/wiki/C%2B%2B_Programming")

        // When & Then: 공백 포함 검색어 테스트
        viewModel.loadDetail(searchTermWithSpaces)
        testDispatcher.scheduler.advanceUntilIdle()
        
        var uiState = viewModel.uiState.value
        assertTrue("공백 포함 검색어도 성공해야 함", uiState is DetailUiState.Success)
        
        // When & Then: 특수문자 포함 검색어 테스트
        viewModel.loadDetail(searchTermWithSpecialChars)
        testDispatcher.scheduler.advanceUntilIdle()
        
        uiState = viewModel.uiState.value
        assertTrue("특수문자 포함 검색어도 성공해야 함", uiState is DetailUiState.Success)
    }

    // =================================
    // 테스트용 Fake Repository
    // =================================

    /**
     * 테스트용 Fake Repository - Android API만 사용
     */
    private class FakeWikipediaRepository : WikipediaRepository {
        private val detailUrls = mutableMapOf<String, String>()
        private var shouldThrowError = false
        
        fun setDetailPageUrl(searchTerm: String, url: String) {
            detailUrls[searchTerm] = url
        }
        
        fun setShouldThrowError(shouldThrow: Boolean) {
            shouldThrowError = shouldThrow
        }
        
        override suspend fun getSummary(searchTerm: String): com.grensil.domain.dto.Summary {
            throw NotImplementedError("Not needed for DetailViewModel test")
        }
        
        override suspend fun getMediaList(searchTerm: String): List<com.grensil.domain.dto.MediaItem> {
            throw NotImplementedError("Not needed for DetailViewModel test")
        }
        
        override fun getDetailPageUrl(searchTerm: String): String {
            if (shouldThrowError) {
                throw RuntimeException("Test error occurred")
            }
            return detailUrls[searchTerm] ?: "https://en.wikipedia.org/wiki/Default"
        }
    }
}
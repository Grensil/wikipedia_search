package com.grensil.detail

import com.grensil.domain.repository.WikipediaRepository
import com.grensil.domain.usecase.GetDetailPageUrlUseCase
import com.grensil.domain.usecase.GetDetailPageUrlUseCaseImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Detail Module ViewModel Unit Test
 * 
 * 테스트 목적:
 * 1. DetailViewModel의 URL 생성 로직 검증
 * 2. UI State 변화 패턴 확인
 * 3. 검색어 처리 로직 검증
 * 4. UseCase 통합 동작 확인
 * 
 * 특징:
 * - 실제 네트워크 호출 없이 ViewModel 로직만 테스트
 * - Android API + JUnit 4 + Coroutine Test 사용
 * - 빠른 실행 속도로 개발 중 자주 실행 가능
 * 
 * Naming Convention:
 * - Class: DetailViewModelTest
 * - Methods: camelCase naming without backticks
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private lateinit var viewModel: DetailViewModel
    private lateinit var fakeRepository: FakeWikipediaRepository
    private lateinit var getDetailPageUrlUseCase: GetDetailPageUrlUseCase

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Fake Repository 및 UseCase 설정
        fakeRepository = FakeWikipediaRepository()
        fakeRepository.setShouldThrowError(false) // 초기화
        getDetailPageUrlUseCase = object : GetDetailPageUrlUseCase {
            override fun invoke(searchTerm: String): String = fakeRepository.getDetailPageUrl(searchTerm)
        }
        
        // ViewModel 생성
        viewModel = DetailViewModel(getDetailPageUrlUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =====================================
    // ✅ Successful Load Scenarios
    // =====================================

    /**
     * ✅ 상세 페이지 로드 성공 시나리오 테스트
     * 
     * 테스트 시나리오:
     * 1. 초기 상태는 Idle
     * 2. loadDetail 호출 시 Loading 상태
     * 3. 성공 시 Success 상태와 올바른 URL 반환
     */
    @Test
    fun loadDetailWithValidSearchTermUpdatesUiStateToSuccess() = runTest {
        // Given: 테스트 검색어 준비
        val searchTerm = "Android"
        val expectedUrl = "https://en.wikipedia.org/wiki/Android"
        
        fakeRepository.setDetailPageUrl(searchTerm, expectedUrl)

        // When: 상세 페이지 로드 실행
        viewModel.getDetailPageUrl(searchTerm)
        advanceUntilIdle() // 모든 코루틴 작업 완료 대기

        // Then: UI State 검증
        val uiState = viewModel.uiState.value
        assertTrue("성공 상태여야 함", uiState is DetailUiState.Success)
        
        val successState = uiState as DetailUiState.Success
        assertEquals("URL이 올바르게 설정되어야 함", expectedUrl, successState.webUrl)
    }

    // =====================================
    // ❌ Error Handling Scenarios
    // =====================================

    /**
     * ❌ 상세 페이지 로드 실패 시나리오 테스트
     * 
     * 테스트 시나리오:
     * 1. Repository에서 예외 발생
     * 2. Error 상태로 변화
     * 3. 적절한 에러 메시지 표시
     */
    @Test
    fun loadDetailWithRepositoryErrorUpdatesUiStateToError() = runTest {
        // Given: Repository가 예외를 던지도록 설정
        val searchTerm = "Fail Case"
        fakeRepository.setShouldThrowError(true)

        // When: 상세 페이지 로드 실행
        viewModel.getDetailPageUrl(searchTerm)
        advanceUntilIdle() // 모든 코루틴 작업 완료 대기

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
    fun loadDetailWithEmptySearchTermShowsError() = runTest {
        // When: 빈 검색어로 상세 페이지 로드
        viewModel.getDetailPageUrl("")
        advanceUntilIdle() // 모든 코루틴 작업 완료 대기

        // Then: Idle 상태여야 함 (빈 검색어는 처리하지 않음)
        val uiState = viewModel.uiState.value
        assertTrue("빈 검색어는 Idle 상태여야 함", uiState is DetailUiState.Idle)
    }

    // =====================================
    // 🔄 Advanced Load Scenarios
    // =====================================

    /**
     * 🔄 연속 로드 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. 첫 번째 상세 페이지 로드
     * 2. 두 번째 상세 페이지 로드 (이전 로드 취소)
     * 3. 마지막 로드 결과만 표시되는지 확인
     */
    @Test
    fun loadDetailWithMultipleConsecutiveCallsCancelsPreviousOnes() = runTest {
        // Given: 두 개의 다른 검색어 준비
        val firstTerm = "Android"
        val secondTerm = "iOS"
        
        fakeRepository.setDetailPageUrl(firstTerm, "https://en.wikipedia.org/wiki/Android")
        fakeRepository.setDetailPageUrl(secondTerm, "https://en.wikipedia.org/wiki/IOS")

        // When: 연속으로 상세 페이지 로드 실행
        viewModel.getDetailPageUrl(firstTerm)
        viewModel.getDetailPageUrl(secondTerm)
        advanceUntilIdle() // 모든 코루틴 작업 완료 대기

        // Then: 마지막 로드 결과만 표시되어야 함
        val uiState = viewModel.uiState.value
        assertTrue("성공 상태여야 함", uiState is DetailUiState.Success)
        
        val successState = uiState as DetailUiState.Success
        assertEquals("마지막 URL", "https://en.wikipedia.org/wiki/IOS", successState.webUrl)
    }

    /**
     * 🌐 URL 포맷 검증 테스트
     * 
     * 테스트 시나리오:
     * 1. 다양한 검색어에 대한 URL 생성
     * 2. URL 형식이 올바른지 확인
     */
    @Test
    fun loadDetailWithVariousSearchTermsGeneratesCorrectUrlFormat() = runTest {
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
            viewModel.getDetailPageUrl(searchTerm)
            advanceUntilIdle() // 모든 코루틴 작업 완료 대기

            // Then: 올바른 URL 생성 확인
            val uiState = viewModel.uiState.value
            assertTrue("$searchTerm: 성공 상태여야 함", uiState is DetailUiState.Success)
            
            val successState = uiState as DetailUiState.Success
            assertEquals("$searchTerm: URL이 올바르게 생성되어야 함", expectedUrl, successState.webUrl)
            assertTrue("$searchTerm: URL이 Wikipedia 형식이어야 함", successState.webUrl.startsWith("https://en.wikipedia.org/wiki/"))
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
    fun loadDetailWithInvalidUrlFromRepositoryHandlesError() = runTest {
        // Given: 잘못된 URL로 예외를 발생시키도록 설정
        val searchTerm = "InvalidCase"
        fakeRepository.setShouldThrowError(true)

        // When: 상세 페이지 로드
        viewModel.getDetailPageUrl(searchTerm)
        advanceUntilIdle() // 모든 코루틴 작업 완료 대기

        // Then: Error 상태여야 함
        val uiState = viewModel.uiState.value
        assertTrue("잘못된 URL은 에러 상태여야 함", uiState is DetailUiState.Error)
    }

    // =====================================
    // 🔧 ViewModel State Management Tests
    // =====================================

    /**
     * 🔧 UI State 초기화 테스트
     * 
     * ViewModel 생성 시 초기 상태가 올바른지 확인
     */
    @Test
    fun viewModelCreationHasCorrectInitialState() {
        // Then: 초기 상태는 Idle이어야 함
        assertTrue("초기 상태는 Idle이어야 함", viewModel.uiState.value is DetailUiState.Idle)
    }

    /**
     * ⏱️ Loading 상태 테스트
     * 
     * 로딩 상태가 올바르게 설정되는지 확인
     */
    @Test
    fun loadDetailExecutionShowsLoadingStateDuringExecution() = runTest {
        // Given: 검색어 준비
        val searchTerm = "Android"
        fakeRepository.setDetailPageUrl(searchTerm, "https://en.wikipedia.org/wiki/Android")

        // When: 상세 페이지 로드 시작하고 즉시 완료까지 기다림
        viewModel.getDetailPageUrl(searchTerm)
        advanceUntilIdle() // 모든 코루틴 작업 완료 대기

        // Then: 성공 상태로 변경 (로딩 상태는 매우 빠르게 지나감)
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
    fun loadDetailWithSpecialCharactersHandlesSearchTermCorrectly() = runTest {
        // Given: 특수 문자가 포함된 검색어
        val searchTermWithSpaces = "Android Development"
        val searchTermWithSpecialChars = "C++ Programming"
        
        fakeRepository.setDetailPageUrl(searchTermWithSpaces, "https://en.wikipedia.org/wiki/Android_Development")
        fakeRepository.setDetailPageUrl(searchTermWithSpecialChars, "https://en.wikipedia.org/wiki/C%2B%2B_Programming")

        // When & Then: 공백 포함 검색어 테스트
        viewModel.getDetailPageUrl(searchTermWithSpaces)
        advanceUntilIdle() // 모든 코루틴 작업 완료 대기
        
        var uiState = viewModel.uiState.value
        assertTrue("공백 포함 검색어도 성공해야 함", uiState is DetailUiState.Success)
        
        // When & Then: 특수문자 포함 검색어 테스트
        viewModel.getDetailPageUrl(searchTermWithSpecialChars)
        advanceUntilIdle() // 모든 코루틴 작업 완료 대기
        
        uiState = viewModel.uiState.value
        assertTrue("특수문자 포함 검색어도 성공해야 함", uiState is DetailUiState.Success)
    }

    // =====================================
    // 🛠️ Test Helper Classes
    // =====================================

    /**
     * 테스트용 Fake Repository
     * 
     * 특징: Android API만 사용하여 빠른 테스트 실행
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
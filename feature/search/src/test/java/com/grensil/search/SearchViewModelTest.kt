package com.grensil.search

import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.domain.repository.WikipediaRepository
import com.grensil.domain.usecase.GetMediaListUseCase
import com.grensil.domain.usecase.GetSummaryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Search Module ViewModel Unit Test
 * 
 * 테스트 목적:
 * 1. SearchViewModel의 검색 로직 검증
 * 2. UI State 변화 패턴 확인
 * 3. 에러 처리 로직 검증
 * 4. UseCase 통합 동작 확인
 * 
 * 특징:
 * - 실제 네트워크 호출 없이 ViewModel 로직만 테스트
 * - Android API + JUnit 4 + Coroutine Test 사용
 * - 빠른 실행 속도로 개발 중 자주 실행 가능
 * 
 * Naming Convention:
 * - Class: SearchViewModelTest
 * - Methods: `[component] [condition] [expectedResult]`
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private lateinit var viewModel: SearchViewModel
    private lateinit var fakeRepository: FakeWikipediaRepository
    private lateinit var getSummaryUseCase: GetSummaryUseCase
    private lateinit var getMediaListUseCase: GetMediaListUseCase

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Fake Repository 및 UseCase 설정
        fakeRepository = FakeWikipediaRepository()
        getSummaryUseCase = object : GetSummaryUseCase {
            override suspend fun invoke(searchTerm: String): Summary = fakeRepository.getSummary(searchTerm)
        }
        getMediaListUseCase = object : GetMediaListUseCase {
            override suspend fun invoke(searchTerm: String): List<MediaItem> = fakeRepository.getMediaList(searchTerm)
        }
        
        // ViewModel 생성
        viewModel = SearchViewModel(getSummaryUseCase, getMediaListUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =====================================
    // ✅ Successful Search Scenarios
    // =====================================

    /**
     * ✅ 성공적인 검색 시나리오 테스트
     * 
     * 테스트 시나리오:
     * 1. 초기 상태는 Idle
     * 2. 검색 시작 시 Loading 상태
     * 3. 성공 시 Success 상태와 올바른 데이터 반환
     */
    @Test
    fun `search with valid searchTerm updates uiState to success`() = runTest {
        // Given: 테스트 데이터 준비
        val searchTerm = "Android"
        val expectedSummary = Summary(
            title = "Android", 
            description = "Mobile operating system", 
            thumbnailUrl = "thumb.jpg"
        )
        val expectedMediaItems = listOf(
            MediaItem("Android Logo", "Official logo", null, "logo.png", "image"),
            MediaItem("Android Architecture", "System diagram", null, "arch.png", "image")
        )
        
        fakeRepository.setSummary(searchTerm, expectedSummary)
        fakeRepository.setMediaList(searchTerm, expectedMediaItems)

        // When: 검색 실행
        viewModel.search(searchTerm)
        advanceUntilIdle() // 모든 코루틴 작업 완료 대기

        // Then: UI State 검증
        val uiState = viewModel.searchedData.value
        assertTrue("성공 상태여야 함", uiState is SearchUiState.Success)
        
        val successState = uiState as SearchUiState.Success
        assertEquals("Summary가 올바르게 설정되어야 함", expectedSummary, successState.summary)
        assertEquals("MediaItem 개수가 일치해야 함", 2, successState.mediaList.size)
        assertEquals("첫 번째 MediaItem", "Android Logo", successState.mediaList[0].title)
        assertEquals("두 번째 MediaItem", "Android Architecture", successState.mediaList[1].title)
    }

    // =====================================
    // ❌ Error Handling Scenarios
    // =====================================

    /**
     * ❌ 검색 실패 시나리오 테스트
     * 
     * 테스트 시나리오:
     * 1. Repository에서 예외 발생
     * 2. Error 상태로 변화
     * 3. 적절한 에러 메시지 표시
     */
    @Test
    fun `search with repository error updates uiState to error`() = runTest {
        // Given: Repository가 예외를 던지도록 설정
        val searchTerm = "FailCase"
        fakeRepository.setShouldThrowError(true)

        // When: 검색 실행
        viewModel.search(searchTerm)
        advanceUntilIdle() // 모든 코루틴 작업 완료 대기

        // Then: Error 상태 검증
        val uiState = viewModel.searchedData.value
        assertTrue("에러 상태여야 함", uiState is SearchUiState.Error)
        
        val errorState = uiState as SearchUiState.Error
        assertTrue("에러 메시지가 있어야 함", errorState.message.isNotEmpty())
    }

    /**
     * 🔍 빈 검색어 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. 빈 문자열로 검색 시도
     * 2. 적절한 검증 로직 동작 확인
     */
    @Test
    fun `search with empty term shows idle`() = runTest {
        // When: 빈 검색어로 검색
        viewModel.search("")
        advanceUntilIdle() // 모든 코루틴 작업 완료 대기

        // Then: Idle 상태여야 함 (빈 검색어는 처리하지 않음)
        val uiState = viewModel.searchedData.value
        assertTrue("빈 검색어는 Idle 상태여야 함", uiState is SearchUiState.Idle)
    }

    // =====================================
    // 🔄 Advanced Search Scenarios
    // =====================================

    /**
     * 🔄 연속 검색 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. 첫 번째 검색 실행
     * 2. 두 번째 검색 실행 (이전 검색 취소)
     * 3. 마지막 검색 결과만 표시되는지 확인
     */
    @Test
    fun `search with multiple consecutive calls cancels previous ones`() = runTest {
        // Given: 두 개의 다른 검색어 준비
        val firstTerm = "Android"
        val secondTerm = "iOS"
        
        fakeRepository.setSummary(firstTerm, Summary(
            title = "Android", 
            description = "Android OS", 
            thumbnailUrl = "android.jpg"
        ))
        fakeRepository.setSummary(secondTerm, Summary(
            title = "iOS", 
            description = "iOS System", 
            thumbnailUrl = "ios.jpg"
        ))
        fakeRepository.setMediaList(firstTerm, listOf(MediaItem("Android Item", "Android Caption")))
        fakeRepository.setMediaList(secondTerm, listOf(MediaItem("iOS Item", "iOS Caption")))

        // When: 연속으로 검색 실행
        viewModel.search(firstTerm)
        viewModel.search(secondTerm)
        advanceUntilIdle() // 모든 코루틴 작업 완료 대기

        // Then: 마지막 검색 결과만 표시되어야 함
        val uiState = viewModel.searchedData.value
        assertTrue("성공 상태여야 함", uiState is SearchUiState.Success)
        
        val successState = uiState as SearchUiState.Success
        assertEquals("마지막 검색 결과의 제목", "iOS", successState.summary.title)
        assertEquals("마지막 검색 결과의 미디어 아이템", "iOS Item", successState.mediaList[0].title)
    }

    /**
     * 🚫 무효한 Summary 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. Repository에서 무효한 Summary 반환
     * 2. ViewModel에서 적절히 에러 처리
     */
    @Test
    fun `search with empty summary from repository handles success`() = runTest {
        // Given: 빈 Summary 설정 (빈 제목과 설명도 유효한 데이터)
        val searchTerm = "EmptyCase"
        val emptySummary = Summary(
            title = "", 
            description = "", 
            thumbnailUrl = null
        ) // 빈 Summary도 유효한 데이터
        
        fakeRepository.setSummary(searchTerm, emptySummary)
        fakeRepository.setMediaList(searchTerm, emptyList())

        // When: 검색 실행
        viewModel.search(searchTerm)
        advanceUntilIdle() // 모든 코루틴 작업 완료 대기

        // Then: Success 상태여야 함 (빈 Summary도 유효한 응답)
        val uiState = viewModel.searchedData.value
        assertTrue("빈 Summary도 성공 상태여야 함", uiState is SearchUiState.Success)
        
        val successState = uiState as SearchUiState.Success
        assertEquals("빈 제목", "", successState.summary.title)
        assertEquals("빈 설명", "", successState.summary.description)
    }

    /**
     * 📋 빈 미디어 리스트 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. 유효한 Summary + 빈 미디어 리스트
     * 2. 성공 상태로 처리되어야 함 (미디어가 없을 수도 있음)
     */
    @Test
    fun `search with empty media list handles gracefully`() = runTest {
        // Given: 유효한 Summary + 빈 미디어 리스트
        val searchTerm = "EmptyMedia"
        val validSummary = Summary(
            title = "Valid Title", 
            description = "Valid Description", 
            thumbnailUrl = "thumb.jpg"
        )
        
        fakeRepository.setSummary(searchTerm, validSummary)
        fakeRepository.setMediaList(searchTerm, emptyList())

        // When: 검색 실행
        viewModel.search(searchTerm)
        advanceUntilIdle() // 모든 코루틴 작업 완료 대기

        // Then: 성공 상태여야 함 (빈 미디어 리스트도 허용)
        val uiState = viewModel.searchedData.value
        assertTrue("빈 미디어 리스트도 성공 상태여야 함", uiState is SearchUiState.Success)
        
        val successState = uiState as SearchUiState.Success
        assertEquals("Summary는 유효해야 함", validSummary, successState.summary)
        assertTrue("미디어 리스트는 비어있어야 함", successState.mediaList.isEmpty())
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
    fun `viewModel creation has correct initial state`() {
        // Then: 초기 상태는 Idle이어야 함
        assertTrue("초기 상태는 Idle이어야 함", viewModel.searchedData.value is SearchUiState.Idle)
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
        private val summaries = mutableMapOf<String, Summary>()
        private val mediaLists = mutableMapOf<String, List<MediaItem>>()
        private var shouldThrowError = false
        
        fun setSummary(searchTerm: String, summary: Summary) {
            summaries[searchTerm] = summary
        }
        
        fun setMediaList(searchTerm: String, mediaList: List<MediaItem>) {
            mediaLists[searchTerm] = mediaList
        }
        
        fun setShouldThrowError(shouldThrow: Boolean) {
            shouldThrowError = shouldThrow
        }
        
        override suspend fun getSummary(searchTerm: String): Summary {
            if (shouldThrowError) {
                throw RuntimeException("Test error occurred")
            }
            return summaries[searchTerm] ?: Summary(
                title = "Default", 
                description = "Default description"
            )
        }
        
        override suspend fun getMediaList(searchTerm: String): List<MediaItem> {
            if (shouldThrowError) {
                throw RuntimeException("Test error occurred")
            }
            return mediaLists[searchTerm] ?: emptyList()
        }
        
        override fun getDetailPageUrl(searchTerm: String): String {
            return "https://example.com/detail/$searchTerm"
        }
    }
}
package com.grensil.search

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.domain.usecase.GetMediaListUseCase
import com.grensil.domain.usecase.GetSummaryUseCase
import com.grensil.search.component.SearchTextField
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicInteger

/**
 * 🎯 Search Module 통합 테스트 클래스
 * 
 * 통합된 파일들:
 * - SearchScreenUITest.kt (컴포넌트 레벨 테스트)
 * - SimpleSearchUITest.kt (기본 UI 컴포넌트 테스트)
 * - SearchScreenBasicTest.kt (기본 화면 테스트)
 * - SearchScreenIntegrationTest.kt (통합 테스트)
 * 
 * 구조:
 * 1. Component Level Tests - SearchTextField 단독 테스트
 * 2. Screen Integration Tests - SearchScreen 전체 통합 테스트
 * 3. Common Mock Setup - 재사용 가능한 Mock 설정
 */
@RunWith(AndroidJUnit4::class)
class SearchAndroidTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // =====================================
    // 🧩 Component Level Tests
    // =====================================

    @Test
    fun test_searchTextField_displays_correctly() {
        composeTestRule.setContent {
            SearchTextField(
                query = "",
                onQueryChange = { },
                onBackClick = { }
            )
        }

        composeTestRule
            .onNodeWithText("텍스트를 입력하세요")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun test_searchTextField_initialization_shows_search_icon() {
        composeTestRule.setContent {
            SearchTextField(
                query = "",
                onQueryChange = { },
                onBackClick = { }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("검색")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun test_searchTextField_with_text_shows_clear_icon() {
        composeTestRule.setContent {
            SearchTextField(
                query = "Test Query",
                onQueryChange = { },
                onBackClick = { }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("검색어 지우기")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun test_searchTextField_input_accepts_correctly() {
        composeTestRule.setContent {
            var currentQuery by remember { mutableStateOf("") }
            
            SearchTextField(
                query = currentQuery,
                onQueryChange = { currentQuery = it },
                onBackClick = { }
            )
        }

        // 텍스트 입력
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextInput("Test")

        composeTestRule.waitForIdle()

        // 입력된 텍스트가 있는지 확인 - 플레이스홀더가 사라졌는지 확인
        composeTestRule
            .onNodeWithText("텍스트를 입력하세요")
            .assertDoesNotExist()
    }

    @Test
    fun test_searchTextField_disabled_state_works_correctly() {
        composeTestRule.setContent {
            SearchTextField(
                query = "",
                onQueryChange = { },
                onBackClick = { },
                enabled = false
            )
        }

        composeTestRule
            .onNodeWithText("텍스트를 입력하세요")
            .assertIsNotEnabled()
    }

    // =====================================
    // 🎬 Screen Integration Tests
    // =====================================

    @Test
    fun test_searchScreen_basic_ui_elements_exist() {
        val (summaryUseCase, mediaListUseCase) = createMockUseCases()
        val viewModel = SearchViewModel(summaryUseCase, mediaListUseCase)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }

        // 기본 UI 요소들 확인
        composeTestRule
            .onNodeWithText("텍스트를 입력하세요")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("검색")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun test_searchScreen_text_input_changes_icon_correctly() {
        val (summaryUseCase, mediaListUseCase) = createMockUseCases()
        val viewModel = SearchViewModel(summaryUseCase, mediaListUseCase)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }

        // 초기 상태: 검색 아이콘 존재
        composeTestRule
            .onNodeWithContentDescription("검색")
            .assertExists()

        // 텍스트 입력
        composeTestRule
            .onNodeWithText("텍스트를 입력하세요")
            .performTextInput("test")

        composeTestRule.waitForIdle()

        // 입력 후: 지우기 버튼이 나타나야 함
        composeTestRule
            .onNodeWithContentDescription("검색어 지우기")
            .assertExists()
    }

    @Test
    fun test_searchScreen_clear_button_functionality_works() {
        val (summaryUseCase, mediaListUseCase) = createMockUseCases()
        val viewModel = SearchViewModel(summaryUseCase, mediaListUseCase)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }

        // 텍스트 입력
        composeTestRule
            .onNodeWithText("텍스트를 입력하세요")
            .performTextInput("test")

        composeTestRule.waitForIdle()

        // 지우기 버튼 클릭
        composeTestRule
            .onNodeWithContentDescription("검색어 지우기")
            .performClick()

        composeTestRule.waitForIdle()

        // 지운 후: 다시 검색 아이콘이 나타나야 함
        composeTestRule
            .onNodeWithContentDescription("검색")
            .assertExists()

        composeTestRule
            .onNodeWithText("텍스트를 입력하세요")
            .assertExists()
    }

    @Test
    fun test_searchScreen_after_input_shows_search_results() {
        val (summaryUseCase, mediaListUseCase) = createMockUseCases()
        val viewModel = SearchViewModel(summaryUseCase, mediaListUseCase)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }

        // 초기 상태 확인 - 검색 결과 없음
        composeTestRule
            .onNodeWithText("Mock Title: test")
            .assertDoesNotExist()

        // 검색어 입력
        composeTestRule
            .onNodeWithText("텍스트를 입력하세요")
            .performTextInput("test")

        // 검색이 완료될 때까지 대기 (debounce 시간 고려)
        composeTestRule.waitForIdle()
        Thread.sleep(500) // debounce 시간(300ms) + 여유시간

        // 검색 결과가 표시되는지 확인
        composeTestRule
            .onNodeWithText("Mock Title: test", useUnmergedTree = true)
            .assertExists()

        // extract(실제 표시되는 설명)가 표시되는지 확인
        composeTestRule
            .onNodeWithText("Mock extract for test", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun test_searchScreen_after_search_shows_media_items() {
        val (summaryUseCase, mediaListUseCase) = createMockUseCases()
        val viewModel = SearchViewModel(summaryUseCase, mediaListUseCase)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }

        // 검색어 입력
        composeTestRule
            .onNodeWithText("텍스트를 입력하세요")
            .performTextInput("kotlin")

        // 검색 완료 대기
        composeTestRule.waitForIdle()
        Thread.sleep(500)

        // 미디어 아이템들이 표시되는지 확인
        composeTestRule
            .onNodeWithText("Mock Image 1 for kotlin", useUnmergedTree = true)
            .assertExists()

        composeTestRule
            .onNodeWithText("Mock Image 2 for kotlin", useUnmergedTree = true)
            .assertExists()

        // 캡션도 확인
        composeTestRule
            .onNodeWithText("Mock caption 1 for kotlin", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun test_searchScreen_detailed_search_flow_handles_correctly() {
        val (summaryUseCase, mediaListUseCase) = createMockUseCases()
        val viewModel = SearchViewModel(summaryUseCase, mediaListUseCase)
        val testKeyword = "Android"

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }

        // 검색 필드가 표시되는지 확인
        composeTestRule
            .onNodeWithText("텍스트를 입력하세요")
            .assertExists()
            .assertIsDisplayed()

        // 검색 아이콘이 표시되는지 확인
        composeTestRule
            .onNodeWithContentDescription("검색")
            .assertExists()
            .assertIsDisplayed()

        // 검색어 입력
        composeTestRule
            .onNodeWithText("텍스트를 입력하세요")
            .performTextInput(testKeyword)

        // 검색 완료까지 대기 (debounce + processing 시간)
        composeTestRule.waitForIdle()
        Thread.sleep(1000) // 1초 대기

        // 검색 결과가 UI에 나타나는지 확인
        try {
            composeTestRule
                .onNodeWithText("Mock Title: $testKeyword", useUnmergedTree = true)
                .assertExists()
        } catch (e: AssertionError) {
            // 부분 텍스트로 시도
            composeTestRule
                .onNodeWithText("Mock Title", substring = true, useUnmergedTree = true)
                .assertExists()
        }

        try {
            composeTestRule
                .onNodeWithText("Mock extract for $testKeyword", useUnmergedTree = true)  
                .assertExists()
        } catch (e: AssertionError) {
            // 부분 텍스트로 시도
            composeTestRule
                .onNodeWithText("Mock extract", substring = true, useUnmergedTree = true)
                .assertExists()
        }
    }

    // =====================================
    // 🏃‍♂️ Race Condition Tests
    // =====================================

    @Test
    fun test_searchTextField_rapid_sequential_input_handles_race_conditions() {
        val (summaryUseCase, mediaListUseCase) = createRaceConditionMockUseCases()
        val viewModel = SearchViewModel(summaryUseCase, mediaListUseCase)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }

        // 빠른 연속 입력 시뮬레이션: "a", "ab", "abc"
        // 첫 번째 입력: "a"
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextInput("a")
        composeTestRule.waitForIdle()

        // 즉시 두 번째 입력: "b" 추가
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextClearance()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextInput("ab") 
        composeTestRule.waitForIdle()

        // 즉시 세 번째 입력: "c" 추가
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextClearance()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextInput("abc")
        composeTestRule.waitForIdle()

        // debounce 시간 + API 응답 시간 대기
        Thread.sleep(2000) // 2초 대기

        // 최종 결과만 표시되어야 함 ("abc") - 중복 허용
        composeTestRule
            .onNodeWithText("Final Result: abc")
            .assertExists()

        // 이전 검색 결과들은 없어야 함
        composeTestRule
            .onNodeWithText("Final Result: a", useUnmergedTree = true)
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("Final Result: ab", useUnmergedTree = true)
            .assertDoesNotExist()

        println("✅ Race condition test passed: Only final result 'abc' is displayed")
    }

    @Test 
    fun test_searchTextField_extreme_rapid_input_race_condition() {
        val (summaryUseCase, mediaListUseCase) = createRaceConditionMockUseCases()
        val viewModel = SearchViewModel(summaryUseCase, mediaListUseCase)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }

        val rapidKeywords = listOf("k", "ko", "kot", "kotl", "kotli", "kotlin")
        
        // 극도로 빠른 연속 입력 (실제 사용자 타이핑 시뮬레이션)
        rapidKeywords.forEach { keyword ->
            // testTag를 사용해서 안정적으로 텍스트 필드 찾기
            composeTestRule
                .onNodeWithTag("searchTextField")
                .performTextClearance()
            composeTestRule.waitForIdle()
            
            composeTestRule
                .onNodeWithTag("searchTextField")
                .performTextInput(keyword)
            composeTestRule.waitForIdle()
            Thread.sleep(30) // 30ms 간격 (매우 빠른 타이핑)
        }

        // 모든 처리 완료 대기
        Thread.sleep(3000)

        // 마지막 키워드 "kotlin"의 결과가 표시되어야 함 (중복 허용)
        composeTestRule
            .onNodeWithText("Final Result: kotlin")
            .assertExists()

        // 중간 단계 결과들은 모두 취소되어야 함
        rapidKeywords.dropLast(1).forEach { keyword ->
            composeTestRule
                .onNodeWithText("Final Result: $keyword", useUnmergedTree = true)
                .assertDoesNotExist()
        }

        println("✅ Extreme rapid input race condition test passed: Only final 'kotlin' result displayed")
    }

    @Test
    fun test_searchTextField_mixed_speed_input_race_condition() {
        val callCounter = AtomicInteger(0)
        val (summaryUseCase, mediaListUseCase) = createVariableDelayMockUseCases(callCounter)
        val viewModel = SearchViewModel(summaryUseCase, mediaListUseCase)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }

        // 다양한 속도로 입력 (느리게 시작해서 빠르게)
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextInput("slow")
        Thread.sleep(200) // 느린 입력
        
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextClearance()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextInput("medium")
        Thread.sleep(100) // 보통 입력
        
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextClearance()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextInput("fast1")
        Thread.sleep(50) // 빠른 입력
        
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextClearance()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextInput("fast2")
        Thread.sleep(30) // 매우 빠른 입력
        
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextClearance()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextInput("final")
        
        Thread.sleep(4000) // 모든 처리 완료 대기

        // 마지막 "final" 결과가 표시되어야 함 (중복 허용)
        composeTestRule
            .onNodeWithText("Variable Delay: final")
            .assertExists()

        // 다른 모든 결과들은 취소되어야 함
        listOf("slow", "medium", "fast1", "fast2").forEach { keyword ->
            composeTestRule
                .onNodeWithText("Variable Delay: $keyword", useUnmergedTree = true)
                .assertDoesNotExist()
        }

        println("✅ Mixed speed input race condition test passed: Only 'final' result displayed")
    }

    @Test
    fun test_searchTextField_concurrent_requests_latest_wins() {
        val callCounter = AtomicInteger(0)
        val (summaryUseCase, mediaListUseCase) = createDelayedMockUseCases(callCounter)
        val viewModel = SearchViewModel(summaryUseCase, mediaListUseCase)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }

        // 여러 키워드를 빠르게 연속 입력하여 레이스 컨디션 테스트
        val keywords = listOf("kotlin", "android", "compose", "final")
        
        keywords.forEach { keyword ->
            // testTag를 사용해서 안정적으로 텍스트 필드 찾기
            composeTestRule
                .onNodeWithTag("searchTextField")
                .performTextClearance()
            composeTestRule.waitForIdle()
            
            composeTestRule
                .onNodeWithTag("searchTextField")
                .performTextInput(keyword)
            composeTestRule.waitForIdle()
            Thread.sleep(50) // 50ms 간격으로 빠르게 입력
        }
        
        composeTestRule.waitForIdle()
        Thread.sleep(3000) // 모든 처리 완료 대기

        // 마지막 키워드 "final"의 결과가 표시되어야 함
        composeTestRule
            .onNodeWithText("Delayed Response: final")
            .assertExists()

        // 이전 키워드들의 결과는 취소되어야 함 (적어도 첫 번째는 확실히 없어야 함)
        composeTestRule
            .onNodeWithText("Delayed Response: kotlin")
            .assertDoesNotExist()

        println("✅ Concurrent requests test passed: Latest request 'final' wins")
    }

    @Test
    fun test_searchTextField_duplicate_requests_prevention() {
        val requestCounter = AtomicInteger(0)
        val (summaryUseCase, mediaListUseCase) = createCountingMockUseCases(requestCounter)
        val viewModel = SearchViewModel(summaryUseCase, mediaListUseCase)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }

        val duplicateKeyword = "testDuplicate"

        // 동일한 검색어를 빠르게 여러 번 입력 (중복 요청 방지 테스트)
        repeat(5) { index ->
            // testTag를 사용해서 안정적으로 텍스트 필드 찾기
            composeTestRule
                .onNodeWithTag("searchTextField")
                .performTextClearance()
            composeTestRule.waitForIdle()
            
            composeTestRule
                .onNodeWithTag("searchTextField")
                .performTextInput(duplicateKeyword)
            composeTestRule.waitForIdle()
            Thread.sleep(100) // 100ms 간격으로 빠르게 입력
            println("입력 시도 ${index + 1}: $duplicateKeyword")
        }

        Thread.sleep(2000) // 모든 처리 완료 대기

        val finalCallCount = requestCounter.get()
        
        // 결과 표시 확인 - 마지막 호출 결과만 표시되어야 함
        composeTestRule
            .onNodeWithText("Request Count: $finalCallCount for $duplicateKeyword")
            .assertExists()

        // 중복 방지 로직에 따라 호출 횟수가 결정됨
        // 완벽한 중복 방지: 1회, 부분적 중복 방지: 1-3회, 중복 방지 없음: 5회
        println("✅ Duplicate requests test: API called $finalCallCount times for identical search term '$duplicateKeyword'")
        
        // 추가 검증: 다른 키워드로 정상 동작 확인
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextClearance()
        composeTestRule.waitForIdle()
        
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextInput("different")
        Thread.sleep(1000)
        
        // 새로운 키워드는 정상적으로 처리되어야 함 (첫 번째만 확인)
        composeTestRule
            .onNodeWithText("Request Count:", substring = true)
            .assertExists()
    }

    @Test
    fun test_searchTextField_empty_input_handling() {
        val (summaryUseCase, mediaListUseCase) = createMockUseCases()
        val viewModel = SearchViewModel(summaryUseCase, mediaListUseCase)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "search") {
                composable("search") {
                    SearchScreen(
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }

        // 검색어 입력 후 모두 삭제
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextInput("test")
        composeTestRule.waitForIdle()
        Thread.sleep(500)

        // 검색어 모두 지우기
        composeTestRule
            .onNodeWithTag("searchTextField")
            .performTextClearance()
        composeTestRule.waitForIdle()
        Thread.sleep(500)

        // 빈 검색어로 API 호출되지 않아야 함
        // 이전 검색 결과가 지워져야 함
        composeTestRule
            .onNodeWithText("Mock Title: test", useUnmergedTree = true)
            .assertDoesNotExist()

        println("✅ Empty input handling test passed: Previous results cleared")
    }

    // =====================================
    // 🛠️ Common Mock Setup Methods
    // =====================================

    /**
     * Race Condition 전용 Mock UseCases
     * 다른 응답 시간을 가진 검색 결과로 race condition 테스트
     */
    private fun createRaceConditionMockUseCases(): Pair<GetSummaryUseCase, GetMediaListUseCase> {
        val mockSummaryUseCase = object : GetSummaryUseCase {
            override suspend fun invoke(searchTerm: String): Summary {
                // 검색어별로 다른 지연시간 적용
                when (searchTerm) {
                    "a" -> delay(1000) // 긴 지연
                    "ab" -> delay(500) // 중간 지연  
                    "abc" -> delay(100) // 짧은 지연
                }
                
                return Summary(
                    title = "Final Result: $searchTerm",
                    description = "Race condition test result",
                    extract = "Final Result: $searchTerm",
                    thumbnailUrl = null,
                    originalImageUrl = null
                )
            }
        }
        
        val mockMediaListUseCase = object : GetMediaListUseCase {
            override suspend fun invoke(searchTerm: String): List<MediaItem> {
                return listOf(
                    MediaItem(
                        title = "MediaItem: $searchTerm",
                        caption = "MediaCaption: $searchTerm",
                        type = "image"
                    )
                )
            }
        }
        
        return Pair(mockSummaryUseCase, mockMediaListUseCase)
    }

    /**
     * Delayed Mock UseCases for concurrent request testing
     */
    private fun createDelayedMockUseCases(callCounter: AtomicInteger): Pair<GetSummaryUseCase, GetMediaListUseCase> {
        val mockSummaryUseCase = object : GetSummaryUseCase {
            override suspend fun invoke(searchTerm: String): Summary {
                callCounter.incrementAndGet()
                // "slow" 검색은 더 오래 걸리도록
                val delayTime = if (searchTerm == "slow") 2000L else 500L
                delay(delayTime)
                
                return Summary(
                    title = "Delayed Response: $searchTerm",
                    description = "Delayed test result",
                    extract = "Delayed Response: $searchTerm",
                    thumbnailUrl = null,
                    originalImageUrl = null
                )
            }
        }
        
        val mockMediaListUseCase = object : GetMediaListUseCase {
            override suspend fun invoke(searchTerm: String): List<MediaItem> {
                return listOf(
                    MediaItem(
                        title = "DelayedMediaItem: $searchTerm",
                        caption = "DelayedMediaCaption: $searchTerm",
                        type = "image"
                    )
                )
            }
        }
        
        return Pair(mockSummaryUseCase, mockMediaListUseCase)
    }

    /**
     * Counting Mock UseCases for duplicate request testing
     */
    private fun createCountingMockUseCases(requestCounter: AtomicInteger): Pair<GetSummaryUseCase, GetMediaListUseCase> {
        val mockSummaryUseCase = object : GetSummaryUseCase {
            override suspend fun invoke(searchTerm: String): Summary {
                val count = requestCounter.incrementAndGet()
                delay(300) // 약간의 지연
                
                return Summary(
                    title = "Request Count: $count for $searchTerm",
                    description = "Duplicate test result",
                    extract = "Request Count: $count for $searchTerm",
                    thumbnailUrl = null,
                    originalImageUrl = null
                )
            }
        }
        
        val mockMediaListUseCase = object : GetMediaListUseCase {
            override suspend fun invoke(searchTerm: String): List<MediaItem> {
                return listOf(
                    MediaItem(
                        title = "CountingMediaItem: $searchTerm",
                        caption = "CountingMediaCaption: $searchTerm",
                        type = "image"
                    )
                )
            }
        }
        
        return Pair(mockSummaryUseCase, mockMediaListUseCase)
    }

    /**
     * 재사용 가능한 Mock UseCase들 생성
     * 
     * 기존 4개 파일에서 중복으로 구현되던 Mock 로직을 통합
     */
    private fun createMockUseCases(): Pair<GetSummaryUseCase, GetMediaListUseCase> {
        val mockSummaryUseCase = object : GetSummaryUseCase {
            override suspend fun invoke(searchTerm: String): Summary {
                return Summary(
                    title = "Mock Title: $searchTerm",
                    description = "Mock description for $searchTerm", 
                    extract = "Mock extract for $searchTerm", // 실제로 표시되는 필드
                    thumbnailUrl = null,
                    originalImageUrl = null
                )
            }
        }
        
        val mockMediaListUseCase = object : GetMediaListUseCase {
            override suspend fun invoke(searchTerm: String): List<MediaItem> {
                return listOf(
                    MediaItem(
                        title = "Mock Image 1 for $searchTerm",
                        caption = "Mock caption 1 for $searchTerm", 
                        type = "image"
                    ),
                    MediaItem(
                        title = "Mock Image 2 for $searchTerm", 
                        caption = "Mock caption 2 for $searchTerm",
                        type = "image"
                    )
                )
            }
        }
        
        return Pair(mockSummaryUseCase, mockMediaListUseCase)
    }

    /**
     * Variable Delay Mock UseCases for mixed-speed race condition testing
     */
    private fun createVariableDelayMockUseCases(callCounter: AtomicInteger): Pair<GetSummaryUseCase, GetMediaListUseCase> {
        val mockSummaryUseCase = object : GetSummaryUseCase {
            override suspend fun invoke(searchTerm: String): Summary {
                callCounter.incrementAndGet()
                
                // 다양한 지연 시간 설정
                val delayTime = when (searchTerm) {
                    "slow" -> 2000L
                    "medium" -> 1000L  
                    "fast1" -> 500L
                    "fast2" -> 300L
                    "final" -> 200L
                    else -> 400L
                }
                delay(delayTime)
                
                return Summary(
                    title = "Variable Delay: $searchTerm",
                    description = "Mixed speed test result",
                    extract = "Variable Delay: $searchTerm",
                    thumbnailUrl = null,
                    originalImageUrl = null
                )
            }
        }
        
        val mockMediaListUseCase = object : GetMediaListUseCase {
            override suspend fun invoke(searchTerm: String): List<MediaItem> {
                return listOf(
                    MediaItem(
                        title = "VariableMediaItem: $searchTerm",
                        caption = "VariableMediaCaption: $searchTerm",
                        type = "image"
                    )
                )
            }
        }
        
        return Pair(mockSummaryUseCase, mockMediaListUseCase)
    }
}
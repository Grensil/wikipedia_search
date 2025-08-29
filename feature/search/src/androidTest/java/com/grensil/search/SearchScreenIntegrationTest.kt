package com.grensil.search

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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchScreenIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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

    @Test
    fun searchScreen_displays_correctly_with_real_screen() {
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
            .performTextInput("Android")

        // 검색 완료까지 대기 (debounce + processing 시간)
        composeTestRule.waitForIdle()
        Thread.sleep(1000) // 1초 대기로 증가

        // 먼저 전체 트리를 출력해서 실제 UI 구조 확인
        composeTestRule.onRoot().printToLog("SearchScreen")

        // 검색 결과가 UI에 나타나는지 확인 (여러 방법으로 시도)
        try {
            composeTestRule
                .onNodeWithText("Mock Title: Android", useUnmergedTree = true)
                .assertExists()
        } catch (e: AssertionError) {
            // 부분 텍스트로 시도
            composeTestRule
                .onNodeWithText("Mock Title", substring = true, useUnmergedTree = true)
                .assertExists()
        }

        try {
            composeTestRule
                .onNodeWithText("Mock extract for Android", useUnmergedTree = true)  
                .assertExists()
        } catch (e: AssertionError) {
            // 부분 텍스트로 시도
            composeTestRule
                .onNodeWithText("Mock extract", substring = true, useUnmergedTree = true)
                .assertExists()
        }
    }

    @Test
    fun searchScreen_can_input_text() {
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
            .performTextInput("Android")

        composeTestRule.waitForIdle()

        // 입력된 텍스트 확인
        composeTestRule
            .onNodeWithText("Android")
            .assertExists()

        // 지우기 버튼이 나타나는지 확인
        composeTestRule
            .onNodeWithContentDescription("검색어 지우기")
            .assertExists()
    }

    @Test
    fun searchScreen_clear_button_works() {
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
            .performTextInput("Test")

        composeTestRule.waitForIdle()

        // 지우기 버튼 클릭
        composeTestRule
            .onNodeWithContentDescription("검색어 지우기")
            .performClick()

        composeTestRule.waitForIdle()

        // 텍스트가 지워졌는지 확인 (검색 아이콘이 다시 나타남)
        composeTestRule
            .onNodeWithContentDescription("검색")
            .assertExists()

        composeTestRule
            .onNodeWithText("텍스트를 입력하세요")
            .assertExists()
    }

    @Test
    fun searchScreen_shows_search_results_after_input() {
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
    fun searchScreen_shows_media_items_after_search() {
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
}
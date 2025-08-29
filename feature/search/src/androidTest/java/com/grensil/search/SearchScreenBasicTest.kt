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
class SearchScreenBasicTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockUseCases(): Pair<GetSummaryUseCase, GetMediaListUseCase> {
        val mockSummaryUseCase = object : GetSummaryUseCase {
            override suspend fun invoke(searchTerm: String): Summary {
                return Summary(
                    title = "Test Title",
                    description = "Test Description",
                    extract = "Test Extract",
                    thumbnailUrl = null,
                    originalImageUrl = null
                )
            }
        }
        
        val mockMediaListUseCase = object : GetMediaListUseCase {
            override suspend fun invoke(searchTerm: String): List<MediaItem> {
                return listOf(
                    MediaItem(
                        title = "Test Media",
                        caption = "Test Caption",
                        type = "image"
                    )
                )
            }
        }
        
        return Pair(mockSummaryUseCase, mockMediaListUseCase)
    }

    @Test
    fun searchScreen_basic_ui_elements_exist() {
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

        // 기본 UI 요소들만 확인
        composeTestRule
            .onNodeWithText("텍스트를 입력하세요")
            .assertExists()

        composeTestRule
            .onNodeWithContentDescription("검색")
            .assertExists()
    }

    @Test
    fun searchScreen_text_input_changes_icon() {
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
    fun searchScreen_clear_button_functionality() {
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
    }
}
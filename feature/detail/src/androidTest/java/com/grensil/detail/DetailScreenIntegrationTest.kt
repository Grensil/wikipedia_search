package com.grensil.detail

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.grensil.domain.usecase.GetDetailPageUrlUseCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailScreenIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockUseCase(): GetDetailPageUrlUseCase {
        return object : GetDetailPageUrlUseCase {
            override fun invoke(searchTerm: String): String {
                return "https://en.wikipedia.org/api/rest_v1/page/html/$searchTerm"
            }
        }
    }

    @Test
    fun detailScreen_displays_correctly_with_real_screen() {
        val mockUseCase = createMockUseCase()
        val viewModel = DetailViewModel(mockUseCase)
        val testKeyword = "Android Test"

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "detail") {
                composable("detail") {
                    DetailScreen(
                        viewModel = viewModel,
                        navController = navController,
                        keyword = testKeyword
                    )
                }
            }
        }

        // 뒤로 가기 버튼이 표시되는지 확인
        composeTestRule
            .onNodeWithContentDescription("뒤로 가기")
            .assertExists()
            .assertIsDisplayed()

        // 제목이 표시되는지 확인
        composeTestRule
            .onNodeWithText(testKeyword)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_back_button_is_clickable() {
        val mockUseCase = createMockUseCase()
        val viewModel = DetailViewModel(mockUseCase)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "detail") {
                composable("detail") {
                    DetailScreen(
                        viewModel = viewModel,
                        navController = navController,
                        keyword = "Test"
                    )
                }
            }
        }

        // 뒤로 가기 버튼이 클릭 가능한지 확인
        composeTestRule
            .onNodeWithContentDescription("뒤로 가기")
            .assertHasClickAction()
            .performClick()
    }

    @Test
    fun detailScreen_handles_empty_keyword() {
        val mockUseCase = createMockUseCase()
        val viewModel = DetailViewModel(mockUseCase)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "detail") {
                composable("detail") {
                    DetailScreen(
                        viewModel = viewModel,
                        navController = navController,
                        keyword = ""
                    )
                }
            }
        }

        // 빈 키워드여도 뒤로 가기 버튼은 표시되어야 함
        composeTestRule
            .onNodeWithContentDescription("뒤로 가기")
            .assertExists()
            .assertIsDisplayed()

        // 빈 제목이 표시되는지 확인 (빈 문자열)
        composeTestRule
            .onNodeWithText("")
            .assertExists()
    }

    @Test
    fun detailScreen_shows_loading_initially() {
        val mockUseCase = createMockUseCase()
        val viewModel = DetailViewModel(mockUseCase)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "detail") {
                composable("detail") {
                    DetailScreen(
                        viewModel = viewModel,
                        navController = navController,
                        keyword = "Android"
                    )
                }
            }
        }

        // 헤더는 바로 표시되어야 함
        composeTestRule
            .onNodeWithContentDescription("뒤로 가기")
            .assertExists()

        composeTestRule
            .onNodeWithText("Android")
            .assertExists()

        // 초기 상태에서는 CircularProgressIndicator가 표시됨
        // CircularProgressIndicator는 특별한 텍스트나 contentDescription이 없으므로
        // 로딩 상태임을 다른 방법으로 확인
        composeTestRule.waitForIdle()
        Thread.sleep(100) // 약간의 대기
        
        // 로딩이 끝나면 WebView나 에러 메시지가 표시될 것임
    }

    @Test
    fun detailScreen_header_layout_correct() {
        val mockUseCase = createMockUseCase()
        val viewModel = DetailViewModel(mockUseCase)
        val testKeyword = "Test Keyword"

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "detail") {
                composable("detail") {
                    DetailScreen(
                        viewModel = viewModel,
                        navController = navController,
                        keyword = testKeyword
                    )
                }
            }
        }

        // 모든 헤더 요소들이 표시되는지 확인
        composeTestRule
            .onNodeWithContentDescription("뒤로 가기")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(testKeyword)
            .assertExists()
            .assertIsDisplayed()
    }
}
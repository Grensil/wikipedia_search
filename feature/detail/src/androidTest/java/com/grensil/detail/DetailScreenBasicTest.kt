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
class DetailScreenBasicTest {

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
    fun detailScreen_basic_ui_elements_exist() {
        val mockUseCase = createMockUseCase()
        val viewModel = DetailViewModel(mockUseCase)
        val testKeyword = "Android"

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

        // 기본 UI 요소들만 확인
        composeTestRule
            .onNodeWithContentDescription("뒤로 가기")
            .assertExists()
            .assertIsDisplayed()

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
    }

    @Test
    fun detailScreen_header_displays_keyword() {
        val mockUseCase = createMockUseCase()
        val viewModel = DetailViewModel(mockUseCase)
        val testKeyword = "Kotlin Programming"

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

        // 키워드가 헤더에 표시되는지 확인
        composeTestRule
            .onNodeWithText(testKeyword)
            .assertExists()
            .assertIsDisplayed()

        // 뒤로 가기 버튼도 함께 표시되는지 확인
        composeTestRule
            .onNodeWithContentDescription("뒤로 가기")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_initial_state_shows_loading_or_idle() {
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

        // 초기 상태에서 화면이 정상적으로 렌더링되는지 확인
        composeTestRule.waitForIdle()
    }
}
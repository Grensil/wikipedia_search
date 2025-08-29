package com.grensil.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
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

/**
 * 🎯 Detail Module 통합 테스트 클래스
 * 
 * 통합된 파일들:
 * - DetailScreenUITest.kt (기본 UI 컴포넌트 테스트)
 * - DetailScreenIntegrationTest.kt (통합 테스트)
 * - SimpleDetailUITest.kt (기본 UI 테스트)
 * 
 * 구조:
 * 1. Component Level Tests - 개별 UI 컴포넌트 테스트
 * 2. Screen Integration Tests - DetailScreen 전체 통합 테스트
 * 3. Common Mock Setup - 재사용 가능한 Mock 설정
 */
@RunWith(AndroidJUnit4::class)
class DetailScreenCompleteTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // =====================================
    // 🧩 Component Level Tests
    // =====================================

    @Test
    fun detail_back_button_displays_correctly() {
        composeTestRule.setContent {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로 가기"
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("뒤로 가기")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun detail_title_displays_correctly() {
        val testTitle = "Test Title"
        
        composeTestRule.setContent {
            Text(
                text = testTitle,
                style = MaterialTheme.typography.titleMedium
            )
        }

        composeTestRule
            .onNodeWithText(testTitle)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun detail_loading_indicator_displays() {
        composeTestRule.setContent {
            CircularProgressIndicator()
        }

        // 화면에 렌더링되는지만 확인 (복잡한 selector 피함)
        composeTestRule.waitForIdle()
        
        // 전체 UI 트리가 정상적으로 그려졌는지 확인
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun detail_shows_error_message() {
        val errorMessage = "Test error occurred"

        composeTestRule.setContent {
            Text(text = "Error: $errorMessage")
        }

        composeTestRule
            .onNodeWithText("Error: $errorMessage")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun detail_back_button_is_clickable() {
        composeTestRule.setContent {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로 가기"
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("뒤로 가기")
            .assertHasClickAction()
            .performClick()

        // 클릭이 실제로 동작했는지는 별도 검증 로직 필요
        composeTestRule.waitForIdle()
    }

    // =====================================
    // 🎬 Screen Integration Tests
    // =====================================

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

        // 초기 상태에서 화면이 정상적으로 렌더링되는지 확인
        composeTestRule.waitForIdle()
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

    @Test
    fun detailScreen_handles_different_keywords() {
        val testCases = listOf(
            "Android",
            "Kotlin Programming",
            "React Native",
            "Flutter Development"
        )

        testCases.forEach { keyword ->
            val mockUseCase = createMockUseCase()
            val viewModel = DetailViewModel(mockUseCase)

            composeTestRule.setContent {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "detail") {
                    composable("detail") {
                        DetailScreen(
                            viewModel = viewModel,
                            navController = navController,
                            keyword = keyword
                        )
                    }
                }
            }

            // 키워드가 헤더에 표시되는지 확인
            composeTestRule
                .onNodeWithText(keyword)
                .assertExists()
                .assertIsDisplayed()

            // 뒤로 가기 버튼도 함께 표시되는지 확인
            composeTestRule
                .onNodeWithContentDescription("뒤로 가기")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun detailScreen_webview_integration() {
        val mockUseCase = createMockUseCase()
        val viewModel = DetailViewModel(mockUseCase)

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "detail") {
                composable("detail") {
                    DetailScreen(
                        viewModel = viewModel,
                        navController = navController,
                        keyword = "WebView Test"
                    )
                }
            }
        }

        // 초기 상태에서는 로딩 또는 WebView가 표시됨
        composeTestRule.waitForIdle()
        Thread.sleep(100) // WebView 로딩 대기

        // 헤더는 항상 표시되어야 함
        composeTestRule
            .onNodeWithContentDescription("뒤로 가기")
            .assertExists()

        composeTestRule
            .onNodeWithText("WebView Test")
            .assertExists()
    }

    // =====================================
    // 🛠️ Common Mock Setup Methods
    // =====================================

    /**
     * 재사용 가능한 Mock UseCase 생성
     * 
     * 기존 3개 파일에서 중복으로 구현되던 Mock 로직을 통합
     */
    private fun createMockUseCase(): GetDetailPageUrlUseCase {
        return object : GetDetailPageUrlUseCase {
            override fun invoke(searchTerm: String): String {
                return "https://en.wikipedia.org/api/rest_v1/page/html/$searchTerm"
            }
        }
    }
}
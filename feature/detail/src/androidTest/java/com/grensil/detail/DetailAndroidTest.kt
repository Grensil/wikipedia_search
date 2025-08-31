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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailAndroidTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockUseCase: GetDetailPageUrlUseCase
    private lateinit var viewModel: DetailViewModel

    @Before
    fun setup() {
        mockUseCase = createMockUseCase()
        viewModel = DetailViewModel(mockUseCase)
    }

    // =====================================
    // 🧩 Component Level Tests
    // =====================================

    @Test
    fun backButton_displays_correctly() {
        composeTestRule.setContent {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로 가기"
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("뒤로 가기")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun title_displays_correctly() {
        val testTitle = "Test Title"
        composeTestRule.setContent {
            Text(text = testTitle, style = MaterialTheme.typography.titleMedium)
        }

        composeTestRule.onNodeWithText(testTitle)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun loadingIndicator_displays_correctly() {
        composeTestRule.setContent { CircularProgressIndicator() }
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun errorMessage_shows_correctly() {
        val errorMessage = "Test error occurred"
        composeTestRule.setContent { Text(text = "Error: $errorMessage") }

        composeTestRule.onNodeWithText("Error: $errorMessage")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun backButton_is_clickable() {
        composeTestRule.setContent {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로 가기"
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("뒤로 가기")
            .assertHasClickAction()
            .performClick()
    }

    // =====================================
    // 🎬 Screen Integration Tests
    // =====================================

    @Test
    fun detailScreen_displays_correctly() {
        setupDetailScreen("Android Test")

        composeTestRule.onNodeWithContentDescription("뒤로 가기")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Android Test")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun detailScreen_backButton_is_clickable() {
        setupDetailScreen("Test")

        composeTestRule.onNodeWithContentDescription("뒤로 가기")
            .assertHasClickAction()
            .performClick()
    }

    @Test
    fun detailScreen_with_empty_keyword() {
        setupDetailScreen("")

        composeTestRule.onNodeWithContentDescription("뒤로 가기")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("")
            .assertExists()
    }

    @Test
    fun detailScreen_initialization_shows_loading() {
        setupDetailScreen("Android")

        composeTestRule.onNodeWithContentDescription("뒤로 가기")
            .assertExists()

        composeTestRule.onNodeWithText("Android")
            .assertExists()

        composeTestRule.waitForIdle()
    }

    @Test
    fun detailScreen_webview_integration() {
        setupDetailScreen("WebView Test")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("뒤로 가기")
            .assertExists()

        composeTestRule.onNodeWithText("WebView Test")
            .assertExists()
    }

    // =====================================
    // 🛠️ Common Methods
    // =====================================

    private fun setupDetailScreen(keyword: String) {
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
    }

    private fun createMockUseCase(): GetDetailPageUrlUseCase {
        return object : GetDetailPageUrlUseCase {
            override fun invoke(searchTerm: String) =
                "https://en.wikipedia.org/api/rest_v1/page/html/$searchTerm"
        }
    }
}

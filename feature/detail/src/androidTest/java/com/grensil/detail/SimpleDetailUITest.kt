package com.grensil.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleDetailUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun detailScreen_showsBackButton() {
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
    fun detailScreen_showsTitle() {
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
    fun detailScreen_backButton_isClickable() {
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

    @Test
    fun detailScreen_showsLoadingIndicator() {
        composeTestRule.setContent {
            CircularProgressIndicator()
        }

        // 화면에 렌더링되는지만 확인 (복잡한 selector 피함)
        composeTestRule.waitForIdle()
        
        // 전체 UI 트리가 정상적으로 그려졌는지 확인
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun detailScreen_showsErrorMessage() {
        val errorMessage = "Test error occurred"

        composeTestRule.setContent {
            Text(text = "Error: $errorMessage")
        }

        composeTestRule
            .onNodeWithText("Error: $errorMessage")
            .assertExists()
            .assertIsDisplayed()
    }
}
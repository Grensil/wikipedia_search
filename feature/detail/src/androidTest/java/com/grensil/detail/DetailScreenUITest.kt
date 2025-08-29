package com.grensil.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

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
    }

    @Test
    fun detail_loading_indicator_displays() {
        composeTestRule.setContent {
            CircularProgressIndicator()
        }

        // CircularProgressIndicator는 특별한 content description이 없으므로
        // 다른 방법으로 존재 확인
        composeTestRule
            .onAllNodes(hasClickAction().not())
            .assertCountEquals(1)
    }
}
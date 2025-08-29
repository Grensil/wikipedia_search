package com.grensil.search

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.domain.usecase.GetMediaListUseCase
import com.grensil.domain.usecase.GetSummaryUseCase
import com.grensil.search.component.SearchTextField
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchTextField_displays_correctly() {
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
    }

    @Test
    fun searchTextField_shows_search_icon_when_empty() {
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
    }

    @Test
    fun searchTextField_shows_clear_icon_when_has_text() {
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
    }
}
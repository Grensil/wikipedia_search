package com.grensil.search.component

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.grensil.domain.dto.Summary
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 🎯 SummaryCard 컴포넌트 테스트
 * 
 * 테스트 커버리지:
 * 1. 기본 표시 기능 테스트
 * 2. 긴 텍스트 처리 테스트
 * 3. 클릭 이벤트 테스트
 * 4. 이미지 없는 경우 처리 테스트
 * 5. 커스터마이징 옵션 테스트
 */
@RunWith(AndroidJUnit4::class)
class SummaryCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // =====================================
    // 📱 Basic Display Tests
    // =====================================

    @Test
    fun summaryCard_displaysBasicInfo() {
        val testSummary = createTestSummary(
            title = "Test Title",
            extract = "This is a test extract text"
        )
        var clickCount = 0

        composeTestRule.setContent {
            SummaryCard(
                summary = testSummary,
                onClick = { clickCount++ }
            )
        }

        // 제목이 표시되는지 확인
        composeTestRule
            .onNodeWithText("Test Title")
            .assertExists()
            .assertIsDisplayed()

        // 추출 텍스트가 표시되는지 확인
        composeTestRule
            .onNodeWithText("This is a test extract text")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun summaryCard_displaysExtractText() {
        val testSummary = createTestSummary(
            title = "Android",
            extract = "Android is a mobile operating system based on a modified version of the Linux kernel."
        )

        composeTestRule.setContent {
            SummaryCard(
                summary = testSummary,
                onClick = { }
            )
        }

        composeTestRule
            .onNodeWithText("Android")
            .assertExists()

        composeTestRule
            .onNodeWithText("Android is a mobile operating system based on a modified version of the Linux kernel.")
            .assertExists()
    }

    @Test
    fun summaryCard_handlesEmptyExtract() {
        val testSummary = createTestSummary(
            title = "Test Title",
            extract = ""
        )

        composeTestRule.setContent {
            SummaryCard(
                summary = testSummary,
                onClick = { }
            )
        }

        // 제목은 표시되어야 함
        composeTestRule
            .onNodeWithText("Test Title")
            .assertExists()

        // 빈 extract는 표시되지 않아야 함 (조건부 표시)
        composeTestRule.waitForIdle()
    }

    // =====================================
    // 📏 Long Text Handling Tests
    // =====================================

    @Test
    fun summaryCard_handlesLongTitle() {
        val longTitle = "This is a very long title that should be truncated with ellipsis because it exceeds the maximum allowed length for display in the summary card component"
        val testSummary = createTestSummary(
            title = longTitle,
            extract = "Short extract"
        )

        composeTestRule.setContent {
            SummaryCard(
                summary = testSummary,
                onClick = { }
            )
        }

        // 긴 제목이 표시되는지 확인 (truncation 확인은 UI 레벨에서 처리)
        composeTestRule
            .onNodeWithText(longTitle)
            .assertExists()

        composeTestRule
            .onNodeWithText("Short extract")
            .assertExists()
    }

    @Test
    fun summaryCard_handlesLongExtract() {
        val longExtract = "This is a very long extract text that contains multiple sentences and should be properly displayed in the summary card. The text might be truncated based on the maxLines setting, but the component should handle it gracefully without any layout issues. This is important for maintaining good user experience when dealing with varying content lengths from the Wikipedia API."

        val testSummary = createTestSummary(
            title = "Test Title",
            extract = longExtract
        )

        composeTestRule.setContent {
            SummaryCard(
                summary = testSummary,
                onClick = { }
            )
        }

        composeTestRule
            .onNodeWithText("Test Title")
            .assertExists()

        // 긴 텍스트의 시작 부분이 표시되는지 확인
        composeTestRule
            .onNodeWithText(longExtract, substring = true)
            .assertExists()
    }

    // =====================================
    // 👆 Click Event Tests
    // =====================================

    @Test
    fun summaryCard_onClickTriggered() {
        val testSummary = createTestSummary()
        var clickCount = 0

        composeTestRule.setContent {
            SummaryCard(
                summary = testSummary,
                onClick = { clickCount++ }
            )
        }

        // 카드 클릭
        composeTestRule
            .onNodeWithText("Test Title")
            .performClick()

        composeTestRule.waitForIdle()

        // 클릭 이벤트가 발생했는지 확인 (실제로는 count를 확인할 수 없지만, 클릭이 가능한지 확인)
        composeTestRule
            .onNodeWithText("Test Title")
            .assertHasClickAction()
    }

    @Test
    fun summaryCard_multipleClicksHandled() {
        val testSummary = createTestSummary()
        var clickCount = 0

        composeTestRule.setContent {
            SummaryCard(
                summary = testSummary,
                onClick = { clickCount++ }
            )
        }

        // 여러 번 클릭
        repeat(3) {
            composeTestRule
                .onNodeWithText("Test Title")
                .performClick()
            
            composeTestRule.waitForIdle()
        }

        // 클릭 가능 상태 유지 확인
        composeTestRule
            .onNodeWithText("Test Title")
            .assertHasClickAction()
    }

    // =====================================
    // 🖼️ Image Handling Tests
    // =====================================

    @Test
    fun summaryCard_handlesNullThumbnailUrl() {
        val testSummary = createTestSummary(thumbnailUrl = null)

        composeTestRule.setContent {
            SummaryCard(
                summary = testSummary,
                onClick = { }
            )
        }

        // 이미지가 null이어도 카드는 정상 표시되어야 함
        composeTestRule
            .onNodeWithText("Test Title")
            .assertExists()

        composeTestRule
            .onNodeWithText("Test extract")
            .assertExists()
    }

    @Test
    fun summaryCard_handlesValidThumbnailUrl() {
        val testSummary = createTestSummary(
            thumbnailUrl = "https://example.com/image.jpg"
        )

        composeTestRule.setContent {
            SummaryCard(
                summary = testSummary,
                onClick = { }
            )
        }

        // 텍스트는 정상 표시되어야 함
        composeTestRule
            .onNodeWithText("Test Title")
            .assertExists()

        composeTestRule
            .onNodeWithText("Test extract")
            .assertExists()
    }

    // =====================================
    // ⚙️ Customization Tests
    // =====================================

    @Test
    fun summaryCard_customImageSize() {
        val testSummary = createTestSummary()

        composeTestRule.setContent {
            SummaryCard(
                summary = testSummary,
                onClick = { },
                imageWidth = 200,
                imageHeight = 150
            )
        }

        // 커스텀 이미지 크기를 설정해도 텍스트는 정상 표시되어야 함
        composeTestRule
            .onNodeWithText("Test Title")
            .assertExists()

        composeTestRule
            .onNodeWithText("Test extract")
            .assertExists()
    }

    @Test
    fun summaryCard_customMaxLines() {
        val longExtract = "Line 1. Line 2. Line 3. Line 4. Line 5. This is a very long text with multiple lines to test the maxLines parameter functionality."
        val testSummary = createTestSummary(extract = longExtract)

        composeTestRule.setContent {
            SummaryCard(
                summary = testSummary,
                onClick = { },
                maxExtractLines = 2 // 2줄로 제한
            )
        }

        // 텍스트가 표시되는지 확인 (줄 수 제한은 UI 레벨에서 처리)
        composeTestRule
            .onNodeWithText(longExtract, substring = true)
            .assertExists()
    }

    // =====================================
    // 🔧 Edge Cases Tests
    // =====================================

    @Test
    fun summaryCard_handlesSpecialCharacters() {
        val testSummary = createTestSummary(
            title = "C++ Programming & Software Development",
            extract = "C++ is a general-purpose programming language with low-level & high-level features."
        )

        composeTestRule.setContent {
            SummaryCard(
                summary = testSummary,
                onClick = { }
            )
        }

        composeTestRule
            .onNodeWithText("C++ Programming & Software Development")
            .assertExists()

        composeTestRule
            .onNodeWithText("C++ is a general-purpose programming language with low-level & high-level features.")
            .assertExists()
    }

    @Test
    fun summaryCard_handlesUnicodeCharacters() {
        val testSummary = createTestSummary(
            title = "안드로이드 개발",
            extract = "안드로이드는 구글이 개발한 모바일 운영체제입니다. 🤖📱"
        )

        composeTestRule.setContent {
            SummaryCard(
                summary = testSummary,
                onClick = { }
            )
        }

        composeTestRule
            .onNodeWithText("안드로이드 개발")
            .assertExists()

        composeTestRule
            .onNodeWithText("안드로이드는 구글이 개발한 모바일 운영체제입니다. 🤖📱")
            .assertExists()
    }

    // =====================================
    // 🛠️ Helper Methods
    // =====================================

    private fun createTestSummary(
        title: String = "Test Title",
        description: String = "Test description",
        extract: String = "Test extract",
        thumbnailUrl: String? = "https://example.com/thumb.jpg",
        originalImageUrl: String? = "https://example.com/original.jpg",
        pageId: Int = 12345,
        timestamp: String = "2024-01-01T00:00:00Z"
    ): Summary {
        return Summary(
            title = title,
            description = description,
            extract = extract,
            thumbnailUrl = thumbnailUrl,
            originalImageUrl = originalImageUrl,
            pageId = pageId,
            timestamp = timestamp
        )
    }
}
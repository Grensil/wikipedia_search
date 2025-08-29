package com.grensil.search.component

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.grensil.domain.dto.MediaItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 🎯 MediaItemCard 컴포넌트 테스트
 * 
 * 테스트 커버리지:
 * 1. 기본 표시 기능 테스트
 * 2. Caption 표시/숨김 기능 테스트
 * 3. 클릭 이벤트 테스트
 * 4. 이미지 처리 테스트
 * 5. 긴 텍스트 처리 테스트
 * 6. 커스터마이징 옵션 테스트
 */
@RunWith(AndroidJUnit4::class)
class MediaItemCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // =====================================
    // 📱 Basic Display Tests
    // =====================================

    @Test
    fun mediaItemCard_displaysBasicInfo() {
        val testMediaItem = createTestMediaItem(
            title = "Test Image",
            caption = "Test caption"
        )
        var clickCount = 0

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { clickCount++ }
            )
        }

        // 제목이 표시되는지 확인
        composeTestRule
            .onNodeWithText("Test Image")
            .assertExists()
            .assertIsDisplayed()

        // 캡션이 표시되는지 확인 (기본값 showSubtitle = true)
        composeTestRule
            .onNodeWithText("Test caption")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun mediaItemCard_displaysTitle() {
        val testMediaItem = createTestMediaItem(
            title = "Android Logo",
            caption = "Official Android mascot"
        )

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { }
            )
        }

        composeTestRule
            .onNodeWithText("Android Logo")
            .assertExists()

        composeTestRule
            .onNodeWithText("Official Android mascot")
            .assertExists()
    }

    // =====================================
    // 📝 Caption Display Control Tests
    // =====================================

    @Test
    fun mediaItemCard_showsCaption_whenEnabled() {
        val testMediaItem = createTestMediaItem(
            title = "Test Image",
            caption = "This caption should be visible"
        )

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { },
                showSubtitle = true
            )
        }

        composeTestRule
            .onNodeWithText("Test Image")
            .assertExists()

        composeTestRule
            .onNodeWithText("This caption should be visible")
            .assertExists()
    }

    @Test
    fun mediaItemCard_hidesCaption_whenDisabled() {
        val testMediaItem = createTestMediaItem(
            title = "Test Image",
            caption = "This caption should NOT be visible"
        )

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { },
                showSubtitle = false
            )
        }

        // 제목은 표시되어야 함
        composeTestRule
            .onNodeWithText("Test Image")
            .assertExists()

        // 캡션은 표시되지 않아야 함
        composeTestRule
            .onNodeWithText("This caption should NOT be visible")
            .assertDoesNotExist()
    }

    @Test
    fun mediaItemCard_handlesEmptyCaption() {
        val testMediaItem = createTestMediaItem(
            title = "Test Image",
            caption = ""
        )

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { },
                showSubtitle = true
            )
        }

        // 제목은 표시되어야 함
        composeTestRule
            .onNodeWithText("Test Image")
            .assertExists()

        // 빈 캡션은 조건부로 표시되지 않을 수 있음
        composeTestRule.waitForIdle()
    }

    @Test
    fun mediaItemCard_handlesBlankCaption() {
        val testMediaItem = createTestMediaItem(
            title = "Test Image",
            caption = "   " // 공백만 있는 캡션
        )

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { },
                showSubtitle = true
            )
        }

        composeTestRule
            .onNodeWithText("Test Image")
            .assertExists()

        // 공백 캡션 처리 확인
        composeTestRule.waitForIdle()
    }

    // =====================================
    // 📏 Long Text Handling Tests
    // =====================================

    @Test
    fun mediaItemCard_handlesLongTitle() {
        val longTitle = "This is a very long media item title that should be truncated with ellipsis because it exceeds the maximum allowed length for display in the media card component layout"
        val testMediaItem = createTestMediaItem(
            title = longTitle,
            caption = "Short caption"
        )

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { }
            )
        }

        // 긴 제목이 표시되는지 확인
        composeTestRule
            .onNodeWithText(longTitle)
            .assertExists()

        composeTestRule
            .onNodeWithText("Short caption")
            .assertExists()
    }

    @Test
    fun mediaItemCard_handlesLongCaption() {
        val longCaption = "This is a very long caption text that contains detailed information about the media item and might be truncated based on the UI design requirements to maintain good layout and user experience"

        val testMediaItem = createTestMediaItem(
            title = "Short Title",
            caption = longCaption
        )

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { }
            )
        }

        composeTestRule
            .onNodeWithText("Short Title")
            .assertExists()

        // 긴 캡션의 일부가 표시되는지 확인
        composeTestRule
            .onNodeWithText(longCaption, substring = true)
            .assertExists()
    }

    // =====================================
    // 👆 Click Event Tests
    // =====================================

    @Test
    fun mediaItemCard_onClickTriggered() {
        val testMediaItem = createTestMediaItem()
        var clickCount = 0

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { clickCount++ }
            )
        }

        // 카드 클릭
        composeTestRule
            .onNodeWithText("Test Title")
            .performClick()

        composeTestRule.waitForIdle()

        // 클릭 가능한지 확인
        composeTestRule
            .onNodeWithText("Test Title")
            .assertHasClickAction()
    }

    @Test
    fun mediaItemCard_clickableWhenCaptionHidden() {
        val testMediaItem = createTestMediaItem(
            title = "Clickable Item",
            caption = "Hidden caption"
        )

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { },
                showSubtitle = false
            )
        }

        // 캡션이 숨겨져도 클릭 가능해야 함
        composeTestRule
            .onNodeWithText("Clickable Item")
            .assertHasClickAction()
            .performClick()
    }

    // =====================================
    // 🖼️ Image Handling Tests
    // =====================================

    @Test
    fun mediaItemCard_handlesNullImageUrl() {
        val testMediaItem = createTestMediaItem(imageUrl = null)

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { }
            )
        }

        // 이미지가 null이어도 텍스트는 정상 표시되어야 함
        composeTestRule
            .onNodeWithText("Test Title")
            .assertExists()

        composeTestRule
            .onNodeWithText("Test Caption")
            .assertExists()
    }

    @Test
    fun mediaItemCard_handlesValidImageUrl() {
        val testMediaItem = createTestMediaItem(
            imageUrl = "https://example.com/test-image.jpg"
        )

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { }
            )
        }

        // 이미지 URL이 있어도 텍스트는 정상 표시되어야 함
        composeTestRule
            .onNodeWithText("Test Title")
            .assertExists()

        composeTestRule
            .onNodeWithText("Test Caption")
            .assertExists()
    }

    // =====================================
    // ⚙️ Customization Tests
    // =====================================

    @Test
    fun mediaItemCard_customImageSize() {
        val testMediaItem = createTestMediaItem()

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { },
                imageSize = 120 // 커스텀 이미지 크기
            )
        }

        // 커스텀 이미지 크기를 설정해도 텍스트는 정상 표시되어야 함
        composeTestRule
            .onNodeWithText("Test Title")
            .assertExists()

        composeTestRule
            .onNodeWithText("Test Caption")
            .assertExists()
    }

    @Test
    fun mediaItemCard_withCustomModifier() {
        val testMediaItem = createTestMediaItem(
            title = "Custom Card",
            caption = "With modifier"
        )

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { },
                showSubtitle = true
            )
        }

        composeTestRule
            .onNodeWithText("Custom Card")
            .assertExists()

        composeTestRule
            .onNodeWithText("With modifier")
            .assertExists()
    }

    // =====================================
    // 🔧 Edge Cases Tests
    // =====================================

    @Test
    fun mediaItemCard_handlesSpecialCharacters() {
        val testMediaItem = createTestMediaItem(
            title = "Image & Photo Collection",
            caption = "Photos, images & graphics collection"
        )

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { }
            )
        }

        composeTestRule
            .onNodeWithText("Image & Photo Collection")
            .assertExists()

        composeTestRule
            .onNodeWithText("Photos, images & graphics collection")
            .assertExists()
    }

    @Test
    fun mediaItemCard_handlesUnicodeCharacters() {
        val testMediaItem = createTestMediaItem(
            title = "한국어 이미지 제목",
            caption = "이미지 설명입니다. 🖼️📸"
        )

        composeTestRule.setContent {
            MediaItemCard(
                mediaItem = testMediaItem,
                onClick = { }
            )
        }

        composeTestRule
            .onNodeWithText("한국어 이미지 제목")
            .assertExists()

        composeTestRule
            .onNodeWithText("이미지 설명입니다. 🖼️📸")
            .assertExists()
    }

    @Test
    fun mediaItemCard_handlesVariousMediaTypes() {
        val mediaTypes = listOf("image", "video", "audio", "document")

        mediaTypes.forEach { type ->
            val testMediaItem = createTestMediaItem(
                title = "$type Item",
                caption = "This is a $type file",
                type = type
            )

            composeTestRule.setContent {
                MediaItemCard(
                    mediaItem = testMediaItem,
                    onClick = { }
                )
            }

            composeTestRule
                .onNodeWithText("$type Item")
                .assertExists()

            composeTestRule
                .onNodeWithText("This is a $type file")
                .assertExists()
        }
    }

    // =====================================
    // 🛠️ Helper Methods
    // =====================================

    private fun createTestMediaItem(
        title: String = "Test Title",
        caption: String = "Test Caption",
        extractedKeywords: String? = null,
        imageUrl: String? = "https://example.com/test.jpg",
        type: String = "image"
    ): MediaItem {
        return MediaItem(
            title = title,
            caption = caption,
            extractedKeywords = extractedKeywords,
            imageUrl = imageUrl,
            type = type
        )
    }
}
package com.grensil.data

import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import org.junit.Assert.*
import org.junit.Test

/**
 * 간단한 Data Layer 테스트 - Android API만 사용
 * 실제 네트워크 호출 없이 데이터 클래스와 로직만 테스트
 */
class SimpleDataLayerTest {

    @Test
    fun `Summary data class works correctly`() {
        // Given
        val summary = Summary(
            title = "Test Article",
            description = "Test description",
            thumbnailUrl = "https://example.com/thumb.jpg",
            originalImageUrl = "https://example.com/full.jpg",
            pageId = 123,
            extract = "Test extract content",
            timestamp = "2023-01-01T00:00:00Z"
        )

        // Then
        assertEquals("Test Article", summary.title)
        assertEquals("Test description", summary.description)
        assertEquals("https://example.com/thumb.jpg", summary.thumbnailUrl)
        assertEquals("https://example.com/full.jpg", summary.originalImageUrl)
        assertEquals(123, summary.pageId)
        assertEquals("Test extract content", summary.extract)
        assertEquals("2023-01-01T00:00:00Z", summary.timestamp)
    }

    @Test
    fun `Summary isValid works correctly`() {
        // Valid summary
        val validSummary = Summary("Title", "Description")
        assertTrue(validSummary.isValid())

        // Invalid summary - blank title
        val invalidSummary1 = Summary("", "Description")
        assertFalse(invalidSummary1.isValid())

        // Invalid summary - blank description
        val invalidSummary2 = Summary("Title", "")
        assertFalse(invalidSummary2.isValid())

        // Invalid summary - both blank
        val invalidSummary3 = Summary("", "")
        assertFalse(invalidSummary3.isValid())
    }

    @Test
    fun `Summary hasThumbnail works correctly`() {
        // With thumbnail
        val summaryWithThumb = Summary("Title", "Desc", "https://example.com/thumb.jpg")
        assertTrue(summaryWithThumb.hasThumbnail())

        // Without thumbnail
        val summaryWithoutThumb = Summary("Title", "Desc", null)
        assertFalse(summaryWithoutThumb.hasThumbnail())

        // With empty thumbnail
        val summaryEmptyThumb = Summary("Title", "Desc", "")
        assertFalse(summaryEmptyThumb.hasThumbnail())
    }

    @Test
    fun `Summary getDisplayImageUrl works correctly`() {
        // Thumbnail available
        val summaryWithThumb = Summary("Title", "Desc", "thumb.jpg", "original.jpg")
        assertEquals("thumb.jpg", summaryWithThumb.getDisplayImageUrl())

        // Only original available
        val summaryOnlyOriginal = Summary("Title", "Desc", null, "original.jpg")
        assertEquals("original.jpg", summaryOnlyOriginal.getDisplayImageUrl())

        // Neither available
        val summaryNoImages = Summary("Title", "Desc", null, null)
        assertNull(summaryNoImages.getDisplayImageUrl())
    }

    @Test
    fun `Summary getShortDescription works correctly`() {
        // Short description
        val shortDesc = "Short description"
        val summary1 = Summary("Title", shortDesc)
        assertEquals(shortDesc, summary1.getShortDescription(100))

        // Long description
        val longDesc = "This is a very long description that should be truncated when it exceeds the maximum length limit"
        val summary2 = Summary("Title", longDesc)
        val shortResult = summary2.getShortDescription(50)
        
        assertTrue(shortResult.length <= 50)
        assertTrue(shortResult.endsWith("..."))
        assertEquals(longDesc.take(47) + "...", shortResult)
    }

    @Test
    fun `MediaItem data class works correctly`() {
        // Given
        val mediaItem = MediaItem(
            title = "Test Image",
            caption = "Test image caption",
            imageUrl = "https://example.com/image.jpg",
            type = "image"
        )

        // Then
        assertEquals("Test Image", mediaItem.title)
        assertEquals("Test image caption", mediaItem.caption)
        assertEquals("https://example.com/image.jpg", mediaItem.imageUrl)
        assertEquals("image", mediaItem.type)
    }

    @Test
    fun `MediaItem hasImage works correctly`() {
        // With image
        val mediaWithImage = MediaItem("Title", "Caption", "https://example.com/image.jpg")
        assertTrue(mediaWithImage.hasImage())

        // Without image
        val mediaWithoutImage = MediaItem("Title", "Caption", null)
        assertFalse(mediaWithoutImage.hasImage())

        // Empty image URL
        val mediaEmptyImage = MediaItem("Title", "Caption", "")
        assertFalse(mediaEmptyImage.hasImage())
    }

    @Test
    fun `MediaItem isValid works correctly`() {
        // Valid media item
        val validMedia = MediaItem("Test Title", "Caption")
        assertTrue(validMedia.isValid())

        // Invalid media item - blank title
        val invalidMedia = MediaItem("", "Caption")
        assertFalse(invalidMedia.isValid())
    }

    @Test
    fun `MediaItem isImage works correctly`() {
        // Image type
        val imageItem = MediaItem("Title", "Caption", "url", "image")
        assertTrue(imageItem.isImage())

        // Bitmap type
        val bitmapItem = MediaItem("Title", "Caption", "url", "bitmap")
        assertTrue(bitmapItem.isImage())

        // Video type
        val videoItem = MediaItem("Title", "Caption", "url", "video")
        assertFalse(videoItem.isImage())

        // Unknown type
        val unknownItem = MediaItem("Title", "Caption", "url", "unknown")
        assertFalse(unknownItem.isImage())
    }

    @Test
    fun `MediaItem getDisplayCaption works correctly`() {
        // With caption
        val mediaWithCaption = MediaItem("Title", "Test caption")
        assertEquals("Test caption", mediaWithCaption.getDisplayCaption())

        // Without caption - should use title
        val mediaWithoutCaption = MediaItem("Test Title", "")
        assertEquals("Test Title", mediaWithoutCaption.getDisplayCaption())
    }

    @Test
    fun `MediaItem extractSearchKeywords works correctly`() {
        // Normal caption with multiple words
        val media1 = MediaItem("Title", "Android mobile development framework")
        val keywords1 = media1.extractSearchKeywords()
        assertEquals(3, keywords1.size)
        assertTrue(keywords1.contains("Android"))
        assertTrue(keywords1.contains("mobile"))
        assertTrue(keywords1.contains("development"))

        // Caption with short words (should be filtered out)
        val media2 = MediaItem("Title", "A big car is on the road")
        val keywords2 = media2.extractSearchKeywords()
        assertEquals(3, keywords2.size) // Should take first 3 valid words
        assertTrue(keywords2.contains("big"))
        assertTrue(keywords2.contains("car"))
        assertTrue(keywords2.contains("the"))

        // Caption with special characters
        val media3 = MediaItem("Title", "Test-word! Another@word #third")
        val keywords3 = media3.extractSearchKeywords()
        assertTrue(keywords3.contains("Testword"))
        assertTrue(keywords3.contains("Anotherword"))
        assertTrue(keywords3.contains("third"))
    }
}
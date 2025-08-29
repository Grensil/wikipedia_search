package com.grensil.domain

import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import org.junit.Assert.*
import org.junit.Test

/**
 * 간단한 Domain Layer 테스트 - Android API만 사용
 * UseCase 로직과 DTO 검증에 집중
 */
class SimpleDomainTest {

    @Test
    fun `Summary isValid validation works correctly`() {
        // Valid summary
        assertTrue(Summary("Title", "Description").isValid())
        
        // Invalid summaries
        assertFalse(Summary("", "Description").isValid()) // Empty title
        assertFalse(Summary("Title", "").isValid()) // Empty description
        assertFalse(Summary("", "").isValid()) // Both empty
    }

    @Test
    fun `MediaItem validation works correctly`() {
        // Valid media items
        assertTrue(MediaItem("Title", "Caption").isValid())
        assertTrue(MediaItem("Title", "").isValid()) // Caption can be empty
        
        // Invalid media items
        assertFalse(MediaItem("", "Caption").isValid()) // Empty title
        assertFalse(MediaItem("", "").isValid()) // Both empty
    }

    @Test
    fun `MediaItem filtering logic works correctly`() {
        // Test data
        val validImageItem = MediaItem("Image Title", "Caption", null, "https://example.com/image.jpg", "image")
        val invalidItem = MediaItem("", "Caption") // Invalid title
        val itemWithoutImage = MediaItem("Title", "Caption", null, null, "text")
        val videoItem = MediaItem("Video Title", "Caption", null, "https://example.com/video.mp4", "video")
        
        val allItems = listOf(validImageItem, invalidItem, itemWithoutImage, videoItem)
        
        // Filter valid items
        val validItems = allItems.filter { it.isValid() }
        assertEquals(3, validItems.size) // Should exclude invalidItem
        
        // Filter items with images
        val itemsWithImages = validItems.filter { it.hasImage() }
        assertEquals(2, itemsWithImages.size) // Should exclude itemWithoutImage
        
        // Sort by image type first
        val sortedItems = itemsWithImages.sortedWith(
            compareBy<MediaItem> { !it.isImage() }.thenBy { it.title }
        )
        
        assertEquals(validImageItem, sortedItems[0]) // Image should come first
        assertEquals(videoItem, sortedItems[1])
    }

    @Test
    fun `search term normalization logic works correctly`() {
        // Test the normalization logic that should be in UseCase
        fun normalizeSearchTerm(searchTerm: String): String {
            return searchTerm
                .trim()
                .replace("\\s+".toRegex(), " ")
                .split(" ")
                .joinToString("_") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }
        }
        
        // Test cases
        assertEquals("Android", normalizeSearchTerm("android"))
        assertEquals("Android", normalizeSearchTerm("  android  "))
        assertEquals("Android_Development", normalizeSearchTerm("android development"))
        assertEquals("Android_Development", normalizeSearchTerm("android   development"))
        assertEquals("Android_Development_Tools", normalizeSearchTerm("ANDROID development TOOLS"))
    }

    @Test
    fun `search term validation logic works correctly`() {
        // Test validation logic that should be in UseCase
        fun validateSearchTerm(searchTerm: String) {
            require(searchTerm.isNotBlank()) { "Search term cannot be blank" }
            require(searchTerm.length >= 2) { "Search term must be at least 2 characters" }
        }
        
        // Valid search terms
        validateSearchTerm("ab") // Minimum valid length
        validateSearchTerm("android") // Normal case
        validateSearchTerm("Android Development") // With spaces
        
        // Invalid search terms should throw exceptions
        assertThrows(IllegalArgumentException::class.java) {
            validateSearchTerm("")
        }
        
        assertThrows(IllegalArgumentException::class.java) {
            validateSearchTerm("   ") // Whitespace only
        }
        
        assertThrows(IllegalArgumentException::class.java) {
            validateSearchTerm("a") // Too short
        }
    }

    @Test
    fun `MediaItem keyword extraction works correctly`() {
        val mediaItem = MediaItem("Title", "Android mobile development framework tutorial")
        val keywords = mediaItem.extractSearchKeywords()
        
        assertEquals(3, keywords.size) // Should extract exactly 3 keywords
        assertTrue(keywords.contains("Android"))
        assertTrue(keywords.contains("mobile"))
        assertTrue(keywords.contains("development"))
        // "tutorial" should not be included as we only take first 3
    }

    @Test
    fun `Summary utility methods work correctly`() {
        val summary = Summary(
            title = "Test Title",
            description = "This is a very long description that should be truncated when it exceeds the limit",
            thumbnailUrl = "https://example.com/thumb.jpg",
            originalImageUrl = "https://example.com/full.jpg"
        )
        
        // Test thumbnail detection
        assertTrue(summary.hasThumbnail())
        assertTrue(summary.hasOriginalImage())
        
        // Test image URL preference
        assertEquals("https://example.com/thumb.jpg", summary.getDisplayImageUrl())
        
        // Test description truncation
        val shortDesc = summary.getShortDescription(50)
        assertTrue(shortDesc.length <= 50)
        assertTrue(shortDesc.endsWith("..."))
    }

    @Test
    fun `MediaItem type detection works correctly`() {
        assertTrue(MediaItem("Title", "Caption", null, "url", "image").isImage())
        assertTrue(MediaItem("Title", "Caption", null, "url", "bitmap").isImage())
        assertFalse(MediaItem("Title", "Caption", null, "url", "video").isImage())
        assertFalse(MediaItem("Title", "Caption", null, "url", "unknown").isImage())
    }

    @Test
    fun `domain objects handle null values correctly`() {
        // Summary with null values
        val summary = Summary("Title", "Description", null, null)
        assertFalse(summary.hasThumbnail())
        assertFalse(summary.hasOriginalImage())
        assertNull(summary.getDisplayImageUrl())
        
        // MediaItem with null values
        val mediaItem = MediaItem("Title", "Caption", null, null)
        assertFalse(mediaItem.hasImage())
        assertEquals("Caption", mediaItem.getDisplayCaption())
    }

    @Test
    fun `edge cases are handled correctly`() {
        // Empty caption should use title
        val mediaItem = MediaItem("Test Title", "")
        assertEquals("Test Title", mediaItem.getDisplayCaption())
        
        // Very short description
        val summary = Summary("Title", "Short")
        assertEquals("Short", summary.getShortDescription(100))
        
        // Exactly at limit
        val summary2 = Summary("Title", "A".repeat(50))
        assertEquals(50, summary2.getShortDescription(50).length)
        assertFalse(summary2.getShortDescription(50).endsWith("..."))
    }
}
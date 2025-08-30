package com.grensil.domain

import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.domain.repository.WikipediaRepository
import com.grensil.domain.usecase.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Domain Module Unit Test
 * 
 * Tests all domain layer components:
 * - Domain objects (Summary, MediaItem)
 * - Use cases (GetSummary, GetDetailPageUrl, GetMediaList)
 * - Business logic validation
 * 
 * Naming Convention:
 * - Class: DomainTest
 * - Methods: test_[component]_[condition]_[expectedResult]
 */
class DomainTest {

    private lateinit var fakeRepository: UnifiedFakeRepository
    private lateinit var getSummaryUseCase: GetSummaryUseCase
    private lateinit var getDetailPageUrlUseCase: GetDetailPageUrlUseCase
    private lateinit var getMediaListUseCase: GetMediaListUseCase

    @Before
    fun setup() {
        fakeRepository = UnifiedFakeRepository()
        getSummaryUseCase = GetSummaryUseCaseImpl(fakeRepository)
        getDetailPageUrlUseCase = GetDetailPageUrlUseCaseImpl(fakeRepository)
        getMediaListUseCase = GetMediaListUseCaseImpl(fakeRepository)
    }

    // =================================
    // 📋 Summary 도메인 객체 테스트
    // =================================

    @Test
    fun test_summary_validation_with_valid_data_returns_true() {
        val validSummary = Summary("Title", "Description")
        assertTrue(validSummary.isValid())
    }

    @Test  
    fun test_summary_validation_with_empty_title_returns_false() {
        val invalidSummary = Summary("", "Description")
        assertFalse(invalidSummary.isValid())
    }

    @Test
    fun test_summary_validation_with_empty_description_returns_false() {
        val invalidSummary = Summary("Title", "")
        assertFalse(invalidSummary.isValid())
    }

    @Test
    fun test_summary_thumbnail_check_with_valid_url_returns_true() {
        val summary = Summary("T", "D", "url")
        assertTrue(summary.hasThumbnail())
    }

    @Test
    fun test_summary_thumbnail_check_with_null_url_returns_false() {
        val summary = Summary("T", "D", null)
        assertFalse(summary.hasThumbnail())
    }

    @Test
    fun test_summary_original_image_check_with_valid_url_returns_true() {
        val summary = Summary("T", "D", null, "url")
        assertTrue(summary.hasOriginalImage())
    }

    @Test
    fun test_summary_original_image_check_with_null_url_returns_false() {
        val summary = Summary("T", "D", null, null)
        assertFalse(summary.hasOriginalImage())
    }

    @Test
    fun test_summary_display_image_with_thumbnail_returns_thumbnail_url() {
        val summary = Summary("T", "D", "thumb", "original")
        assertEquals("thumb", summary.getDisplayImageUrl())
    }

    @Test
    fun test_summary_display_image_without_thumbnail_returns_original_url() {
        val summary = Summary("T", "D", null, "original")
        assertEquals("original", summary.getDisplayImageUrl())
    }

    @Test
    fun test_summary_display_image_without_images_returns_null() {
        val summary = Summary("T", "D", null, null)
        assertNull(summary.getDisplayImageUrl())
    }

    @Test
    fun test_summary_short_description_with_long_text_returns_truncated_text() {
        val longDesc = "A".repeat(200)
        val summary = Summary("T", longDesc)
        
        val shortDesc = summary.getShortDescription(100)
        assertTrue(shortDesc.length <= 100)
        assertTrue(shortDesc.endsWith("..."))
    }

    // =================================
    // MediaItem Domain Object Tests  
    // =================================

    @Test
    fun test_mediaitem_validation_with_valid_data_returns_true() {
        val mediaItem = MediaItem("Title", "Caption")
        assertTrue(mediaItem.isValid())
    }

    @Test
    fun test_mediaitem_validation_with_empty_title_returns_false() {
        val mediaItem = MediaItem("", "Caption")
        assertFalse(mediaItem.isValid())
    }

    @Test
    fun test_mediaitem_validation_with_blank_title_returns_false() {
        val mediaItem = MediaItem("   ", "Caption")
        assertFalse(mediaItem.isValid())
    }

    @Test
    fun test_mediaitem_image_check_with_valid_url_returns_true() {
        val mediaItem = MediaItem("T", "C", null, "url")
        assertTrue(mediaItem.hasImage())
    }

    @Test
    fun test_mediaitem_image_check_with_null_url_returns_false() {
        val mediaItem = MediaItem("T", "C", null, null)
        assertFalse(mediaItem.hasImage())
    }

    @Test
    fun test_mediaitem_image_check_with_empty_url_returns_false() {
        val mediaItem = MediaItem("T", "C", null, "")
        assertFalse(mediaItem.hasImage())
    }

    // =================================
    // GetSummaryUseCase Tests
    // =================================

    @Test
    fun test_get_summary_usecase_with_valid_search_term_returns_summary() = runBlocking {
        val searchTerm = "Android"
        val expectedSummary = Summary("Android", "Mobile OS", "thumb.jpg")
        fakeRepository.setSummary(searchTerm, expectedSummary)

        val result = getSummaryUseCase(searchTerm)

        assertEquals(expectedSummary, result)
        assertTrue(fakeRepository.wasMethodCalled("getSummary:$searchTerm"))
    }

    @Test
    fun test_get_summary_usecase_with_empty_search_term_throws_exception() = runBlocking {
        try {
            getSummaryUseCase("")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("blank"))
        }
    }

    @Test
    fun test_get_summary_usecase_with_repository_error_propagates_exception() = runBlocking {
        fakeRepository.setShouldThrowError(true, "Repository error")

        try {
            getSummaryUseCase("test")
            fail("Expected RuntimeException")
        } catch (e: RuntimeException) {
            assertEquals("Repository error", e.message)
        }
    }

    // =================================
    // GetDetailPageUrlUseCase Tests
    // =================================

    @Test
    fun test_get_detail_url_usecase_with_valid_search_term_returns_url() {
        val searchTerm = "Android"
        val expectedUrl = "https://en.wikipedia.org/wiki/Android"
        fakeRepository.setDetailPageUrl(searchTerm, expectedUrl)

        val result = getDetailPageUrlUseCase(searchTerm)

        assertEquals(expectedUrl, result)
        assertTrue(result.startsWith("https://en.wikipedia.org/wiki/"))
    }

    @Test
    fun test_get_detail_url_usecase_with_empty_search_term_throws_exception() {
        try {
            getDetailPageUrlUseCase("")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("blank"))
        }
    }

    @Test
    fun test_get_detail_url_usecase_with_special_characters_handles_correctly() {
        val testCases = mapOf(
            "C++" to "https://en.wikipedia.org/wiki/C%2B%2B",
            "Node.js" to "https://en.wikipedia.org/wiki/Node.js"
        )

        testCases.forEach { (searchTerm, expectedUrl) ->
            fakeRepository.setDetailPageUrl(searchTerm.replaceFirstChar { it.uppercase() }, expectedUrl)
            val result = getDetailPageUrlUseCase(searchTerm)
            assertEquals(expectedUrl, result)
        }
    }

    // =================================
    // GetMediaListUseCase Tests
    // =================================

    @Test
    fun test_get_media_list_usecase_with_valid_captions_extracts_keywords() = runBlocking {
        val searchTerm = "Android"
        val rawMediaItems = listOf(
            MediaItem("Test1", "Official Android mobile development logo", null, "url", "image"),
            MediaItem("Test2", "System architecture diagram", null, "url", "image")
        )
        fakeRepository.setMediaList(searchTerm, rawMediaItems)

        val result = getMediaListUseCase(searchTerm)

        assertEquals(2, result.size)
        assertNotNull(result[0].extractedKeywords)
        assertNotNull(result[1].extractedKeywords)
    }

    @Test
    fun test_get_media_list_usecase_with_no_images_filters_items() = runBlocking {
        val searchTerm = "test"
        val rawMediaItems = listOf(
            MediaItem("With Image", "Caption", null, "url", "image"),
            MediaItem("No Image", "Caption", null, null, "text")
        )
        fakeRepository.setMediaList(searchTerm, rawMediaItems)

        val result = getMediaListUseCase(searchTerm)

        assertEquals(1, result.size)
        assertEquals("With Image", result[0].title)
    }

    @Test
    fun test_get_media_list_usecase_with_long_caption_limits_keywords_to_three() = runBlocking {
        val searchTerm = "maxtest"
        val rawMediaItems = listOf(
            MediaItem("Test", "First Second Third Fourth Fifth", null, "url", "image")
        )
        fakeRepository.setMediaList(searchTerm, rawMediaItems)

        val result = getMediaListUseCase(searchTerm)

        val keywords = result[0].extractedKeywords!!.split(" ")
        assertTrue(keywords.size <= 3)
        assertEquals(3, keywords.size)
        assertEquals("First Second Third", result[0].extractedKeywords)
    }

    @Test
    fun test_get_media_list_usecase_with_unicode_characters_handles_correctly() = runBlocking {
        val searchTerm = "unicode"
        val rawMediaItems = listOf(
            MediaItem("Korean", "안드로이드 개발 튜토리얼", null, "url", "image"),
            MediaItem("Mixed", "Android 안드로이드 Development", null, "url", "image")
        )
        fakeRepository.setMediaList(searchTerm, rawMediaItems)

        val result = getMediaListUseCase(searchTerm)

        assertEquals(2, result.size)
        assertTrue(result[0].extractedKeywords!!.contains("안드로이드"))
        assertTrue(result[1].extractedKeywords!!.contains("Android") && 
                  result[1].extractedKeywords!!.contains("안드로이드"))
    }

    // =================================
    // 🛠️ 통합 Fake Repository
    // =================================

    private class UnifiedFakeRepository : WikipediaRepository {
        private val summaries = mutableMapOf<String, Summary>()
        private val mediaLists = mutableMapOf<String, List<MediaItem>>()
        private val detailUrls = mutableMapOf<String, String>()
        private val calledMethods = mutableListOf<String>()
        private var shouldThrowError = false
        private var errorMessage = "Test error"

        fun setSummary(searchTerm: String, summary: Summary) {
            summaries[searchTerm] = summary
        }

        fun setMediaList(searchTerm: String, mediaList: List<MediaItem>) {
            mediaLists[searchTerm] = mediaList
        }

        fun setDetailPageUrl(searchTerm: String, url: String) {
            detailUrls[searchTerm] = url
        }

        fun setShouldThrowError(shouldThrow: Boolean, message: String = "Test error") {
            shouldThrowError = shouldThrow
            errorMessage = message
        }

        fun wasMethodCalled(methodCall: String): Boolean {
            return calledMethods.contains(methodCall)
        }

        override suspend fun getSummary(searchTerm: String): Summary {
            calledMethods.add("getSummary:$searchTerm")
            if (shouldThrowError) throw RuntimeException(errorMessage)
            return summaries[searchTerm] ?: Summary("Default", "Default desc")
        }

        override suspend fun getMediaList(searchTerm: String): List<MediaItem> {
            calledMethods.add("getMediaList:$searchTerm")
            if (shouldThrowError) throw RuntimeException(errorMessage)
            return mediaLists[searchTerm] ?: emptyList()
        }

        override fun getDetailPageUrl(searchTerm: String): String {
            calledMethods.add("getDetailPageUrl:$searchTerm")
            if (shouldThrowError) throw RuntimeException(errorMessage)
            return detailUrls[searchTerm] ?: "https://en.wikipedia.org/wiki/$searchTerm"
        }
    }
}
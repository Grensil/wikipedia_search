package com.grensil.domain

import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.domain.repository.WikipediaRepository
import com.grensil.domain.usecase.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DomainTest {

    private lateinit var fakeRepository: FakeRepository
    private lateinit var getSummaryUseCase: GetSummaryUseCase
    private lateinit var getDetailPageUrlUseCase: GetDetailPageUrlUseCase
    private lateinit var getMediaListUseCase: GetMediaListUseCase

    @Before
    fun setup() {
        fakeRepository = FakeRepository()
        getSummaryUseCase = GetSummaryUseCaseImpl(fakeRepository)
        getDetailPageUrlUseCase = GetDetailPageUrlUseCaseImpl(fakeRepository)
        getMediaListUseCase = GetMediaListUseCaseImpl(fakeRepository)
    }

    // ==============================
    // Summary 도메인 객체 테스트
    // ==============================
    @Test
    fun summary_validation() {
        assertTrue(Summary("Title", "Desc").isValid())
        assertFalse(Summary("", "Desc").isValid())
        assertFalse(Summary("Title", "").isValid())
    }

    @Test
    fun summary_thumbnail_and_original_image() {
        val s = Summary("T", "D", "thumb", "original")
        assertTrue(s.hasThumbnail())
        assertTrue(s.hasOriginalImage())
        assertEquals("thumb", s.getDisplayImageUrl())
    }

    @Test
    fun summary_short_description_truncates() {
        val longDesc = "A".repeat(200)
        val s = Summary("T", longDesc)
        val shortDesc = s.getShortDescription(100)
        assertTrue(shortDesc.length <= 100)
        assertTrue(shortDesc.endsWith("..."))
    }

    // ==============================
    // MediaItem 도메인 객체 테스트
    // ==============================
    @Test
    fun mediaitem_validation_and_image() {
        val valid = MediaItem("T", "C", null, "url")
        assertTrue(valid.isValid())
        assertTrue(valid.hasImage())

        val invalidTitle = MediaItem("", "C")
        assertFalse(invalidTitle.isValid())

        val noImage = MediaItem("T", "C", null, null)
        assertFalse(noImage.hasImage())
    }

    // ==============================
    // UseCase 테스트
    // ==============================
    @Test
    fun getSummaryUseCase_returns_expected_summary() = runTest {
        val term = "Android"
        val expected = Summary(term, "Mobile OS")
        fakeRepository.setSummary(term, expected)

        val result = getSummaryUseCase(term)
        assertEquals(expected, result)
    }

    @Test
    fun getSummaryUseCase_blank_term_throws_exception() = runTest {
        try {
            getSummaryUseCase("") // suspend 함수 직접 호출 가능
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("blank"))
        }
    }

    @Test
    fun getDetailPageUrlUseCase_returns_expected_url() {
        val term = "Android"
        val expected = "https://en.wikipedia.org/wiki/Android"
        fakeRepository.setDetailPageUrl(term, expected)

        val result = getDetailPageUrlUseCase(term)
        assertEquals(expected, result)
    }

    @Test
    fun getMediaListUseCase_filters_invalid_items() = runTest {
        val term = "Test"
        val items = listOf(
            MediaItem("Valid", "Caption", null, "url"),
            MediaItem("NoImage", "Caption", null, null)
        )
        fakeRepository.setMediaList(term, items)

        val result = getMediaListUseCase(term)
        assertEquals(1, result.size)
        assertEquals("Valid", result.first().title)
    }

    @Test
    fun getMediaListUseCase_keyword_extraction_limits_to_three() = runTest {
        val term = "Keywords"
        val items = listOf(
            MediaItem("T", "One Two Three Four Five", null, "url")
        )
        fakeRepository.setMediaList(term, items)

        val result = getMediaListUseCase(term)
        val keywords = result.first().extractedKeywords!!.split(" ")
        assertEquals(3, keywords.size)
        assertEquals("One Two Three", result.first().extractedKeywords)
    }

    // ==============================
    // Fake Repository
    // ==============================
    private class FakeRepository : WikipediaRepository {
        private val summaries = mutableMapOf<String, Summary>()
        private val mediaLists = mutableMapOf<String, List<MediaItem>>()
        private val detailUrls = mutableMapOf<String, String>()

        fun setSummary(term: String, summary: Summary) {
            summaries[term] = summary
        }

        fun setMediaList(term: String, items: List<MediaItem>) {
            mediaLists[term] = items
        }

        fun setDetailPageUrl(term: String, url: String) {
            detailUrls[term] = url
        }

        override suspend fun getSummary(searchTerm: String): Summary =
            if (searchTerm.isBlank()) throw IllegalArgumentException("searchTerm cannot be blank")
            else summaries[searchTerm] ?: Summary("Default", "Default")

        override suspend fun getMediaList(searchTerm: String): List<MediaItem> =
            mediaLists[searchTerm] ?: emptyList()

        override fun getDetailPageUrl(searchTerm: String): String =
            detailUrls[searchTerm] ?: "https://en.wikipedia.org/wiki/$searchTerm"
    }
}

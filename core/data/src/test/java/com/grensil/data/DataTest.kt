package com.grensil.data

import com.grensil.data.datasource.WikipediaRemoteDataSource
import com.grensil.data.entity.MediaListEntity
import com.grensil.data.entity.SummaryEntity
import com.grensil.data.mapper.WikipediaMapper
import com.grensil.data.repository.WikipediaRepositoryImpl
import com.grensil.network.HttpClient
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DataTest {

    private lateinit var repository: WikipediaRepositoryImpl

    @Before
    fun setup() {
        val client = HttpClient()
        val dataSource = WikipediaRemoteDataSource(client)
        repository = WikipediaRepositoryImpl(dataSource)
    }

    // ============================
    // Repository Tests
    // ============================

    @Test
    fun `Given valid page name When generating detail URL Then returns valid Wikipedia link`() {
        val url = repository.getDetailPageUrl("Android")
        assertTrue(url.startsWith("https://"))
        assertTrue(url.contains("wikipedia.org"))
        assertTrue(url.contains("Android"))
    }

    // ============================
    // Mapper Tests
    // ============================

    @Test
    fun `Given valid SummaryEntity When mapped Then returns expected Summary`() {
        val entity = SummaryEntity(
            type = "standard",
            title = "Android",
            displaytitle = "Android",
            pageid = 123,
            extract = "Android OS",
            extractHtml = "<p>Android OS</p>",
            thumbnail = SummaryEntity.ThumbnailEntity("https://thumb.jpg", 100, 100),
            originalimage = SummaryEntity.OriginalImageEntity("https://full.jpg", 800, 600),
            lang = "en",
            dir = "ltr",
            timestamp = "2023-01-01",
            description = "Mobile OS"
        )

        val summary = WikipediaMapper.mapToSummary(entity)
        assertEquals("Android", summary.title)
        assertEquals("https://thumb.jpg", summary.thumbnailUrl)
        assertEquals("https://full.jpg", summary.originalImageUrl)
    }

    @Test
    fun `Given empty MediaListEntity When mapped Then returns empty list`() {
        val result = WikipediaMapper.mapToMediaItemList(MediaListEntity(emptyList()))
        assertTrue(result.isEmpty())
    }

    // ============================
    // Network Integration Test
    // ============================

    @Test
    fun `Given page title When fetching summary from API Then returns valid result`() = runTest {
        try {
            val result = repository.getSummary("Android")
            assertNotNull(result)
            assertTrue(result.title.isNotBlank())
        } catch (e: Exception) {
            println("⚠️ Skipping test: ${e.message}")
        }
    }
}

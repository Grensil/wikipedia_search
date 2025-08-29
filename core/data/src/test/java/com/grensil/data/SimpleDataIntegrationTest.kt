package com.grensil.data

import com.grensil.data.entity.MediaListEntity
import com.grensil.data.entity.SummaryEntity
import com.grensil.data.mapper.WikipediaMapper
import org.junit.Test
import org.junit.Assert.*

/**
 * Android API만 사용하는 Data Layer 통합 테스트
 * 매퍼와 엔티티 로직 검증에 집중
 */
class SimpleDataIntegrationTest {

    @Test
    fun `WikipediaMapper should correctly map SummaryEntity to Summary`() {
        // Given
        val entity = SummaryEntity(
            type = "standard",
            title = "Android",
            displaytitle = "Android (operating system)",
            pageid = 123,
            extract = "Android is a mobile operating system",
            extractHtml = "<p>Android is a mobile operating system</p>",
            thumbnail = SummaryEntity.ThumbnailEntity(
                source = "https://example.com/thumb.jpg",
                width = 100,
                height = 80
            ),
            originalimage = SummaryEntity.OriginalImageEntity(
                source = "https://example.com/full.jpg",
                width = 800,
                height = 600
            ),
            lang = "en",
            dir = "ltr",
            timestamp = "2023-01-01T00:00:00Z",
            description = "Mobile operating system"
        )

        // When
        val summary = WikipediaMapper.mapToSummary(entity)

        // Then
        assertEquals("Android", summary.title)
        assertEquals("Mobile operating system", summary.description)
        assertEquals("https://example.com/thumb.jpg", summary.thumbnailUrl)
        assertEquals("https://example.com/full.jpg", summary.originalImageUrl)
        assertEquals(123, summary.pageId)
        assertEquals("Android is a mobile operating system", summary.extract)
        assertEquals("2023-01-01T00:00:00Z", summary.timestamp)
    }

    @Test
    fun `WikipediaMapper should handle null values correctly in SummaryEntity`() {
        // Given
        val entity = SummaryEntity(
            type = null,
            title = "Test Title",
            displaytitle = null,
            pageid = null,
            extract = null,
            extractHtml = null,
            thumbnail = null,
            originalimage = null,
            lang = null,
            dir = null,
            timestamp = null,
            description = "Test Description"
        )

        // When
        val summary = WikipediaMapper.mapToSummary(entity)

        // Then
        assertEquals("Test Title", summary.title)
        assertEquals("Test Description", summary.description)
        assertNull(summary.thumbnailUrl)
        assertNull(summary.originalImageUrl)
        assertEquals(0, summary.pageId)
        assertEquals("", summary.extract)
        assertNull(summary.timestamp)
    }

    @Test
    fun `WikipediaMapper should correctly map MediaListEntity to MediaItem list`() {
        // Given
        val mediaListEntity = MediaListEntity(
            items = listOf(
                MediaListEntity.MediaItemEntity(
                    title = "Android Logo",
                    section_id = 1,
                    type = "image",
                    caption = MediaListEntity.MediaItemEntity.CaptionEntity(
                        text = "Official Android logo",
                        html = "<p>Official Android logo</p>"
                    ),
                    srcset = listOf(
                        MediaListEntity.MediaItemEntity.SrcSetEntity(
                            src = "https://example.com/android_logo.png",
                            scale = "1x"
                        )
                    )
                ),
                MediaListEntity.MediaItemEntity(
                    title = "Android Architecture",
                    section_id = 2,
                    type = "image",
                    caption = MediaListEntity.MediaItemEntity.CaptionEntity(
                        text = "Android system architecture diagram",
                        html = null
                    ),
                    srcset = emptyList()
                )
            )
        )

        // When
        val mediaItems = WikipediaMapper.mapToMediaItemList(mediaListEntity)

        // Then
        assertEquals(2, mediaItems.size)
        
        // First item
        assertEquals("Android Logo", mediaItems[0].title)
        assertEquals("Official Android logo", mediaItems[0].caption)
        assertEquals("image", mediaItems[0].type)
        assertEquals("https://example.com/android_logo.png", mediaItems[0].imageUrl)
        
        // Second item 
        assertEquals("Android Architecture", mediaItems[1].title)
        assertEquals("Android system architecture diagram", mediaItems[1].caption)
        assertEquals("image", mediaItems[1].type)
        assertNull(mediaItems[1].imageUrl) // No srcset
    }

    @Test
    fun `WikipediaMapper should not extract keywords - this is UseCase responsibility`() {
        // Given
        val mediaItemEntity = MediaListEntity.MediaItemEntity(
            title = "Test Title",
            section_id = 1,
            type = "image",
            caption = MediaListEntity.MediaItemEntity.CaptionEntity(
                text = "Android mobile development framework tutorial guide",
                html = null
            ),
            srcset = emptyList()
        )

        val mediaListEntity = MediaListEntity(items = listOf(mediaItemEntity))

        // When
        val mediaItems = WikipediaMapper.mapToMediaItemList(mediaListEntity)

        // Then
        assertEquals(1, mediaItems.size)
        val mediaItem = mediaItems[0]
        
        // Mapper는 순수한 데이터 변환만 수행
        assertEquals("Test Title", mediaItem.title)
        assertEquals("Android mobile development framework tutorial guide", mediaItem.caption)
        assertNull(mediaItem.extractedKeywords) // UseCase에서 처리하므로 null이어야 함
        assertEquals("image", mediaItem.type)
    }

    @Test
    fun `MediaListEntity should handle empty items list`() {
        // Given
        val emptyMediaListEntity = MediaListEntity(items = emptyList())

        // When
        val mediaItems = WikipediaMapper.mapToMediaItemList(emptyMediaListEntity)

        // Then
        assertTrue(mediaItems.isEmpty())
    }

    @Test
    fun `SummaryEntity with minimal data should map correctly`() {
        // Given - Only required fields
        val minimalEntity = SummaryEntity(
            type = null,
            title = "Minimal Title",
            displaytitle = null,
            pageid = null,
            extract = null,
            extractHtml = null,
            thumbnail = null,
            originalimage = null,
            lang = null,
            dir = null,
            timestamp = null,
            description = "Minimal Description"
        )

        // When
        val summary = WikipediaMapper.mapToSummary(minimalEntity)

        // Then
        assertNotNull(summary)
        assertEquals("Minimal Title", summary.title)
        assertEquals("Minimal Description", summary.description)
        assertTrue(summary.isValid()) // Should be valid with title and description
    }
}
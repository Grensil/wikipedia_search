package com.grensil.domain.usecase

import com.grensil.domain.dto.MediaItem
import com.grensil.domain.repository.WikipediaRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Android API만 사용하는 GetMediaListUseCase 테스트
 * 비즈니스 로직 (키워드 추출) 검증에 집중
 */
class GetMediaListUseCaseTest {

    private lateinit var fakeRepository: FakeWikipediaRepository
    private lateinit var useCase: GetMediaListUseCase

    @Before
    fun setup() {
        fakeRepository = FakeWikipediaRepository()
        useCase = GetMediaListUseCase(fakeRepository)
    }

    @Test
    fun `invoke should extract keywords from media item captions`() = runBlocking {
        // Given
        val searchTerm = "Android"
        val rawMediaItems = listOf(
            MediaItem(
                title = "Android Logo",
                caption = "Official Android mobile development logo",
                extractedKeywords = null, // 아직 추출되지 않음
                imageUrl = "https://example.com/logo.png",
                type = "image"
            ),
            MediaItem(
                title = "Android Architecture", 
                caption = "System architecture diagram",
                extractedKeywords = null,
                imageUrl = "https://example.com/arch.png",
                type = "image"
            )
        )
        
        fakeRepository.setMediaList(searchTerm, rawMediaItems)

        // When
        val result = useCase(searchTerm)

        // Then
        assertEquals(2, result.size)
        
        // 첫 번째 아이템 - 키워드가 추출되어야 함
        val firstItem = result[0]
        assertNotNull(firstItem.extractedKeywords)
        assertTrue("Expected keywords extracted from caption", 
            firstItem.extractedKeywords!!.contains("Official") || 
            firstItem.extractedKeywords!!.contains("Android") ||
            firstItem.extractedKeywords!!.contains("mobile"))
        
        // 두 번째 아이템 - 키워드가 추출되어야 함
        val secondItem = result[1]
        assertNotNull(secondItem.extractedKeywords)
        assertTrue("Expected keywords extracted from caption",
            secondItem.extractedKeywords!!.contains("System") ||
            secondItem.extractedKeywords!!.contains("architecture") ||
            secondItem.extractedKeywords!!.contains("diagram"))
    }

    @Test
    fun `invoke should filter out items without images`() = runBlocking {
        // Given
        val searchTerm = "test"
        val rawMediaItems = listOf(
            MediaItem("With Image", "Caption", null, "https://example.com/image.jpg", "image"),
            MediaItem("No Image", "Caption", null, null, "text") // 필터링되어야 함
        )
        
        fakeRepository.setMediaList(searchTerm, rawMediaItems)

        // When
        val result = useCase(searchTerm)

        // Then
        assertEquals(1, result.size) // 이미지 없는 항목은 필터링됨
        assertEquals("With Image", result[0].title)
    }

    @Test
    fun `invoke should handle empty caption correctly`() = runBlocking {
        // Given
        val searchTerm = "test"
        val rawMediaItems = listOf(
            MediaItem("Test", "", null, "https://example.com/image.jpg", "image") // 빈 캡션
        )
        
        fakeRepository.setMediaList(searchTerm, rawMediaItems)

        // When
        val result = useCase(searchTerm)

        // Then
        assertEquals(1, result.size)
        assertNull(result[0].extractedKeywords) // 빈 캡션은 null 키워드
    }

    @Test
    fun `keyword extraction should work correctly with various inputs`() = runBlocking {
        // Given
        val searchTerm = "test"
        val rawMediaItems = listOf(
            MediaItem("Test1", "Android mobile development", null, "url", "image"),
            MediaItem("Test2", "A B C", null, "url", "image"), // 짧은 단어들 - 필터링됨  
            MediaItem("Test3", "Special!@# Characters### Test", null, "url", "image")
        )
        
        fakeRepository.setMediaList(searchTerm, rawMediaItems)

        // When
        val result = useCase(searchTerm)

        // Then
        assertEquals(3, result.size)
        
        // 첫 번째: 정상적인 키워드 추출
        assertNotNull(result[0].extractedKeywords)
        
        // 두 번째: 짧은 단어들은 필터링되어 null
        assertNull(result[1].extractedKeywords)
        
        // 세 번째: 특수문자 제거 후 키워드 추출
        assertNotNull(result[2].extractedKeywords)
        assertTrue("Should contain 'Special' and 'Test'", 
            result[2].extractedKeywords!!.contains("Special") &&
            result[2].extractedKeywords!!.contains("Characters"))
    }

    /**
     * 테스트용 Fake Repository - Android API만 사용
     */
    private class FakeWikipediaRepository : WikipediaRepository {
        private val mediaLists = mutableMapOf<String, List<MediaItem>>()
        
        fun setMediaList(searchTerm: String, mediaList: List<MediaItem>) {
            mediaLists[searchTerm] = mediaList
        }
        
        override suspend fun getSummary(searchTerm: String): com.grensil.domain.dto.Summary {
            throw NotImplementedError("Not needed for this test")
        }
        
        override suspend fun getMediaList(searchTerm: String): List<MediaItem> {
            return mediaLists[searchTerm] ?: emptyList()
        }
        
        override fun getDetailPageUrl(searchTerm: String): String {
            return "https://example.com/detail/$searchTerm"
        }
    }
}
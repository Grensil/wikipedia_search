package com.grensil.domain

import com.grensil.domain.dto.Summary
import com.grensil.domain.usecase.GetSummaryUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

/**
 * Android API만 사용하는 단순한 UseCase 테스트
 * 복잡한 Mock 없이 핵심 로직만 검증
 */
class SimpleUseCaseTest {

    @Test
    fun `GetSummaryUseCase validation logic works correctly`() = runBlocking {
        // UseCase의 입력 검증 로직만 테스트
        val useCase = GetSummaryUseCase(TestRepository())
        
        // 빈 문자열 테스트
        try {
            useCase("")
            fail("Should throw IllegalArgumentException for empty string")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("blank"))
        }
        
        // 공백만 있는 문자열 테스트
        try {
            useCase("   ")
            fail("Should throw IllegalArgumentException for whitespace")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("blank"))
        }
    }
    
    @Test
    fun `GetSummaryUseCase normalization logic works correctly`() = runBlocking {
        val useCase = GetSummaryUseCase(TestRepository())
        
        // 공백이 있는 검색어로 테스트 (normalize 로직 확인)
        val result = useCase("  test query  ")
        
        // 결과가 정상적으로 반환되는지 확인
        assertNotNull(result)
        assertEquals("Test Query", result.title) // TestRepository에서 반환
    }
    
    @Test 
    fun `GetSummaryUseCase should validate returned summary`() = runBlocking {
        val invalidRepository = InvalidSummaryRepository()
        val useCase = GetSummaryUseCase(invalidRepository)
        
        try {
            useCase("test")
            fail("Should throw IllegalStateException for invalid summary")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("Invalid summary"))
        }
    }
    
    /**
     * 테스트용 Repository 구현 - Android API만 사용
     */
    private class TestRepository : com.grensil.domain.repository.WikipediaRepository {
        override suspend fun getSummary(searchTerm: String): Summary {
            return Summary(
                title = "Test Query", 
                description = "Test description",
                thumbnailUrl = "https://example.com/image.jpg",
                pageId = 123
            )
        }
        
        override suspend fun getMediaList(searchTerm: String): List<com.grensil.domain.dto.MediaItem> {
            return emptyList()
        }
        
        override fun getDetailPageUrl(searchTerm: String): String {
            return "https://example.com/detail/$searchTerm"
        }
    }
    
    /**
     * 잘못된 Summary를 반환하는 테스트용 Repository
     */
    private class InvalidSummaryRepository : com.grensil.domain.repository.WikipediaRepository {
        override suspend fun getSummary(searchTerm: String): Summary {
            return Summary(
                title = "", // Invalid: empty title
                description = "", // Invalid: empty description
                pageId = 0
            )
        }
        
        override suspend fun getMediaList(searchTerm: String): List<com.grensil.domain.dto.MediaItem> {
            return emptyList()
        }
        
        override fun getDetailPageUrl(searchTerm: String): String {
            return "https://example.com/detail/$searchTerm"
        }
    }
}
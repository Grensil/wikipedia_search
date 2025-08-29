package com.grensil.domain.usecase

import com.grensil.domain.dto.Summary
import com.grensil.domain.repository.WikipediaRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * GetSummaryUseCaseImpl Unit Test
 * 
 * 테스트 목적:
 * 1. GetSummaryUseCaseImpl의 비즈니스 로직 검증
 * 2. 입력 검증 및 정규화 기능 확인
 * 3. Repository 통합 동작 검증
 * 4. 에러 처리 로직 검증
 * 
 * 사용 기술: Android API + JUnit 4만 사용 (외부 라이브러리 없음)
 * 특징: 실제 네트워크 호출 없이 UseCase 로직만 테스트
 */
class GetSummaryUseCaseImplTest {

    private lateinit var useCase: GetSummaryUseCase
    private lateinit var fakeRepository: FakeWikipediaRepository

    @Before
    fun setup() {
        fakeRepository = FakeWikipediaRepository()
        useCase = GetSummaryUseCase(fakeRepository)
    }

    /**
     * ✅ 정상적인 Summary 조회 테스트
     * 
     * 테스트 시나리오:
     * 1. 유효한 검색어로 UseCase 실행
     * 2. Repository에서 받은 Summary를 그대로 반환
     * 3. 반환된 Summary가 유효한지 검증
     */
    @Test
    fun `invoke should return valid Summary for valid search term`() = runBlocking {
        // Given: 유효한 검색어와 예상 결과 준비
        val searchTerm = "Android"
        val expectedSummary = Summary(
            title = "Android",
            description = "Mobile operating system developed by Google",
            thumbnailUrl = "https://example.com/android_thumb.jpg",
            originalImageUrl = "https://example.com/android_full.jpg",
            pageId = 123,
            extract = "Android is a mobile operating system",
            timestamp = "2023-01-01T00:00:00Z"
        )
        
        fakeRepository.setSummary(searchTerm, expectedSummary)

        // When: UseCase 실행
        val result = useCase(searchTerm)

        // Then: 결과 검증
        assertEquals("제목이 올바르게 반환되어야 함", expectedSummary.title, result.title)
        assertEquals("설명이 올바르게 반환되어야 함", expectedSummary.description, result.description)
        assertEquals("썸네일 URL이 올바르게 반환되어야 함", expectedSummary.thumbnailUrl, result.thumbnailUrl)
        assertEquals("원본 이미지 URL이 올바르게 반환되어야 함", expectedSummary.originalImageUrl, result.originalImageUrl)
        assertEquals("페이지 ID가 올바르게 반환되어야 함", expectedSummary.pageId, result.pageId)
        assertEquals("추출 내용이 올바르게 반환되어야 함", expectedSummary.extract, result.extract)
        assertEquals("타임스탬프가 올바르게 반환되어야 함", expectedSummary.timestamp, result.timestamp)
        assertTrue("반환된 Summary는 유효해야 함", result.isValid())
    }

    /**
     * 🔍 검색어 정규화 테스트
     * 
     * 테스트 시나리오:
     * 1. 공백이 포함된 검색어 입력
     * 2. UseCase에서 검색어 정규화 후 Repository 호출
     * 3. 정규화된 검색어로 올바른 결과 반환
     */
    @Test
    fun `invoke should normalize search term correctly`() = runBlocking {
        // Given: 공백이 포함된 검색어
        val searchTermWithSpaces = "  Android Development  "
        val normalizedTerm = "Android_Development"
        val expectedSummary = Summary("Android Development", "Mobile app development", "thumb.jpg")
        
        // Repository에는 정규화된 검색어로 설정
        fakeRepository.setSummary(normalizedTerm, expectedSummary)

        // When: 공백이 포함된 검색어로 UseCase 실행
        val result = useCase(searchTermWithSpaces)

        // Then: 정규화 과정이 올바르게 동작했는지 확인
        assertEquals("정규화된 검색어로 결과가 반환되어야 함", expectedSummary.title, result.title)
        assertEquals("정규화된 검색어로 결과가 반환되어야 함", expectedSummary.description, result.description)
        assertTrue("Repository가 정규화된 검색어로 호출되었는지 확인", fakeRepository.wasCalledWith(normalizedTerm))
    }

    /**
     * ❌ 빈 검색어 검증 테스트
     * 
     * 테스트 시나리오:
     * 1. 빈 문자열로 UseCase 호출
     * 2. IllegalArgumentException 발생
     * 3. 적절한 에러 메시지 포함
     */
    @Test
    fun `invoke should throw exception for empty search term`() = runBlocking {
        // When & Then: 빈 검색어로 호출 시 예외 발생
        try {
            useCase("")
            fail("빈 검색어는 IllegalArgumentException을 발생시켜야 함")
        } catch (e: IllegalArgumentException) {
            assertTrue("에러 메시지에 'blank' 포함", e.message!!.contains("blank"))
        }
    }

    /**
     * ❌ 공백만 있는 검색어 검증 테스트
     * 
     * 테스트 시나리오:
     * 1. 공백만 있는 문자열로 UseCase 호출
     * 2. IllegalArgumentException 발생
     * 3. 적절한 에러 메시지 포함
     */
    @Test
    fun `invoke should throw exception for whitespace-only search term`() = runBlocking {
        // When & Then: 공백만 있는 검색어로 호출 시 예외 발생
        try {
            useCase("   ")
            fail("공백만 있는 검색어는 IllegalArgumentException을 발생시켜야 함")
        } catch (e: IllegalArgumentException) {
            assertTrue("에러 메시지에 'blank' 포함", e.message!!.contains("blank"))
        }
    }

    /**
     * ❌ 짧은 검색어 검증 테스트
     * 
     * 테스트 시나리오:
     * 1. 2자 미만의 검색어로 UseCase 호출
     * 2. IllegalArgumentException 발생
     * 3. 적절한 에러 메시지 포함
     */
    @Test
    fun `invoke should throw exception for too short search term`() = runBlocking {
        // When & Then: 너무 짧은 검색어로 호출 시 예외 발생
        try {
            useCase("a")
            fail("2자 미만 검색어는 IllegalArgumentException을 발생시켜야 함")
        } catch (e: IllegalArgumentException) {
            assertTrue("에러 메시지에 '2 characters' 포함", e.message!!.contains("2 characters"))
        }
    }

    /**
     * ✅ 최소 길이 검색어 테스트
     * 
     * 테스트 시나리오:
     * 1. 정확히 2자인 검색어로 UseCase 호출
     * 2. 정상적으로 처리되어야 함
     */
    @Test
    fun `invoke should accept minimum valid length search term`() = runBlocking {
        // Given: 최소 길이(2자) 검색어
        val minLengthTerm = "ab"
        val expectedSummary = Summary("AB", "Test description", "thumb.jpg")
        
        fakeRepository.setSummary("Ab", expectedSummary) // 정규화된 형태

        // When: UseCase 실행
        val result = useCase(minLengthTerm)

        // Then: 정상적으로 처리되어야 함
        assertEquals("최소 길이 검색어도 정상 처리되어야 함", expectedSummary.title, result.title)
    }

    /**
     * 🚫 무효한 Summary 반환 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. Repository에서 무효한 Summary 반환
     * 2. UseCase에서 검증 후 IllegalStateException 발생
     */
    @Test
    fun `invoke should throw exception for invalid Summary from repository`() = runBlocking {
        // Given: Repository가 무효한 Summary를 반환하도록 설정
        val searchTerm = "InvalidCase"
        val invalidSummary = Summary("", "", null) // 무효한 Summary (제목, 설명 모두 빈 문자열)
        
        fakeRepository.setSummary("Invalidcase", invalidSummary) // 정규화된 형태

        // When & Then: 무효한 Summary 반환 시 예외 발생
        try {
            useCase(searchTerm)
            fail("무효한 Summary는 IllegalStateException을 발생시켜야 함")
        } catch (e: IllegalStateException) {
            assertTrue("에러 메시지에 'Invalid summary' 포함", e.message!!.contains("Invalid summary"))
        }
    }

    /**
     * 🔄 Repository 예외 전파 테스트
     * 
     * 테스트 시나리오:
     * 1. Repository에서 예외 발생
     * 2. UseCase에서 예외를 그대로 전파
     */
    @Test
    fun `invoke should propagate repository exceptions`() = runBlocking {
        // Given: Repository가 예외를 던지도록 설정
        val searchTerm = "ErrorCase"
        fakeRepository.setShouldThrowError(true)

        // When & Then: Repository 예외가 전파되어야 함
        try {
            useCase(searchTerm)
            fail("Repository 예외가 전파되어야 함")
        } catch (e: RuntimeException) {
            assertEquals("Repository에서 발생한 예외가 전파되어야 함", "Test repository error", e.message)
        }
    }

    /**
     * 🔤 대소문자 및 특수문자 정규화 테스트
     * 
     * 테스트 시나리오:
     * 1. 대소문자가 섞인 검색어 입력
     * 2. 특수문자가 포함된 검색어 입력
     * 3. 올바르게 정규화되어 Repository 호출
     */
    @Test
    fun `invoke should normalize mixed case and special characters`() = runBlocking {
        // Given: 복잡한 검색어들
        val complexSearchTerm = "  ANDROID   development   TUTORIAL  "
        val expectedNormalized = "Android_Development_Tutorial"
        val expectedSummary = Summary("Android Development Tutorial", "Tutorial content", "thumb.jpg")
        
        fakeRepository.setSummary(expectedNormalized, expectedSummary)

        // When: 복잡한 검색어로 UseCase 실행
        val result = useCase(complexSearchTerm)

        // Then: 올바르게 정규화되어 처리되었는지 확인
        assertEquals("복잡한 검색어가 정규화되어 처리되어야 함", expectedSummary.title, result.title)
        assertTrue("Repository가 정규화된 검색어로 호출되었는지 확인", fakeRepository.wasCalledWith(expectedNormalized))
    }

    /**
     * 🧪 다양한 언어 검색어 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. 한글 검색어 입력
     * 2. 영어와 한글 혼합 검색어 입력
     * 3. 올바르게 처리되는지 확인
     */
    @Test
    fun `invoke should handle various language search terms`() = runBlocking {
        // Given: 다양한 언어 검색어
        val koreanTerm = "안드로이드"
        val mixedTerm = "Android 개발"
        
        val koreanSummary = Summary("안드로이드", "구글이 개발한 모바일 운영체제", "thumb_ko.jpg")
        val mixedSummary = Summary("Android 개발", "안드로이드 앱 개발", "thumb_mixed.jpg")
        
        fakeRepository.setSummary("안드로이드", koreanSummary)
        fakeRepository.setSummary("Android_개발", mixedSummary)

        // When & Then: 한글 검색어 테스트
        var result = useCase(koreanTerm)
        assertEquals("한글 검색어 처리", koreanSummary.title, result.title)
        assertTrue("한글 검색어로 Repository 호출", fakeRepository.wasCalledWith("안드로이드"))

        // When & Then: 혼합 언어 검색어 테스트
        result = useCase(mixedTerm)
        assertEquals("혼합 언어 검색어 처리", mixedSummary.title, result.title)
        assertTrue("혼합 언어 검색어로 Repository 호출", fakeRepository.wasCalledWith("Android_개발"))
    }

    // =================================
    // 테스트용 Fake Repository
    // =================================

    /**
     * 테스트용 Fake Repository - Android API만 사용
     */
    private class FakeWikipediaRepository : WikipediaRepository {
        private val summaries = mutableMapOf<String, Summary>()
        private val calledSearchTerms = mutableListOf<String>()
        private var shouldThrowError = false
        
        fun setSummary(searchTerm: String, summary: Summary) {
            summaries[searchTerm] = summary
        }
        
        fun setShouldThrowError(shouldThrow: Boolean) {
            shouldThrowError = shouldThrow
        }
        
        fun wasCalledWith(searchTerm: String): Boolean {
            return calledSearchTerms.contains(searchTerm)
        }
        
        override suspend fun getSummary(searchTerm: String): Summary {
            calledSearchTerms.add(searchTerm)
            
            if (shouldThrowError) {
                throw RuntimeException("Test repository error")
            }
            
            return summaries[searchTerm] ?: Summary("Default", "Default description")
        }
        
        override suspend fun getMediaList(searchTerm: String): List<com.grensil.domain.dto.MediaItem> {
            throw NotImplementedError("Not needed for GetSummaryUseCase test")
        }
        
        override fun getDetailPageUrl(searchTerm: String): String {
            throw NotImplementedError("Not needed for GetSummaryUseCase test")
        }
    }
}
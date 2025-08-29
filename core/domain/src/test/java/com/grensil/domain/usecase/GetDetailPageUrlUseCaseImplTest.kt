package com.grensil.domain.usecase

import com.grensil.domain.repository.WikipediaRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * GetDetailPageUrlUseCaseImpl Unit Test
 * 
 * 테스트 목적:
 * 1. GetDetailPageUrlUseCaseImpl의 URL 생성 로직 검증
 * 2. 입력 검증 및 정규화 기능 확인
 * 3. Repository 통합 동작 검증
 * 4. URL 형식 검증 로직 확인
 * 
 * 사용 기술: Android API + JUnit 4만 사용 (외부 라이브러리 없음)
 * 특징: 실제 네트워크 호출 없이 UseCase 로직만 테스트
 */
class GetDetailPageUrlUseCaseImplTest {

    private lateinit var useCase: GetDetailPageUrlUseCase
    private lateinit var fakeRepository: FakeWikipediaRepository

    @Before
    fun setup() {
        fakeRepository = FakeWikipediaRepository()
        useCase = GetDetailPageUrlUseCase(fakeRepository)
    }

    /**
     * ✅ 정상적인 URL 생성 테스트
     * 
     * 테스트 시나리오:
     * 1. 유효한 검색어로 UseCase 실행
     * 2. Repository에서 받은 URL을 그대로 반환
     * 3. 반환된 URL이 Wikipedia 형식인지 검증
     */
    @Test
    fun `invoke should return valid Wikipedia URL for valid search term`() {
        // Given: 유효한 검색어와 예상 URL 준비
        val searchTerm = "Android"
        val expectedUrl = "https://en.wikipedia.org/wiki/Android"
        
        fakeRepository.setDetailPageUrl(searchTerm, expectedUrl)

        // When: UseCase 실행
        val result = useCase(searchTerm)

        // Then: 결과 검증
        assertEquals("예상 URL이 반환되어야 함", expectedUrl, result)
        assertTrue("Wikipedia URL 형식이어야 함", result.startsWith("https://en.wikipedia.org/wiki/"))
        assertTrue("Repository가 올바른 검색어로 호출되었는지 확인", fakeRepository.wasCalledWith(searchTerm))
    }

    /**
     * 🔍 검색어 정규화 테스트
     * 
     * 테스트 시나리오:
     * 1. 공백이 포함된 검색어 입력
     * 2. UseCase에서 검색어 정규화 후 Repository 호출
     * 3. 정규화된 검색어로 올바른 URL 반환
     */
    @Test
    fun `invoke should normalize search term correctly`() {
        // Given: 공백이 포함된 검색어
        val searchTermWithSpaces = "  Android Development  "
        val normalizedTerm = "Android_Development"
        val expectedUrl = "https://en.wikipedia.org/wiki/Android_Development"
        
        // Repository에는 정규화된 검색어로 설정
        fakeRepository.setDetailPageUrl(normalizedTerm, expectedUrl)

        // When: 공백이 포함된 검색어로 UseCase 실행
        val result = useCase(searchTermWithSpaces)

        // Then: 정규화 과정이 올바르게 동작했는지 확인
        assertEquals("정규화된 검색어로 URL이 생성되어야 함", expectedUrl, result)
        assertTrue("Repository가 정규화된 검색어로 호출되었는지 확인", fakeRepository.wasCalledWith(normalizedTerm))
        assertFalse("Repository가 원본 검색어로 호출되지 않았는지 확인", fakeRepository.wasCalledWith(searchTermWithSpaces))
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
    fun `invoke should throw exception for empty search term`() {
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
    fun `invoke should throw exception for whitespace-only search term`() {
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
    fun `invoke should throw exception for too short search term`() {
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
    fun `invoke should accept minimum valid length search term`() {
        // Given: 최소 길이(2자) 검색어
        val minLengthTerm = "ab"
        val normalizedTerm = "Ab"
        val expectedUrl = "https://en.wikipedia.org/wiki/Ab"
        
        fakeRepository.setDetailPageUrl(normalizedTerm, expectedUrl)

        // When: UseCase 실행
        val result = useCase(minLengthTerm)

        // Then: 정상적으로 처리되어야 함
        assertEquals("최소 길이 검색어도 정상 처리되어야 함", expectedUrl, result)
        assertTrue("Repository가 정규화된 검색어로 호출되었는지 확인", fakeRepository.wasCalledWith(normalizedTerm))
    }

    /**
     * 🚫 잘못된 URL 반환 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. Repository에서 잘못된 URL 반환
     * 2. UseCase에서 검증 후 IllegalStateException 발생
     */
    @Test
    fun `invoke should throw exception for invalid URL from repository`() {
        // Given: Repository가 잘못된 URL을 반환하도록 설정
        val searchTerm = "InvalidCase"
        val invalidUrl = "" // 빈 URL
        
        fakeRepository.setDetailPageUrl("Invalidcase", invalidUrl) // 정규화된 형태

        // When & Then: 잘못된 URL 반환 시 예외 발생
        try {
            useCase(searchTerm)
            fail("잘못된 URL은 IllegalStateException을 발생시켜야 함")
        } catch (e: IllegalStateException) {
            assertTrue("에러 메시지에 'Invalid URL' 포함", e.message!!.contains("Invalid URL"))
        }
    }

    /**
     * 🌐 Wikipedia URL 형식 검증 테스트
     * 
     * 테스트 시나리오:
     * 1. Repository에서 Wikipedia가 아닌 URL 반환
     * 2. UseCase에서 검증 후 IllegalStateException 발생
     */
    @Test
    fun `invoke should throw exception for non-Wikipedia URL from repository`() {
        // Given: Repository가 Wikipedia가 아닌 URL을 반환하도록 설정
        val searchTerm = "NonWikiCase"
        val nonWikiUrl = "https://google.com/search?q=test"
        
        fakeRepository.setDetailPageUrl("Nonwikicase", nonWikiUrl) // 정규화된 형태

        // When & Then: Wikipedia가 아닌 URL 반환 시 예외 발생
        try {
            useCase(searchTerm)
            fail("Wikipedia가 아닌 URL은 IllegalStateException을 발생시켜야 함")
        } catch (e: IllegalStateException) {
            assertTrue("에러 메시지에 'Wikipedia URL' 포함", e.message!!.contains("Wikipedia URL"))
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
    fun `invoke should propagate repository exceptions`() {
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
    fun `invoke should normalize mixed case and special characters`() {
        // Given: 복잡한 검색어들
        val complexSearchTerm = "  C++   programming   LANGUAGE  "
        val expectedNormalized = "C++_Programming_Language"
        val expectedUrl = "https://en.wikipedia.org/wiki/C%2B%2B_Programming_Language"
        
        fakeRepository.setDetailPageUrl(expectedNormalized, expectedUrl)

        // When: 복잡한 검색어로 UseCase 실행
        val result = useCase(complexSearchTerm)

        // Then: 올바르게 정규화되어 처리되었는지 확인
        assertEquals("복잡한 검색어가 정규화되어 처리되어야 함", expectedUrl, result)
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
    fun `invoke should handle various language search terms`() {
        // Given: 다양한 언어 검색어
        val koreanTerm = "안드로이드"
        val mixedTerm = "Android 개발"
        
        val koreanUrl = "https://ko.wikipedia.org/wiki/안드로이드"
        val mixedUrl = "https://en.wikipedia.org/wiki/Android_개발"
        
        fakeRepository.setDetailPageUrl("안드로이드", koreanUrl)
        fakeRepository.setDetailPageUrl("Android_개발", mixedUrl)

        // When & Then: 한글 검색어 테스트
        var result = useCase(koreanTerm)
        assertEquals("한글 검색어 URL 생성", koreanUrl, result)
        assertTrue("한글 검색어로 Repository 호출", fakeRepository.wasCalledWith("안드로이드"))

        // When & Then: 혼합 언어 검색어 테스트
        result = useCase(mixedTerm)
        assertEquals("혼합 언어 검색어 URL 생성", mixedUrl, result)
        assertTrue("혼합 언어 검색어로 Repository 호출", fakeRepository.wasCalledWith("Android_개발"))
    }

    /**
     * 🔗 URL 인코딩 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. URL 인코딩이 필요한 특수문자가 포함된 검색어
     * 2. Repository에서 올바르게 인코딩된 URL 반환
     * 3. UseCase에서 URL 검증 통과
     */
    @Test
    fun `invoke should handle URL encoding correctly`() {
        // Given: URL 인코딩이 필요한 검색어들
        val testCases = mapOf(
            "C++" to "https://en.wikipedia.org/wiki/C%2B%2B",
            "C#" to "https://en.wikipedia.org/wiki/C%23",
            "Node.js" to "https://en.wikipedia.org/wiki/Node.js",
            "ASP.NET" to "https://en.wikipedia.org/wiki/ASP.NET"
        )

        testCases.forEach { (searchTerm, expectedUrl) ->
            // Given: Repository에 URL 설정
            val normalizedTerm = searchTerm.replaceFirstChar { it.uppercase() }
            fakeRepository.setDetailPageUrl(normalizedTerm, expectedUrl)

            // When: UseCase 실행
            val result = useCase(searchTerm)

            // Then: 올바른 URL 반환 확인
            assertEquals("$searchTerm: 올바른 URL이 반환되어야 함", expectedUrl, result)
            assertTrue("$searchTerm: Wikipedia URL 형식이어야 함", result.contains("wikipedia.org"))
            assertTrue("$searchTerm: Repository가 정규화된 검색어로 호출되었는지 확인", fakeRepository.wasCalledWith(normalizedTerm))
        }
    }

    /**
     * 📋 다양한 Wikipedia 도메인 지원 테스트
     * 
     * 테스트 시나리오:
     * 1. 영어 위키피디아 (en.wikipedia.org)
     * 2. 한글 위키피디아 (ko.wikipedia.org) 
     * 3. 모바일 위키피디아 (m.wikipedia.org)
     * 4. 모두 유효한 Wikipedia URL로 인정
     */
    @Test
    fun `invoke should accept various Wikipedia domains`() {
        // Given: 다양한 Wikipedia 도메인 URL들
        val validWikipediaUrls = listOf(
            "https://en.wikipedia.org/wiki/Android",
            "https://ko.wikipedia.org/wiki/안드로이드",
            "https://m.wikipedia.org/wiki/Android",
            "https://simple.wikipedia.org/wiki/Android"
        )

        validWikipediaUrls.forEachIndexed { index, url ->
            // Given: Repository 설정
            val searchTerm = "Test$index"
            fakeRepository.setDetailPageUrl(searchTerm, url)

            // When: UseCase 실행
            val result = useCase(searchTerm)

            // Then: 모든 Wikipedia 도메인이 허용되어야 함
            assertEquals("Wikipedia 도메인 $url 허용", url, result)
        }
    }

    /**
     * 🚫 비 Wikipedia URL 거부 테스트
     * 
     * 테스트 시나리오:
     * 1. 다양한 비 Wikipedia URL들
     * 2. 모두 IllegalStateException 발생해야 함
     */
    @Test
    fun `invoke should reject non-Wikipedia URLs`() {
        // Given: 다양한 비 Wikipedia URL들
        val invalidUrls = listOf(
            "https://google.com/search?q=android",
            "https://stackoverflow.com/questions/android",
            "https://github.com/android",
            "https://developer.android.com",
            "http://wikipedia.com/wiki/Android", // HTTP (보안되지 않음)
            "https://fake-wikipedia.com/wiki/Android" // 가짜 도메인
        )

        invalidUrls.forEachIndexed { index, invalidUrl ->
            // Given: Repository가 잘못된 URL을 반환하도록 설정
            val searchTerm = "Invalid$index"
            fakeRepository.setDetailPageUrl(searchTerm, invalidUrl)

            // When & Then: 모든 비 Wikipedia URL에서 예외 발생
            try {
                useCase(searchTerm)
                fail("비 Wikipedia URL $invalidUrl 은 예외를 발생시켜야 함")
            } catch (e: IllegalStateException) {
                assertTrue("에러 메시지에 'Wikipedia URL' 포함", e.message!!.contains("Wikipedia URL"))
            }
        }
    }

    // =================================
    // 테스트용 Fake Repository
    // =================================

    /**
     * 테스트용 Fake Repository - Android API만 사용
     */
    private class FakeWikipediaRepository : WikipediaRepository {
        private val detailUrls = mutableMapOf<String, String>()
        private val calledSearchTerms = mutableListOf<String>()
        private var shouldThrowError = false
        
        fun setDetailPageUrl(searchTerm: String, url: String) {
            detailUrls[searchTerm] = url
        }
        
        fun setShouldThrowError(shouldThrow: Boolean) {
            shouldThrowError = shouldThrow
        }
        
        fun wasCalledWith(searchTerm: String): Boolean {
            return calledSearchTerms.contains(searchTerm)
        }
        
        override suspend fun getSummary(searchTerm: String): com.grensil.domain.dto.Summary {
            throw NotImplementedError("Not needed for GetDetailPageUrlUseCase test")
        }
        
        override suspend fun getMediaList(searchTerm: String): List<com.grensil.domain.dto.MediaItem> {
            throw NotImplementedError("Not needed for GetDetailPageUrlUseCase test")
        }
        
        override fun getDetailPageUrl(searchTerm: String): String {
            calledSearchTerms.add(searchTerm)
            
            if (shouldThrowError) {
                throw RuntimeException("Test repository error")
            }
            
            return detailUrls[searchTerm] ?: "https://en.wikipedia.org/wiki/Default"
        }
    }
}
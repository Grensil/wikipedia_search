package com.grensil.domain.usecase

import com.grensil.domain.dto.MediaItem
import com.grensil.domain.repository.WikipediaRepository
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * GetMediaListUseCase 테스트 클래스
 * 
 * 테스트 목적:
 * 1. Wikipedia 미디어 검색 시 caption에서 키워드 추출이 올바르게 작동하는지 검증
 * 2. 이미지 없는 항목이 필터링되는지 검증
 * 3. 빈 캡션, 특수문자 등 엣지 케이스 처리 검증
 * 
 * 사용 기술: Android API + JUnit 4만 사용 (외부 라이브러리 없음)
 */
class GetMediaListUseCaseTest {

    // 테스트용 가짜 Repository (실제 네트워크 호출 없음)
    private lateinit var fakeRepository: FakeWikipediaRepository
    // 테스트 대상 UseCase
    private lateinit var useCase: GetMediaListUseCase

    /**
     * 각 테스트 실행 전 초기화
     * - FakeRepository 생성 (실제 API 호출 대신 미리 정의된 데이터 반환)
     * - GetMediaListUseCase 인스턴스 생성
     */
    @Before
    fun setup() {
        fakeRepository = FakeWikipediaRepository()
        useCase = GetMediaListUseCaseImpl(fakeRepository)
    }

    /**
     * 🎯 핵심 테스트: 캡션에서 키워드 추출 기능 검증
     * 
     * 테스트 시나리오:
     * 1. "Android" 검색 시 2개의 미디어 아이템 반환
     * 2. 각 아이템의 caption에서 최대 3개 키워드 추출
     * 3. 특수문자 제거 후 의미있는 단어만 추출되는지 확인
     * 
     * 예상 결과:
     * - "Official Android mobile development logo" → "Official", "Android", "mobile" 등
     * - "System architecture diagram" → "System", "architecture", "diagram"
     */
    @Test
    fun `invoke should extract keywords from media item captions`() = runBlocking {
        // Given: 테스트 데이터 준비 (캡션이 있는 2개 미디어 아이템)
        val searchTerm = "Android"
        val rawMediaItems = listOf(
            MediaItem(
                title = "Android Logo",
                caption = "Official Android mobile development logo", // 5개 단어 → 최대 3개 추출
                extractedKeywords = null, // UseCase 실행 전에는 null
                imageUrl = "https://example.com/logo.png",
                type = "image"
            ),
            MediaItem(
                title = "Android Architecture", 
                caption = "System architecture diagram", // 3개 단어 → 모두 추출
                extractedKeywords = null,
                imageUrl = "https://example.com/arch.png",
                type = "image"
            )
        )
        
        // 가짜 Repository에 테스트 데이터 설정
        fakeRepository.setMediaList(searchTerm, rawMediaItems)

        // When: UseCase 실행 (키워드 추출 로직 동작)
        val result = useCase(searchTerm)

        // Then: 결과 검증
        assertEquals("2개 아이템이 반환되어야 함", 2, result.size)
        
        // 첫 번째 아이템 검증: "Official Android mobile development logo"에서 키워드 추출
        val firstItem = result[0]
        assertNotNull("키워드가 추출되어야 함", firstItem.extractedKeywords)
        assertTrue("추출된 키워드 중 하나는 포함되어야 함 (Official/Android/mobile)", 
            firstItem.extractedKeywords!!.contains("Official") || 
            firstItem.extractedKeywords.contains("Android") ||
            firstItem.extractedKeywords.contains("mobile"))
        
        // 두 번째 아이템 검증: "System architecture diagram"에서 키워드 추출
        val secondItem = result[1]
        assertNotNull("키워드가 추출되어야 함", secondItem.extractedKeywords)
        assertTrue("추출된 키워드 중 하나는 포함되어야 함 (System/architecture/diagram)",
            secondItem.extractedKeywords!!.contains("System") ||
            secondItem.extractedKeywords!!.contains("architecture") ||
            secondItem.extractedKeywords!!.contains("diagram"))
    }

    /**
     * 🔍 필터링 테스트: 이미지 없는 항목 제외 기능 검증
     * 
     * 테스트 시나리오:
     * 1. 이미지 URL이 있는 항목과 없는 항목을 준비
     * 2. UseCase 실행 후 이미지 없는 항목이 제외되는지 확인
     * 
     * 비즈니스 규칙: 이미지가 없는 MediaItem은 결과에서 제외
     * (과제 요구사항: 미디어 리스트이므로 이미지는 필수)
     */
    @Test
    fun `invoke should filter out items without images`() = runBlocking {
        // Given: 이미지 있는 항목 1개 + 이미지 없는 항목 1개
        val searchTerm = "test"
        val rawMediaItems = listOf(
            MediaItem("With Image", "Caption", null, "https://example.com/image.jpg", "image"), // ✅ 유지
            MediaItem("No Image", "Caption", null, null, "text") // ❌ 필터링 대상 (imageUrl = null)
        )
        
        fakeRepository.setMediaList(searchTerm, rawMediaItems)

        // When: UseCase 실행 (필터링 로직 동작)
        val result = useCase(searchTerm)

        // Then: 이미지 있는 항목만 남아있어야 함
        assertEquals("이미지 없는 항목은 제외되어 1개만 반환", 1, result.size)
        assertEquals("이미지 있는 항목만 남아야 함", "With Image", result[0].title)
    }

    /**
     * 🔄 엣지 케이스 테스트: 빈 캡션 처리 검증
     * 
     * 테스트 시나리오:
     * 1. caption이 빈 문자열인 MediaItem 준비
     * 2. UseCase 실행 후 extractedKeywords가 null인지 확인
     * 
     * 예상 동작: 빈 캡션에서는 키워드를 추출할 수 없으므로 null 반환
     * (하지만 이미지가 있으면 항목 자체는 유지)
     */
    @Test
    fun `invoke should handle empty caption correctly`() = runBlocking {
        // Given: 캡션이 비어있는 미디어 아이템
        val searchTerm = "test"
        val rawMediaItems = listOf(
            MediaItem("Test", "", null, "https://example.com/image.jpg", "image") // caption = "" (빈 문자열)
        )
        
        fakeRepository.setMediaList(searchTerm, rawMediaItems)

        // When: UseCase 실행
        val result = useCase(searchTerm)

        // Then: 아이템은 유지되지만 키워드는 null
        assertEquals("이미지가 있으므로 아이템은 유지", 1, result.size)
        assertNull("빈 캡션에서는 키워드 추출 불가능", result[0].extractedKeywords)
    }

    /**
     * 🧪 복합 테스트: 다양한 캡션 형태에서 키워드 추출 검증
     * 
     * 테스트 시나리오:
     * 1. 정상적인 단어들 → 키워드 추출 성공
     * 2. 짧은 단어들(A, B, C) → 수정된 로직에서는 모든 단어 추출
     * 3. 특수문자 포함 → 특수문자 제거 후 키워드 추출
     * 
     * 주요 검증 포인트:
     * - 공백으로 단어 분리
     * - 특수문자 제거 ([^a-zA-Z0-9가-힣])
     * - 최대 3개 키워드 제한
     */
    @Test
    fun `keyword extraction should work correctly with various inputs`() = runBlocking {
        // Given: 다양한 형태의 캡션을 가진 3개 아이템
        val searchTerm = "test"
        val rawMediaItems = listOf(
            // 케이스 1: 일반적인 영어 단어들
            MediaItem("Test1", "Android mobile development", null, "url", "image"),
            // 케이스 2: 짧은 단어들 (수정된 로직: 길이 제한 없음)
            MediaItem("Test2", "A B C", null, "url", "image"), 
            // 케이스 3: 특수문자가 포함된 단어들
            MediaItem("Test3", "Special!@# Characters### Test", null, "url", "image")
        )
        
        fakeRepository.setMediaList(searchTerm, rawMediaItems)

        // When: UseCase 실행 (키워드 추출 로직 동작)
        val result = useCase(searchTerm)

        // Then: 각 케이스별 결과 검증
        assertEquals("3개 아이템 모두 반환되어야 함", 3, result.size)
        
        // 케이스 1: "Android mobile development" → 정상 추출
        assertNotNull("일반 단어는 추출되어야 함", result[0].extractedKeywords)
        
        // 케이스 2: "A B C" → 수정된 로직으로 모든 단어 추출됨 (길이 제한 없음)
        assertNotNull("짧은 단어도 추출되어야 함 (수정된 로직)", result[1].extractedKeywords)
        assertEquals("A B C 모두 추출되어야 함", "A B C", result[1].extractedKeywords)
        
        // 케이스 3: "Special!@# Characters### Test" → 특수문자 제거 후 추출
        assertNotNull("특수문자 제거 후 추출되어야 함", result[2].extractedKeywords)
        assertTrue("Special과 Characters가 포함되어야 함 (특수문자 제거됨)", 
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
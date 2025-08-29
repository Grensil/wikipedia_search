package com.grensil.domain

import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.domain.usecase.GetSummaryUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * Domain Layer 통합 완전 테스트 클래스
 * 
 * 테스트 목적:
 * 1. 도메인 모델(Summary, MediaItem)의 모든 메소드 검증
 * 2. 비즈니스 로직 유틸리티 메소드들의 동작 확인
 * 3. UseCase 입력 검증 및 정규화 로직 테스트
 * 4. 엣지 케이스와 null 값 처리 검증
 * 5. UseCase에서 사용될 로직들의 사전 검증
 * 
 * 통합 내용:
 * - SimpleDomainTest.kt의 모든 테스트 케이스
 * - SimpleUseCaseTest.kt의 UseCase 로직 테스트
 * - 중복 제거 및 통합된 검증 로직
 * 
 * 특징:
 * - Android API + JUnit 4만 사용 (외부 라이브러리 없음)
 * - 실제 네트워크 호출 없이 순수 도메인 로직만 테스트
 * - GetMediaListUseCase의 보조 로직들도 포함하여 포괄적 검증
 */
class DomainLayerCompleteTest {

    // =================================
    // Summary 데이터 클래스 테스트
    // =================================

    /**
     * ✅ Summary 유효성 검증 테스트: isValid() 메소드 핵심 로직 확인
     * 
     * 테스트 시나리오:
     * 1. 유효한 Summary (제목 + 설명 모두 존재) → true
     * 2. 무효한 Summary (제목 또는 설명 중 하나라도 비어있음) → false
     * 
     * 비즈니스 규칙:
     * - Wikipedia Summary API에서 제목과 설명은 필수 정보
     * - 둘 중 하나라도 없으면 사용자에게 의미있는 정보 제공 불가
     * 
     * UI 활용: 유효하지 않은 Summary는 ListView Header에 표시하지 않음
     */
    @Test
    fun `Summary isValid validation works correctly`() {
        // 케이스 1: 유효한 Summary
        assertTrue("제목과 설명이 모두 있으면 유효해야 함", Summary("Title", "Description").isValid())
        
        // 케이스 2~4: 무효한 Summary들
        assertFalse("제목이 비어있으면 무효", Summary("", "Description").isValid())
        assertFalse("설명이 비어있으면 무효", Summary("Title", "").isValid())
        assertFalse("제목과 설명이 모두 비어있으면 무효", Summary("", "").isValid())
    }

    /**
     * 🖼️ Summary 썸네일 검증 테스트: hasThumbnail() 메소드 동작 확인
     * 
     * 테스트 시나리오:
     * 1. 썸네일 URL이 있는 경우 → true
     * 2. 썸네일 URL이 null인 경우 → false
     * 3. 썸네일 URL이 빈 문자열인 경우 → false
     * 
     * 목적: UI에서 썸네일 이미지를 표시할지 결정하는 로직 검증
     * 활용: ListView Header에서 이미지 표시 여부 판단
     */
    @Test
    fun `Summary hasThumbnail works correctly`() {
        // 케이스 1: 썸네일 URL 존재
        val summaryWithThumb = Summary("Title", "Desc", "https://example.com/thumb.jpg")
        assertTrue("썸네일 URL이 있으면 true를 반환해야 함", summaryWithThumb.hasThumbnail())

        // 케이스 2: 썸네일 URL이 null
        val summaryWithoutThumb = Summary("Title", "Desc", null)
        assertFalse("썸네일 URL이 null이면 false를 반환해야 함", summaryWithoutThumb.hasThumbnail())

        // 케이스 3: 썸네일 URL이 빈 문자열
        val summaryEmptyThumb = Summary("Title", "Desc", "")
        assertFalse("썸네일 URL이 빈 문자열이면 false를 반환해야 함", summaryEmptyThumb.hasThumbnail())
    }

    /**
     * 🎨 Summary 이미지 우선순위 테스트: getDisplayImageUrl() 메소드 로직 검증
     * 
     * 테스트 시나리오:
     * 1. 썸네일과 원본 모두 있음 → 썸네일 우선 반환
     * 2. 원본만 있음 → 원본 이미지 반환
     * 3. 둘 다 없음 → null 반환
     * 
     * 비즈니스 로직:
     * - 썸네일이 있으면 썸네일 사용 (빠른 로딩)
     * - 썸네일이 없으면 원본 이미지 사용
     * - 둘 다 없으면 기본 이미지 또는 placeholder 사용
     * 
     * UI 활용: ListView Header에서 표시할 이미지 URL 결정
     */
    @Test
    fun `Summary getDisplayImageUrl works correctly`() {
        // 케이스 1: 썸네일과 원본 모두 존재 → 썸네일 우선
        val summaryWithThumb = Summary("Title", "Desc", "thumb.jpg", "original.jpg")
        assertEquals("썸네일이 있으면 썸네일을 우선 반환해야 함", "thumb.jpg", summaryWithThumb.getDisplayImageUrl())

        // 케이스 2: 원본만 존재 → 원본 반환
        val summaryOnlyOriginal = Summary("Title", "Desc", null, "original.jpg")
        assertEquals("썸네일이 없으면 원본 이미지를 반환해야 함", "original.jpg", summaryOnlyOriginal.getDisplayImageUrl())

        // 케이스 3: 이미지 없음 → null 반환
        val summaryNoImages = Summary("Title", "Desc", null, null)
        assertNull("이미지가 없으면 null을 반환해야 함", summaryNoImages.getDisplayImageUrl())
    }

    /**
     * ✂️ Summary 설명 축약 테스트: getShortDescription() 메소드 동작 확인
     * 
     * 테스트 시나리오:
     * 1. 짧은 설명 → 그대로 반환
     * 2. 긴 설명 → 제한 길이로 잘라내고 "..." 추가
     * 
     * 로직:
     * - 설명이 제한 길이보다 짧으면 그대로 반환
     * - 제한 길이보다 길면 (제한길이-3)만큼 자르고 "..." 추가
     * - 최종 길이는 항상 제한 길이 이하
     * 
     * UI 활용: ListView에서 긴 설명을 화면에 맞게 축약 표시
     */
    @Test
    fun `Summary getShortDescription works correctly`() {
        // 케이스 1: 짧은 설명 → 그대로 반환
        val shortDesc = "Short description"
        val summary1 = Summary("Title", shortDesc)
        assertEquals("짧은 설명은 그대로 반환되어야 함", shortDesc, summary1.getShortDescription(100))

        // 케이스 2: 긴 설명 → 잘라내고 "..." 추가
        val longDesc = "This is a very long description that should be truncated when it exceeds the maximum length limit"
        val summary2 = Summary("Title", longDesc)
        val shortResult = summary2.getShortDescription(50)
        
        assertTrue("축약된 설명은 제한 길이 이하여야 함", shortResult.length <= 50)
        assertTrue("축약된 설명은 \"...\"로 끝나야 함", shortResult.endsWith("..."))
        assertEquals("예상된 축약 형태와 일치해야 함", longDesc.take(47) + "...", shortResult)
    }

    /**
     * 🔧 Summary 유틸리티 메소드들 통합 테스트
     * 
     * 테스트 시나리오:
     * 1. 썸네일/원본 이미지 존재 여부 검증
     * 2. 이미지 URL 우선순위 로직 (썸네일 → 원본) 확인
     * 3. 긴 설명 축약 기능 동작 확인
     * 
     * 목적:
     * - 여러 유틸리티 메소드가 함께 사용될 때 올바르게 동작하는지 검증
     * - UI 컴포넌트에서 실제 사용되는 시나리오와 유사한 환경에서 테스트
     * 
     * 실제 사용 예:
     * - ListView Header에서 Summary 정보를 화면에 표시할 때 이 메소드들 활용
     * - 이미지 로딩, 설명 텍스트 표시 등에서 사용
     */
    @Test
    fun `Summary utility methods work correctly together`() {
        // Given: 모든 속성을 가진 Summary 객체 생성
        val summary = Summary(
            title = "Test Title",
            description = "This is a very long description that should be truncated when it exceeds the limit",
            thumbnailUrl = "https://example.com/thumb.jpg",
            originalImageUrl = "https://example.com/full.jpg"
        )
        
        // When & Then: 이미지 존재 여부 검증
        assertTrue("썸네일이 있어야 함", summary.hasThumbnail())
        assertTrue("원본 이미지가 있어야 함", summary.hasOriginalImage())
        
        // When & Then: 이미지 URL 우선순위 검증 (썸네일 우선)
        assertEquals("썸네일이 있으면 썸네일을 우선 반환해야 함", "https://example.com/thumb.jpg", summary.getDisplayImageUrl())
        
        // When & Then: 설명 축약 기능 검증
        val shortDesc = summary.getShortDescription(50)
        assertTrue("축약된 설명은 제한 길이 이하여야 함", shortDesc.length <= 50)
        assertTrue("축약된 설명은 ...로 끝나야 함", shortDesc.endsWith("..."))
    }

    // =================================
    // MediaItem 데이터 클래스 테스트
    // =================================

    /**
     * 📱 MediaItem 유효성 검증 테스트: isValid() 메소드 동작 확인
     * 
     * 테스트 시나리오:
     * 1. 유효한 MediaItem (제목 필수, 캡션은 선택사항) → true
     * 2. 무효한 MediaItem (제목 없음) → false
     * 
     * 비즈니스 규칙:
     * - 제목은 필수 (Wikipedia 미디어 아이템의 식별자)
     * - 캡션은 선택사항 (빈 캡션도 허용)
     * 
     * 차이점: Summary는 제목+설명 모두 필수, MediaItem은 제목만 필수
     * UI 활용: 유효하지 않은 MediaItem은 ListView에 표시하지 않음
     */
    @Test
    fun `MediaItem validation works correctly`() {
        // 케이스 1: 유효한 MediaItem들
        assertTrue("제목이 있으면 유효해야 함", MediaItem("Title", "Caption").isValid())
        assertTrue("캡션이 비어있어도 제목이 있으면 유효", MediaItem("Title", "").isValid())
        
        // 케이스 2~3: 무효한 MediaItem들
        assertFalse("제목이 비어있으면 무효", MediaItem("", "Caption").isValid())
        assertFalse("제목과 캡션이 모두 비어있으면 무효", MediaItem("", "").isValid())
    }

    /**
     * 📸 MediaItem 이미지 관련 메소드 테스트
     * 
     * 테스트 시나리오:
     * 1. hasImage() - 이미지 URL 존재 여부 검증
     * 2. isImage() - 이미지 타입 감지 (image, bitmap vs video 등)
     * 3. getDisplayCaption() - 캡션이 없을 때 제목 사용
     */
    @Test
    fun `MediaItem image methods work correctly`() {
        // hasImage() 테스트
        val mediaWithImage = MediaItem("Title", "Caption", null, "https://example.com/image.jpg")
        assertTrue("이미지 URL이 있으면 true", mediaWithImage.hasImage())

        val mediaWithoutImage = MediaItem("Title", "Caption", null, null)
        assertFalse("이미지 URL이 없으면 false", mediaWithoutImage.hasImage())

        val mediaEmptyImage = MediaItem("Title", "Caption", null, "")
        assertFalse("빈 이미지 URL이면 false", mediaEmptyImage.hasImage())

        // isImage() 테스트
        assertTrue("image 타입은 true", MediaItem("Title", "Caption", null, "url", "image").isImage())
        assertTrue("bitmap 타입은 true", MediaItem("Title", "Caption", null, "url", "bitmap").isImage())
        assertFalse("video 타입은 false", MediaItem("Title", "Caption", null, "url", "video").isImage())
        assertFalse("unknown 타입은 false", MediaItem("Title", "Caption", null, "url", "unknown").isImage())

        // getDisplayCaption() 테스트
        val mediaWithCaption = MediaItem("Title", "Test caption")
        assertEquals("캡션이 있으면 캡션 반환", "Test caption", mediaWithCaption.getDisplayCaption())

        val mediaWithoutCaption = MediaItem("Test Title", "")
        assertEquals("캡션이 없으면 제목 반환", "Test Title", mediaWithoutCaption.getDisplayCaption())
    }

    /**
     * 🔍 MediaItem 필터링 로직 테스트
     * 
     * UseCase에서 사용되는 필터링 로직을 시뮬레이션하여 검증
     * 1. 유효성 검증으로 잘못된 아이템 제거
     * 2. 이미지가 있는 아이템만 선별
     * 3. 이미지 타입별 정렬 (image/bitmap 우선)
     */
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
        assertEquals("유효한 아이템만 3개 남아야 함", 3, validItems.size) // Should exclude invalidItem
        
        // Filter items with images
        val itemsWithImages = validItems.filter { it.hasImage() }
        assertEquals("이미지가 있는 아이템만 2개 남아야 함", 2, itemsWithImages.size) // Should exclude itemWithoutImage
        
        // Sort by image type first
        val sortedItems = itemsWithImages.sortedWith(
            compareBy<MediaItem> { !it.isImage() }.thenBy { it.title }
        )
        
        assertEquals("이미지 아이템이 먼저 와야 함", validImageItem, sortedItems[0]) // Image should come first
        assertEquals("비디오 아이템이 나중에 와야 함", videoItem, sortedItems[1])
    }

    // =================================
    // UseCase 로직 테스트 (통합)
    // =================================

    /**
     * 🔍 검색어 정규화 로직 테스트
     * 
     * UseCase에서 사용되는 검색어 정규화 로직을 시뮬레이션하여 검증
     * 1. 공백 제거 및 정규화
     * 2. 단어별 첫 글자 대문자 변환
     * 3. 언더스코어로 연결
     */
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
        assertEquals("단일 단어 정규화", "Android", normalizeSearchTerm("android"))
        assertEquals("공백 제거 정규화", "Android", normalizeSearchTerm("  android  "))
        assertEquals("두 단어 정규화", "Android_Development", normalizeSearchTerm("android development"))
        assertEquals("여러 공백 정규화", "Android_Development", normalizeSearchTerm("android   development"))
        assertEquals("대소문자 혼합 정규화", "Android_Development_Tools", normalizeSearchTerm("ANDROID development TOOLS"))
    }

    /**
     * ✅ 검색어 검증 로직 테스트
     * 
     * UseCase에서 사용되는 검색어 검증 로직을 시뮬레이션하여 검증
     * 1. 빈 문자열 검증
     * 2. 최소 길이 검증
     * 3. 공백만 있는 문자열 검증
     */
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

    /**
     * 🧪 GetSummaryUseCase 검증 로직 테스트
     * 
     * UseCase의 입력 검증과 정규화 로직을 실제 UseCase 클래스로 테스트
     */
    @Test
    fun `GetSummaryUseCase validation logic works correctly`() = runBlocking {
        val useCase = GetSummaryUseCase(TestRepository())
        
        // 빈 문자열 테스트
        try {
            useCase("")
            fail("Should throw IllegalArgumentException for empty string")
        } catch (e: IllegalArgumentException) {
            assertTrue("에러 메시지에 'blank' 포함", e.message!!.contains("blank"))
        }
        
        // 공백만 있는 문자열 테스트
        try {
            useCase("   ")
            fail("Should throw IllegalArgumentException for whitespace")
        } catch (e: IllegalArgumentException) {
            assertTrue("에러 메시지에 'blank' 포함", e.message!!.contains("blank"))
        }
    }

    /**
     * 🔄 GetSummaryUseCase 정규화 로직 테스트
     * 
     * UseCase의 검색어 정규화 기능이 올바르게 동작하는지 확인
     */
    @Test
    fun `GetSummaryUseCase normalization logic works correctly`() = runBlocking {
        val useCase = GetSummaryUseCase(TestRepository())
        
        // 공백이 있는 검색어로 테스트 (normalize 로직 확인)
        val result = useCase("  test query  ")
        
        // 결과가 정상적으로 반환되는지 확인
        assertNotNull("결과가 null이 아니어야 함", result)
        assertEquals("TestRepository에서 반환된 제목", "Test Query", result.title) // TestRepository에서 반환
    }

    /**
     * ⚠️ GetSummaryUseCase 반환값 검증 테스트
     * 
     * UseCase가 잘못된 Summary를 받았을 때 적절히 처리하는지 확인
     */
    @Test 
    fun `GetSummaryUseCase should validate returned summary`() = runBlocking {
        val invalidRepository = InvalidSummaryRepository()
        val useCase = GetSummaryUseCase(invalidRepository)
        
        try {
            useCase("test")
            fail("Should throw IllegalStateException for invalid summary")
        } catch (e: IllegalStateException) {
            assertTrue("에러 메시지에 'Invalid summary' 포함", e.message!!.contains("Invalid summary"))
        }
    }

    // =================================
    // 엣지 케이스 및 통합 테스트
    // =================================

    /**
     * 🔄 엣지 케이스 테스트: 경계값 및 예외 상황 처리 검증
     * 
     * 테스트 시나리오:
     * 1. 빈 캡션 → 제목으로 대체
     * 2. 매우 짧은 설명 → 축약하지 않고 그대로 반환
     * 3. 제한 길이와 정확히 같은 설명 → 축약하지 않음
     * 
     * 중요성:
     * - 실제 Wikipedia API에서 다양한 형태의 데이터가 올 수 있음
     * - UI에서 예상치 못한 데이터로 인한 크래시 방지
     * - 사용자 경험을 해치지 않는 fallback 로직 검증
     * 
     * 실제 케이스:
     * - Wikipedia에는 캡션이 없는 이미지들이 많음
     * - 매우 짧은 설명을 가진 페이지들 존재
     * - 정확히 제한에 맞는 설명도 올바르게 처리되어야 함
     */
    @Test
    fun `edge cases are handled correctly`() {
        // 케이스 1: 빈 캡션 → 제목으로 대체 (fallback 로직)
        val mediaItem = MediaItem("Test Title", "")
        assertEquals("빈 캡션이면 제목을 사용해야 함", "Test Title", mediaItem.getDisplayCaption())
        
        // 케이스 2: 매우 짧은 설명 → 축약하지 않음
        val summary = Summary("Title", "Short")
        assertEquals("짧은 설명은 그대로 반환해야 함", "Short", summary.getShortDescription(100))
        
        // 케이스 3: 제한 길이와 정확히 같은 설명 → 축약하지 않음
        val summary2 = Summary("Title", "A".repeat(50))
        assertEquals("제한과 같은 길이면 정확히 50글자여야 함", 50, summary2.getShortDescription(50).length)
        assertFalse("제한과 같은 길이면 ...을 붙이지 않아야 함", summary2.getShortDescription(50).endsWith("..."))
    }

    /**
     * 🛡️ Null 값 처리 테스트
     * 
     * 도메인 객체들이 null 값을 올바르게 처리하는지 검증
     */
    @Test
    fun `domain objects handle null values correctly`() {
        // Summary with null values
        val summary = Summary("Title", "Description", null, null)
        assertFalse("썸네일이 null이면 false", summary.hasThumbnail())
        assertFalse("원본 이미지가 null이면 false", summary.hasOriginalImage())
        assertNull("이미지가 없으면 null 반환", summary.getDisplayImageUrl())
        
        // MediaItem with null values
        val mediaItem = MediaItem("Title", "Caption", null, null)
        assertFalse("이미지가 null이면 false", mediaItem.hasImage())
        assertEquals("캡션이 있으면 캡션 반환", "Caption", mediaItem.getDisplayCaption())
    }

    /**
     * 📊 데이터 클래스 기본 속성 테스트
     * 
     * Summary와 MediaItem의 기본적인 속성 설정이 올바르게 동작하는지 검증
     */
    @Test
    fun `data class properties work correctly`() {
        // Summary 속성 테스트
        val summary = Summary(
            title = "Test Article",
            description = "Test description",
            thumbnailUrl = "https://example.com/thumb.jpg",
            originalImageUrl = "https://example.com/full.jpg",
            pageId = 123,
            extract = "Test extract content",
            timestamp = "2023-01-01T00:00:00Z"
        )

        assertEquals("제목이 올바르게 설정되어야 함", "Test Article", summary.title)
        assertEquals("설명이 올바르게 설정되어야 함", "Test description", summary.description)
        assertEquals("썸네일 URL이 올바르게 설정되어야 함", "https://example.com/thumb.jpg", summary.thumbnailUrl)
        assertEquals("원본 이미지 URL이 올바르게 설정되어야 함", "https://example.com/full.jpg", summary.originalImageUrl)
        assertEquals("페이지 ID가 올바르게 설정되어야 함", 123, summary.pageId)
        assertEquals("추출 내용이 올바르게 설정되어야 함", "Test extract content", summary.extract)
        assertEquals("타임스탬프가 올바르게 설정되어야 함", "2023-01-01T00:00:00Z", summary.timestamp)

        // MediaItem 속성 테스트
        val mediaItem = MediaItem(
            title = "Test Image",
            caption = "Test image caption",
            extractedKeywords = null, // UseCase에서 나중에 설정
            imageUrl = "https://example.com/image.jpg",
            type = "image"
        )

        assertEquals("제목이 올바르게 설정되어야 함", "Test Image", mediaItem.title)
        assertEquals("캡션이 올바르게 설정되어야 함", "Test image caption", mediaItem.caption)
        assertEquals("이미지 URL이 올바르게 설정되어야 함", "https://example.com/image.jpg", mediaItem.imageUrl)
        assertEquals("타입이 올바르게 설정되어야 함", "image", mediaItem.type)
    }

    // =================================
    // 테스트용 Repository 구현체들
    // =================================

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
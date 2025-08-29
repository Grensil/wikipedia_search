package com.grensil.domain

import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import org.junit.Assert.*
import org.junit.Test

/**
 * Domain Layer 통합 테스트 클래스
 * 
 * 테스트 목적:
 * 1. 도메인 모델(Summary, MediaItem)의 모든 메소드 검증
 * 2. 비즈니스 로직 유틸리티 메소드들의 동작 확인
 * 3. 엣지 케이스와 null 값 처리 검증
 * 4. UseCase에서 사용될 로직들의 사전 검증
 * 
 * 특징:
 * - Android API + JUnit 4만 사용 (외부 라이브러리 없음)
 * - 실제 네트워크 호출 없이 순수 도메인 로직만 테스트
 * - GetMediaListUseCase의 보조 로직들도 포함하여 포괄적 검증
 */
class SimpleDomainTest {

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
        assertEquals(3, validItems.size) // Should exclude invalidItem
        
        // Filter items with images
        val itemsWithImages = validItems.filter { it.hasImage() }
        assertEquals(2, itemsWithImages.size) // Should exclude itemWithoutImage
        
        // Sort by image type first
        val sortedItems = itemsWithImages.sortedWith(
            compareBy<MediaItem> { !it.isImage() }.thenBy { it.title }
        )
        
        assertEquals(validImageItem, sortedItems[0]) // Image should come first
        assertEquals(videoItem, sortedItems[1])
    }

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
        assertEquals("Android", normalizeSearchTerm("android"))
        assertEquals("Android", normalizeSearchTerm("  android  "))
        assertEquals("Android_Development", normalizeSearchTerm("android development"))
        assertEquals("Android_Development", normalizeSearchTerm("android   development"))
        assertEquals("Android_Development_Tools", normalizeSearchTerm("ANDROID development TOOLS"))
    }

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
     * 🗑️ 삭제된 메소드 테스트
     * 
     * 참고: MediaItem.extractSearchKeywords() 메소드가 제거되었습니다.
     * 키워드 추출 기능은 GetMediaListUseCase.extractKeywordsFromCaption()에서 처리합니다.
     * 
     * 해당 기능의 테스트는 GetMediaListUseCaseTest.kt에서 수행됩니다.
     */
    // 이 테스트는 삭제된 메소드를 참조하므로 주석 처리됨
    /*
    @Test
    fun `MediaItem keyword extraction works correctly`() {
        val mediaItem = MediaItem("Title", "Android mobile development framework tutorial")
        val keywords = mediaItem.extractSearchKeywords()
        
        assertEquals(3, keywords.size) // Should extract exactly 3 keywords
        assertTrue(keywords.contains("Android"))
        assertTrue(keywords.contains("mobile"))
        assertTrue(keywords.contains("development"))
        // "tutorial" should not be included as we only take first 3
    }
    */

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
    fun `Summary utility methods work correctly`() {
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

    @Test
    fun `MediaItem type detection works correctly`() {
        assertTrue(MediaItem("Title", "Caption", null, "url", "image").isImage())
        assertTrue(MediaItem("Title", "Caption", null, "url", "bitmap").isImage())
        assertFalse(MediaItem("Title", "Caption", null, "url", "video").isImage())
        assertFalse(MediaItem("Title", "Caption", null, "url", "unknown").isImage())
    }

    @Test
    fun `domain objects handle null values correctly`() {
        // Summary with null values
        val summary = Summary("Title", "Description", null, null)
        assertFalse(summary.hasThumbnail())
        assertFalse(summary.hasOriginalImage())
        assertNull(summary.getDisplayImageUrl())
        
        // MediaItem with null values
        val mediaItem = MediaItem("Title", "Caption", null, null)
        assertFalse(mediaItem.hasImage())
        assertEquals("Caption", mediaItem.getDisplayCaption())
    }

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
}
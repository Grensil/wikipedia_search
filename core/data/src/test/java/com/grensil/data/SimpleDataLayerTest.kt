package com.grensil.data

import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import org.junit.Assert.*
import org.junit.Test

/**
 * Data Layer 테스트 클래스
 * 
 * 테스트 목적:
 * 1. 도메인 모델(Summary, MediaItem) 기본 동작 검증
 * 2. 데이터 검증 로직 (isValid, hasThumbnail 등) 테스트
 * 3. 이미지 URL 처리 로직 검증
 * 
 * 특징:
 * - 실제 네트워크 호출 없음 (순수 데이터 클래스 테스트)
 * - Android API + JUnit 4만 사용
 * - 빠른 실행 속도로 개발 중 자주 실행 가능
 */
class SimpleDataLayerTest {

    /**
     * 📄 Summary 데이터 클래스 기본 동작 테스트
     * 
     * 테스트 시나리오:
     * 1. 모든 속성을 가진 Summary 객체 생성
     * 2. 각 속성값이 올바르게 설정되는지 검증
     * 
     * 검증 속성: title, description, thumbnailUrl, originalImageUrl, 
     *          pageId, extract, timestamp
     * 
     * 목적: Wikipedia Summary API 응답을 담는 데이터 클래스가 정상 동작하는지 확인
     */
    @Test
    fun `Summary data class works correctly`() {
        // Given: 모든 속성을 가진 Summary 객체 생성
        val summary = Summary(
            title = "Test Article",
            description = "Test description",
            thumbnailUrl = "https://example.com/thumb.jpg",
            originalImageUrl = "https://example.com/full.jpg",
            pageId = 123,
            extract = "Test extract content",
            timestamp = "2023-01-01T00:00:00Z"
        )

        // Then: 각 속성값이 올바르게 설정되었는지 검증
        assertEquals("제목이 올바르게 설정되어야 함", "Test Article", summary.title)
        assertEquals("설명이 올바르게 설정되어야 함", "Test description", summary.description)
        assertEquals("썸네일 URL이 올바르게 설정되어야 함", "https://example.com/thumb.jpg", summary.thumbnailUrl)
        assertEquals("원본 이미지 URL이 올바르게 설정되어야 함", "https://example.com/full.jpg", summary.originalImageUrl)
        assertEquals("페이지 ID가 올바르게 설정되어야 함", 123, summary.pageId)
        assertEquals("추출 내용이 올바르게 설정되어야 함", "Test extract content", summary.extract)
        assertEquals("타임스탬프가 올바르게 설정되어야 함", "2023-01-01T00:00:00Z", summary.timestamp)
    }

    /**
     * ✅ Summary 유효성 검증 테스트: isValid() 메소드 동작 확인
     * 
     * 테스트 시나리오:
     * 1. 유효한 Summary (제목 + 설명 모두 있음) → true
     * 2. 무효한 Summary들 (제목 또는 설명 없음) → false
     * 
     * 비즈니스 규칙: 
     * - 제목과 설명이 모두 비어있지 않아야 유효
     * - 하나라도 비어있으면 무효한 Summary로 판단
     * 
     * UI에서 활용: 유효하지 않은 Summary는 화면에 표시하지 않음
     */
    @Test
    fun `Summary isValid works correctly`() {
        // 케이스 1: 유효한 Summary (제목 + 설명 모두 존재)
        val validSummary = Summary("Title", "Description")
        assertTrue("제목과 설명이 모두 있으면 유효해야 함", validSummary.isValid())

        // 케이스 2: 무효한 Summary - 제목 없음
        val invalidSummary1 = Summary("", "Description")
        assertFalse("제목이 비어있으면 무효해야 함", invalidSummary1.isValid())

        // 케이스 3: 무효한 Summary - 설명 없음
        val invalidSummary2 = Summary("Title", "")
        assertFalse("설명이 비어있으면 무효해야 함", invalidSummary2.isValid())

        // 케이스 4: 무효한 Summary - 제목과 설명 모두 없음
        val invalidSummary3 = Summary("", "")
        assertFalse("제목과 설명이 모두 비어있으면 무효해야 함", invalidSummary3.isValid())
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
     * 📱 MediaItem 데이터 클래스 기본 동작 테스트
     * 
     * 테스트 시나리오:
     * 1. 모든 속성을 가진 MediaItem 객체 생성
     * 2. 각 속성값이 올바르게 설정되는지 검증
     * 
     * 검증 속성: title, caption, extractedKeywords, imageUrl, type
     * 
     * 목적: Wikipedia Media-list API 응답을 담는 데이터 클래스가 정상 동작하는지 확인
     * 활용: ListView 각 항목에 표시될 데이터의 무결성 보장
     */
    @Test
    fun `MediaItem data class works correctly`() {
        // Given: 모든 속성을 가진 MediaItem 객체 생성
        val mediaItem = MediaItem(
            title = "Test Image",
            caption = "Test image caption",
            extractedKeywords = null, // UseCase에서 나중에 설정
            imageUrl = "https://example.com/image.jpg",
            type = "image"
        )

        // Then: 각 속성값이 올바르게 설정되었는지 검증
        assertEquals("제목이 올바르게 설정되어야 함", "Test Image", mediaItem.title)
        assertEquals("캡션이 올바르게 설정되어야 함", "Test image caption", mediaItem.caption)
        assertEquals("이미지 URL이 올바르게 설정되어야 함", "https://example.com/image.jpg", mediaItem.imageUrl)
        assertEquals("타입이 올바르게 설정되어야 함", "image", mediaItem.type)
    }

    @Test
    fun `MediaItem hasImage works correctly`() {
        // With image
        val mediaWithImage = MediaItem("Title", "Caption", null, "https://example.com/image.jpg")
        assertTrue(mediaWithImage.hasImage())

        // Without image
        val mediaWithoutImage = MediaItem("Title", "Caption", null, null)
        assertFalse(mediaWithoutImage.hasImage())

        // Empty image URL
        val mediaEmptyImage = MediaItem("Title", "Caption", null, "")
        assertFalse(mediaEmptyImage.hasImage())
    }

    @Test
    fun `MediaItem isValid works correctly`() {
        // Valid media item
        val validMedia = MediaItem("Test Title", "Caption")
        assertTrue(validMedia.isValid())

        // Invalid media item - blank title
        val invalidMedia = MediaItem("", "Caption")
        assertFalse(invalidMedia.isValid())
    }

    @Test
    fun `MediaItem isImage works correctly`() {
        // Image type
        val imageItem = MediaItem("Title", "Caption", null, "url", "image")
        assertTrue(imageItem.isImage())

        // Bitmap type
        val bitmapItem = MediaItem("Title", "Caption", null, "url", "bitmap")
        assertTrue(bitmapItem.isImage())

        // Video type
        val videoItem = MediaItem("Title", "Caption", null, "url", "video")
        assertFalse(videoItem.isImage())

        // Unknown type
        val unknownItem = MediaItem("Title", "Caption", null, "url", "unknown")
        assertFalse(unknownItem.isImage())
    }

    @Test
    fun `MediaItem getDisplayCaption works correctly`() {
        // With caption
        val mediaWithCaption = MediaItem("Title", "Test caption")
        assertEquals("Test caption", mediaWithCaption.getDisplayCaption())

        // Without caption - should use title
        val mediaWithoutCaption = MediaItem("Test Title", "")
        assertEquals("Test Title", mediaWithoutCaption.getDisplayCaption())
    }

    // 참고: MediaItem.extractSearchKeywords() 메소드가 제거되어 이 테스트는 삭제됨
    // 키워드 추출 기능은 GetMediaListUseCase.extractKeywordsFromCaption()에서 처리하므로
    // 해당 기능의 테스트는 GetMediaListUseCaseTest.kt에서 수행
}
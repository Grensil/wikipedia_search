package com.grensil.data

import com.grensil.data.entity.MediaListEntity
import com.grensil.data.entity.SummaryEntity
import com.grensil.data.mapper.WikipediaMapper
import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import org.junit.Assert.*
import org.junit.Test

/**
 * Data Layer 완전 통합 테스트 클래스
 * 
 * 테스트 목적:
 * 1. 도메인 모델(Summary, MediaItem) 기본 동작 검증
 * 2. 데이터 검증 로직 (isValid, hasThumbnail 등) 테스트
 * 3. 이미지 URL 처리 로직 검증
 * 4. WikipediaMapper의 Entity → Domain 변환 로직 검증
 * 5. Entity들의 null 값 처리 검증
 * 
 * 통합 내용:
 * - SimpleDataLayerTest.kt의 모든 테스트 케이스 (도메인 모델 테스트)
 * - SimpleDataIntegrationTest.kt의 매퍼 테스트 케이스
 * - 중복 제거 및 통합된 검증 로직
 * 
 * 특징:
 * - 실제 네트워크 호출 없음 (순수 데이터 클래스 및 매퍼 테스트)
 * - Android API + JUnit 4만 사용
 * - 빠른 실행 속도로 개발 중 자주 실행 가능
 */
class DataLayerCompleteTest {

    // =================================
    // Summary 데이터 클래스 테스트
    // =================================

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

    // =================================
    // MediaItem 데이터 클래스 테스트
    // =================================

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

    /**
     * 🖼️ MediaItem 이미지 관련 메소드 테스트
     * 
     * hasImage(), isImage(), getDisplayCaption() 메소드의 동작 검증
     */
    @Test
    fun `MediaItem utility methods work correctly`() {
        // hasImage() 테스트
        val mediaWithImage = MediaItem("Title", "Caption", null, "https://example.com/image.jpg")
        assertTrue("이미지 URL이 있으면 true", mediaWithImage.hasImage())

        val mediaWithoutImage = MediaItem("Title", "Caption", null, null)
        assertFalse("이미지 URL이 없으면 false", mediaWithoutImage.hasImage())

        val mediaEmptyImage = MediaItem("Title", "Caption", null, "")
        assertFalse("빈 이미지 URL이면 false", mediaEmptyImage.hasImage())

        // isValid() 테스트
        val validMedia = MediaItem("Test Title", "Caption")
        assertTrue("제목이 있으면 유효", validMedia.isValid())

        val invalidMedia = MediaItem("", "Caption")
        assertFalse("제목이 없으면 무효", invalidMedia.isValid())

        // isImage() 테스트
        assertTrue("image 타입은 이미지", MediaItem("Title", "Caption", null, "url", "image").isImage())
        assertTrue("bitmap 타입은 이미지", MediaItem("Title", "Caption", null, "url", "bitmap").isImage())
        assertFalse("video 타입은 이미지 아님", MediaItem("Title", "Caption", null, "url", "video").isImage())
        assertFalse("unknown 타입은 이미지 아님", MediaItem("Title", "Caption", null, "url", "unknown").isImage())

        // getDisplayCaption() 테스트
        val mediaWithCaption = MediaItem("Title", "Test caption")
        assertEquals("캡션이 있으면 캡션 반환", "Test caption", mediaWithCaption.getDisplayCaption())

        val mediaWithoutCaption = MediaItem("Test Title", "")
        assertEquals("캡션이 없으면 제목 반환", "Test Title", mediaWithoutCaption.getDisplayCaption())
    }

    // =================================
    // WikipediaMapper 매퍼 테스트
    // =================================

    /**
     * 🔄 WikipediaMapper SummaryEntity 매핑 테스트
     * 
     * 테스트 시나리오:
     * 1. 완전한 데이터를 가진 SummaryEntity → Summary 매핑
     * 2. 모든 속성이 올바르게 변환되는지 검증
     * 
     * 목적: API 응답 Entity가 Domain 모델로 정확히 변환되는지 확인
     */
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
        assertEquals("제목이 올바르게 매핑되어야 함", "Android", summary.title)
        assertEquals("설명이 올바르게 매핑되어야 함", "Mobile operating system", summary.description)
        assertEquals("썸네일 URL이 올바르게 매핑되어야 함", "https://example.com/thumb.jpg", summary.thumbnailUrl)
        assertEquals("원본 이미지 URL이 올바르게 매핑되어야 함", "https://example.com/full.jpg", summary.originalImageUrl)
        assertEquals("페이지 ID가 올바르게 매핑되어야 함", 123, summary.pageId)
        assertEquals("추출 내용이 올바르게 매핑되어야 함", "Android is a mobile operating system", summary.extract)
        assertEquals("타임스탬프가 올바르게 매핑되어야 함", "2023-01-01T00:00:00Z", summary.timestamp)
    }

    /**
     * 🛡️ WikipediaMapper null 값 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. null 값이 포함된 SummaryEntity → Summary 매핑
     * 2. null 값들이 안전하게 처리되는지 검증
     * 
     * 중요성: Wikipedia API에서 일부 필드가 null로 올 수 있음
     * 목적: null 값으로 인한 앱 크래시 방지
     */
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
        assertEquals("제목이 올바르게 매핑되어야 함", "Test Title", summary.title)
        assertEquals("설명이 올바르게 매핑되어야 함", "Test Description", summary.description)
        assertNull("썸네일 URL은 null이어야 함", summary.thumbnailUrl)
        assertNull("원본 이미지 URL은 null이어야 함", summary.originalImageUrl)
        assertEquals("페이지 ID는 0이어야 함", 0, summary.pageId)
        assertEquals("추출 내용은 빈 문자열이어야 함", "", summary.extract)
        assertNull("타임스탬프는 null이어야 함", summary.timestamp)
    }

    /**
     * 📂 WikipediaMapper MediaListEntity 매핑 테스트
     * 
     * 테스트 시나리오:
     * 1. 완전한 데이터를 가진 MediaListEntity → List<MediaItem> 매핑
     * 2. 여러 미디어 아이템이 올바르게 변환되는지 검증
     * 3. srcset 데이터에서 이미지 URL 추출 확인
     * 
     * 목적: Wikipedia Media-list API 응답이 Domain 모델로 정확히 변환되는지 확인
     */
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
        assertEquals("2개 아이템이 변환되어야 함", 2, mediaItems.size)
        
        // First item
        assertEquals("첫 번째 아이템 제목", "Android Logo", mediaItems[0].title)
        assertEquals("첫 번째 아이템 캡션", "Official Android logo", mediaItems[0].caption)
        assertEquals("첫 번째 아이템 타입", "image", mediaItems[0].type)
        assertEquals("첫 번째 아이템 이미지 URL", "https://example.com/android_logo.png", mediaItems[0].imageUrl)
        
        // Second item 
        assertEquals("두 번째 아이템 제목", "Android Architecture", mediaItems[1].title)
        assertEquals("두 번째 아이템 캡션", "Android system architecture diagram", mediaItems[1].caption)
        assertEquals("두 번째 아이템 타입", "image", mediaItems[1].type)
        assertNull("두 번째 아이템은 srcset이 없으므로 이미지 URL은 null", mediaItems[1].imageUrl) // No srcset
    }

    /**
     * 🎯 WikipediaMapper 키워드 추출 비포함 테스트
     * 
     * 테스트 시나리오:
     * 1. 매퍼는 순수한 데이터 변환만 수행해야 함
     * 2. 키워드 추출은 UseCase의 책임이므로 매핑 결과에는 포함되지 않음
     * 
     * 설계 원칙:
     * - Data Layer는 순수한 데이터 변환만 담당
     * - 비즈니스 로직(키워드 추출)은 Domain Layer(UseCase)에서 처리
     */
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
        assertEquals("1개 아이템이 변환되어야 함", 1, mediaItems.size)
        val mediaItem = mediaItems[0]
        
        // Mapper는 순수한 데이터 변환만 수행
        assertEquals("제목이 올바르게 매핑되어야 함", "Test Title", mediaItem.title)
        assertEquals("캡션이 올바르게 매핑되어야 함", "Android mobile development framework tutorial guide", mediaItem.caption)
        assertNull("키워드는 UseCase에서 처리하므로 null이어야 함", mediaItem.extractedKeywords) // UseCase에서 처리하므로 null이어야 함
        assertEquals("타입이 올바르게 매핑되어야 함", "image", mediaItem.type)
    }

    /**
     * 📋 빈 MediaListEntity 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. 빈 items 리스트를 가진 MediaListEntity 매핑
     * 2. 빈 리스트가 안전하게 처리되는지 확인
     * 
     * 목적: Wikipedia API에서 빈 응답이 와도 앱이 안정적으로 동작하도록 보장
     */
    @Test
    fun `MediaListEntity should handle empty items list`() {
        // Given
        val emptyMediaListEntity = MediaListEntity(items = emptyList())

        // When
        val mediaItems = WikipediaMapper.mapToMediaItemList(emptyMediaListEntity)

        // Then
        assertTrue("빈 리스트가 반환되어야 함", mediaItems.isEmpty())
    }

    /**
     * 📄 최소 데이터 SummaryEntity 매핑 테스트
     * 
     * 테스트 시나리오:
     * 1. 최소한의 필수 데이터만 있는 SummaryEntity 매핑
     * 2. 필수 필드(title, description)만으로도 유효한 Summary 생성되는지 확인
     * 
     * 목적: Wikipedia API에서 최소한의 데이터만 제공되어도 앱이 정상 동작하도록 보장
     */
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
        assertNotNull("Summary가 생성되어야 함", summary)
        assertEquals("최소 데이터 제목", "Minimal Title", summary.title)
        assertEquals("최소 데이터 설명", "Minimal Description", summary.description)
        assertTrue("최소 데이터로도 유효한 Summary여야 함", summary.isValid()) // Should be valid with title and description
    }

    // =================================
    // 엣지 케이스 및 통합 테스트
    // =================================

    /**
     * 🔄 엣지 케이스 테스트: Summary 경계값 처리 검증
     * 
     * 테스트 시나리오:
     * 1. 매우 짧은 설명에 대한 축약 처리
     * 2. 제한 길이와 정확히 같은 설명 처리
     * 3. 이미지 우선순위 처리 (썸네일 vs 원본)
     */
    @Test
    fun `Summary edge cases are handled correctly`() {
        // 케이스 1: 매우 짧은 설명 → 축약하지 않음
        val summary = Summary("Title", "Short")
        assertEquals("짧은 설명은 그대로 반환해야 함", "Short", summary.getShortDescription(100))
        
        // 케이스 2: 제한 길이와 정확히 같은 설명 → 축약하지 않음
        val summary2 = Summary("Title", "A".repeat(50))
        assertEquals("제한과 같은 길이면 정확히 50글자여야 함", 50, summary2.getShortDescription(50).length)
        assertFalse("제한과 같은 길이면 ...을 붙이지 않아야 함", summary2.getShortDescription(50).endsWith("..."))
        
        // 케이스 3: 이미지 우선순위 처리
        val summaryWithBoth = Summary("Title", "Desc", "thumb.jpg", "original.jpg")
        assertEquals("썸네일이 우선되어야 함", "thumb.jpg", summaryWithBoth.getDisplayImageUrl())
        
        val summaryOriginalOnly = Summary("Title", "Desc", null, "original.jpg") 
        assertEquals("원본만 있으면 원본 반환", "original.jpg", summaryOriginalOnly.getDisplayImageUrl())
    }

    /**
     * 🛡️ null 값 처리 종합 테스트
     * 
     * Summary와 MediaItem 모두의 null 값 처리를 통합 검증
     */
    @Test
    fun `data classes handle null values correctly`() {
        // Summary null 처리
        val summary = Summary("Title", "Description", null, null)
        assertFalse("썸네일이 null이면 false", summary.hasThumbnail())
        assertFalse("원본 이미지가 null이면 false", summary.hasOriginalImage())
        assertNull("이미지가 없으면 null 반환", summary.getDisplayImageUrl())
        
        // MediaItem null 처리
        val mediaItem = MediaItem("Title", "Caption", null, null)
        assertFalse("이미지가 null이면 false", mediaItem.hasImage())
        assertEquals("캡션이 있으면 캡션 반환", "Caption", mediaItem.getDisplayCaption())
        
        // 빈 문자열 처리
        val mediaItemEmpty = MediaItem("Test Title", "")
        assertEquals("캡션이 비어있으면 제목 반환", "Test Title", mediaItemEmpty.getDisplayCaption())
    }

    /**
     * 🔧 데이터 일관성 검증 테스트
     * 
     * 매퍼를 통해 변환된 도메인 객체가 비즈니스 규칙을 만족하는지 검증
     */
    @Test
    fun `mapped domain objects maintain business rules consistency`() {
        // 유효한 엔티티 → 유효한 도메인 객체
        val validEntity = SummaryEntity(
            type = "standard",
            title = "Valid Title",
            displaytitle = null,
            pageid = 123,
            extract = "Valid extract",
            extractHtml = null,
            thumbnail = null,
            originalimage = null,
            lang = "en",
            dir = "ltr",
            timestamp = "2023-01-01T00:00:00Z",
            description = "Valid Description"
        )
        
        val validSummary = WikipediaMapper.mapToSummary(validEntity)
        assertTrue("매핑된 Summary는 유효해야 함", validSummary.isValid())
        
        // 미디어 아이템도 동일하게 검증
        val validMediaEntity = MediaListEntity(
            items = listOf(
                MediaListEntity.MediaItemEntity(
                    title = "Valid Media Title",
                    section_id = 1,
                    type = "image",
                    caption = MediaListEntity.MediaItemEntity.CaptionEntity(
                        text = "Valid caption",
                        html = null
                    ),
                    srcset = emptyList()
                )
            )
        )
        
        val validMediaItems = WikipediaMapper.mapToMediaItemList(validMediaEntity)
        assertEquals("1개 아이템이 변환되어야 함", 1, validMediaItems.size)
        assertTrue("매핑된 MediaItem은 유효해야 함", validMediaItems[0].isValid())
    }

    /**
     * 📊 매퍼 성능 및 대용량 데이터 처리 테스트
     * 
     * 대용량 MediaListEntity 처리가 안정적으로 동작하는지 확인
     */
    @Test
    fun `mapper handles large data sets efficiently`() {
        // 대용량 미디어 리스트 생성 (100개 아이템)
        val largeItemList = (1..100).map { i ->
            MediaListEntity.MediaItemEntity(
                title = "Media Item $i",
                section_id = i,
                type = "image",
                caption = MediaListEntity.MediaItemEntity.CaptionEntity(
                    text = "Caption for media item $i",
                    html = null
                ),
                srcset = listOf(
                    MediaListEntity.MediaItemEntity.SrcSetEntity(
                        src = "https://example.com/image$i.jpg",
                        scale = "1x"
                    )
                )
            )
        }
        
        val largeMediaListEntity = MediaListEntity(items = largeItemList)
        
        // When
        val startTime = System.currentTimeMillis()
        val mediaItems = WikipediaMapper.mapToMediaItemList(largeMediaListEntity)
        val endTime = System.currentTimeMillis()
        
        // Then
        assertEquals("100개 아이템이 모두 변환되어야 함", 100, mediaItems.size)
        assertTrue("매핑 시간이 1초를 넘지 않아야 함", (endTime - startTime) < 1000)
        
        // 첫 번째와 마지막 아이템 검증
        assertEquals("첫 번째 아이템 제목", "Media Item 1", mediaItems[0].title)
        assertEquals("마지막 아이템 제목", "Media Item 100", mediaItems[99].title)
        assertTrue("모든 아이템이 유효해야 함", mediaItems.all { it.isValid() })
    }
}
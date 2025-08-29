package com.grensil.domain.dto

/**
 * Wikipedia 미디어 아이템 도메인 모델
 */
data class MediaItem(
    val title: String,
    val caption: String,
    val extractedKeywords : String? = null,
    val imageUrl: String? = null,
    val type: String = "unknown"
) {

    /**
     * 이미지가 있는지 확인
     */
    fun hasImage(): Boolean = !imageUrl.isNullOrBlank()

    /**
     * 유효한 미디어 아이템인지 확인
     */
    fun isValid(): Boolean = title.isNotBlank()

    /**
     * 이미지 타입인지 확인
     */
    fun isImage(): Boolean = type.equals("image", ignoreCase = true) ||
            type.equals("bitmap", ignoreCase = true)

    /**
     * 표시용 캡션 (빈 경우 제목 사용)
     */
    fun getDisplayCaption(): String = caption.ifBlank { title }

    /**
     * 캡션에서 검색 키워드 추출 (과제 요구사항: 세 개 단어)
     */
    fun extractSearchKeywords(): List<String> {
        return caption
            .split("\\s+".toRegex()) // 공백으로 분리
            .filter { it.isNotBlank() && it.length > 2 } // 2글자 이상만
            .map { it.replace("[^a-zA-Z0-9]".toRegex(), "") } // 특수문자 제거
            .filter { it.isNotBlank() }
            .take(3) // 최대 3개까지
    }
}
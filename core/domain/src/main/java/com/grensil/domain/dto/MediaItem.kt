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
}
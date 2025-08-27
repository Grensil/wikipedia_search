package com.grensil.domain.dto

data class Summary(
    val title: String,
    val description: String,
    val thumbnailUrl: String? = null,
    val originalImageUrl: String? = null,
    val pageId: Int = 0,
    val extract: String = "",
    val timestamp: String? = null
) {

    /**
     * 썸네일 이미지가 있는지 확인
     */
    fun hasThumbnail(): Boolean = !thumbnailUrl.isNullOrBlank()

    /**
     * 원본 이미지가 있는지 확인
     */
    fun hasOriginalImage(): Boolean = !originalImageUrl.isNullOrBlank()

    /**
     * 유효한 요약 정보인지 확인
     */
    fun isValid(): Boolean = title.isNotBlank() && description.isNotBlank()

    /**
     * 표시용 이미지 URL (썸네일 우선, 없으면 원본)
     */
    fun getDisplayImageUrl(): String? = thumbnailUrl ?: originalImageUrl

    /**
     * 짧은 설명 (최대 길이 제한)
     */
    fun getShortDescription(maxLength: Int = 100): String {
        return if (description.length <= maxLength) {
            description
        } else {
            description.take(maxLength - 3) + "..."
        }
    }
}
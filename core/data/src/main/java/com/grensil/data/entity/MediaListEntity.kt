package com.grensil.data.entity

/**
 * Wikipedia Media List API 응답 Entity
 * API: https://en.wikipedia.org/api/rest_v1/page/media-list/{검색어}
 */
data class MediaListEntity(
    val items: List<MediaItemEntity>? = null
) {
    data class MediaItemEntity(
        val title: String? = null,
        val section_id: Int? = null,
        val type: String? = null,
        val caption: CaptionEntity? = null,
        val srcset: List<SrcSetEntity>? = null
    ) {
        data class CaptionEntity(
            val text: String? = null,
            val html: String? = null
        )

        data class SrcSetEntity(
            val src: String? = null,
            val scale: String? = null
        )
    }
}
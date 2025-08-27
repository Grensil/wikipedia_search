package com.grensil.data.entity

data class SummaryEntity(
    val type: String? = null,
    val title: String? = null,
    val displaytitle: String? = null,
    val pageid: Int? = null,
    val extract: String? = null,
    val extractHtml: String? = null,
    val thumbnail: ThumbnailEntity? = null,
    val originalimage: OriginalImageEntity? = null,
    val lang: String? = null,
    val dir: String? = null,
    val timestamp: String? = null,
    val description: String? = null
) {
    data class ThumbnailEntity(
        val source: String? = null,
        val width: Int? = null,
        val height: Int? = null
    )

    data class OriginalImageEntity(
        val source: String? = null,
        val width: Int? = null,
        val height: Int? = null
    )
}
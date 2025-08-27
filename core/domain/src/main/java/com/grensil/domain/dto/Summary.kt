package com.grensil.domain.dto

data class Summary(
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val originalImageUrl: String? = null,
    val pageId: Int? = null,
    val extract: String? = null,
    val timestamp : String? = null
)

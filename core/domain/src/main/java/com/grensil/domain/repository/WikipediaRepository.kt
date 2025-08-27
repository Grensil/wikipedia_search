package com.grensil.domain.repository

import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary

interface WikipediaRepository {
    suspend fun getSummary(searchTerm: String): Summary
    suspend fun getMediaList(searchTerm: String): List<MediaItem>
    fun getDetailPageUrl(searchTerm: String): String
}
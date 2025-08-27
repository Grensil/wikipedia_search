package com.grensil.domain.repository

import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary

/**
 * Wikipedia 데이터 접근을 위한 Repository 인터페이스
 */
interface WikipediaRepository {

    /**
     * 검색어로 요약 정보 조회
     */
    suspend fun getSummary(searchTerm: String): Summary

    /**
     * 검색어로 미디어 목록 조회
     */
    suspend fun getMediaList(searchTerm: String): List<MediaItem>

    /**
     * 상세 페이지 URL 생성
     */
    fun getDetailPageUrl(searchTerm: String): String
}
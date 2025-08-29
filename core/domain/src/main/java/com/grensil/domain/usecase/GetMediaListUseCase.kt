package com.grensil.domain.usecase

import com.grensil.domain.dto.MediaItem
import com.grensil.domain.repository.WikipediaRepository

/**
 * Wikipedia 미디어 목록 조회 UseCase
 */
class GetMediaListUseCase(
    private val wikipediaRepository: WikipediaRepository
) {

    /**
     * 검색어로 미디어 목록 조회
     * @param searchTerm 검색할 키워드
     * @return 유효한 MediaItem 목록 (필터링 적용)
     * @throws IllegalArgumentException 검색어가 유효하지 않은 경우
     * @throws Exception 네트워크 오류 등
     */
    suspend operator fun invoke(searchTerm: String): List<MediaItem> {
        // 입력 유효성 검사
        require(searchTerm.isNotBlank()) { "Search term cannot be blank" }
        require(searchTerm.isNotEmpty()) { "Search term must be at least 1 characters" }

        // 검색어 정규화
        val normalizedSearchTerm = normalizeSearchTerm(searchTerm)

        // Repository를 통해 데이터 조회 및 필터링, 키워드 추출
        val mediaItems = wikipediaRepository.getMediaList(normalizedSearchTerm)
        
        return mediaItems
            .filter { it.isValid() && it.hasImage() } // 먼저 필터링으로 처리할 객체 수 줄이기
            .map { item -> 
                item.copy(extractedKeywords = extractKeywordsFromCaption(item.caption))
            }
    }

    /**
     * 검색어 정규화
     */
    private fun normalizeSearchTerm(searchTerm: String): String {
        return searchTerm
            .trim()
            .replace("\\s+".toRegex(), " ")
            .split(" ")
            .joinToString(" ") { it }
    }

    /**
     * Caption에서 의미있는 키워드 3개 추출
     */
    private fun extractKeywordsFromCaption(caption: String): String {
        return caption
            .split("\\s+".toRegex()) // 공백으로 분할
            .filter { it.length > 1 && it.matches("[a-zA-Z가-힣]+".toRegex()) } // 의미있는 단어만
            .take(3)
            .joinToString(" ")
    }
}

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
        require(searchTerm.length >= 2) { "Search term must be at least 2 characters" }

        // 검색어 정규화
        val normalizedSearchTerm = normalizeSearchTerm(searchTerm)

        // Repository를 통해 데이터 조회
        val mediaItems = wikipediaRepository.getMediaList(normalizedSearchTerm)

        // 결과 필터링 및 정렬
        return filterAndSortMediaItems(mediaItems)
    }

    /**
     * 검색어 정규화
     */
    private fun normalizeSearchTerm(searchTerm: String): String {
        return searchTerm
            .trim()
            .replace("\\s+".toRegex(), " ")
            .split(" ")
            .joinToString("_") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }
    }

    /**
     * 미디어 아이템 필터링 및 정렬
     */
    private fun filterAndSortMediaItems(items: List<MediaItem>): List<MediaItem> {
        return items
            .filter { it.isValid() } // 유효한 아이템만
            .filter { it.hasImage() } // 이미지가 있는 아이템만
            .sortedWith(compareBy<MediaItem> { !it.isImage() }.thenBy { it.title }) // 이미지 우선, 제목순 정렬
            .take(20) // 최대 20개까지
    }
}

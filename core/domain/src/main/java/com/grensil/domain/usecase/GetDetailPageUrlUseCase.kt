package com.grensil.domain.usecase

import com.grensil.domain.repository.WikipediaRepository

/**
 * Wikipedia 상세 페이지 URL 생성 UseCase
 */
class GetDetailPageUrlUseCase(
    private val wikipediaRepository: WikipediaRepository
) {

    /**
     * 검색어로 상세 페이지 URL 생성
     * @param searchTerm 검색할 키워드
     * @return 상세 페이지 URL
     * @throws IllegalArgumentException 검색어가 유효하지 않은 경우
     */
    operator fun invoke(searchTerm: String): String {
        // 입력 유효성 검사
        require(searchTerm.isNotBlank()) { "Search term cannot be blank" }
        require(searchTerm.length >= 2) { "Search term must be at least 2 characters" }

        // 검색어 정규화
        val normalizedSearchTerm = normalizeSearchTerm(searchTerm)

        // Repository를 통해 URL 생성
        return wikipediaRepository.getDetailPageUrl(normalizedSearchTerm)
    }

    /**
     * 검색어 정규화
     */
    private fun normalizeSearchTerm(searchTerm: String): String {
        return searchTerm
            .trim()
            .replace("\\s+".toRegex(), " ")
            .split(" ")
            .joinToString("_") { it.lowercase().capitalize() }
    }
}

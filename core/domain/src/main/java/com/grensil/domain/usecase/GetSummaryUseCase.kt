package com.grensil.domain.usecase

import com.grensil.domain.dto.Summary
import com.grensil.domain.repository.WikipediaRepository

/**
 * Wikipedia 요약 정보 조회 UseCase 인터페이스
 */
interface GetSummaryUseCase {
    suspend operator fun invoke(searchTerm: String): Summary
}

/**
 * Wikipedia 요약 정보 조회 UseCase 구현체
 */
class GetSummaryUseCaseImpl(
    private val wikipediaRepository: WikipediaRepository
) : GetSummaryUseCase {

    /**
     * 검색어로 요약 정보 조회
     * @param searchTerm 검색할 키워드
     * @return Summary 도메인 모델
     * @throws IllegalArgumentException 검색어가 유효하지 않은 경우
     * @throws Exception 네트워크 오류 등
     */
    override suspend operator fun invoke(searchTerm: String): Summary {
        // 입력 유효성 검사
        require(searchTerm.isNotBlank()) { "Search term cannot be blank" }
        require(searchTerm.isNotEmpty()) { "Search term must be at least 1 characters" }

        // 검색어 정규화
        val normalizedSearchTerm = normalizeSearchTerm(searchTerm)

        // Repository를 통해 데이터 조회
        val summary = wikipediaRepository.getSummary(normalizedSearchTerm)

        // 결과 유효성 검사
        if (!summary.isValid()) {
            throw IllegalStateException("Invalid summary data received for: $searchTerm")
        }

        return summary
    }

    /**
     * 검색어 정규화
     */
    private fun normalizeSearchTerm(searchTerm: String): String {
        return searchTerm
            .trim()
            .replace("\\s+".toRegex(), " ") // 연속 공백을 단일 공백으로
            .split(" ")
            .joinToString(" ") { it }
    }
}
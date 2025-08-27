package com.grensil.domain.usecase

/**
 * 캡션에서 검색 키워드 추출 UseCase
 * 과제 요구사항: ListView 항목 클릭 시 caption.text에서 세 개 단어 추출
 */
class SearchKeywordExtractorUseCase {

    /**
     * 캡션에서 검색 키워드 추출
     * @param caption 캡션 텍스트
     * @return 추출된 키워드 목록 (최대 3개)
     */
    operator fun invoke(caption: String): List<String> {
        if (caption.isBlank()) return emptyList()

        return caption
            .split("\\s+".toRegex()) // 공백으로 분리
            .filter { word ->
                word.isNotBlank() &&
                        word.length > 2 && // 2글자 이상
                        !isStopWord(word.lowercase()) // 불용어 제외
            }
            .map { cleanWord(it) } // 특수문자 정리
            .filter { it.isNotBlank() }
            .distinct() // 중복 제거
            .take(3) // 최대 3개
    }

    /**
     * 불용어 확인 (영어 기준)
     */
    private fun isStopWord(word: String): Boolean {
        val stopWords = setOf(
            "the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
            "is", "are", "was", "were", "be", "been", "being", "have", "has", "had",
            "do", "does", "did", "will", "would", "could", "should", "may", "might",
            "this", "that", "these", "those", "a", "an", "as", "if", "then", "than"
        )
        return stopWords.contains(word)
    }

    /**
     * 단어 정리 (특수문자 제거)
     */
    private fun cleanWord(word: String): String {
        return word
            .replace("[^a-zA-Z0-9']".toRegex(), "") // 알파벳, 숫자, 아포스트로피만 허용
            .trim('\'') // 앞뒤 아포스트로피 제거
    }
}

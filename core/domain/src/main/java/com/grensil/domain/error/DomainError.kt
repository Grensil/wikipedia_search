package com.grensil.domain.error

/**
 * 도메인 레이어의 공통 에러 타입
 * 하위 레이어(data, network)의 구체적인 예외를 추상화하여 
 * 상위 레이어(presentation)에서 타입 안전하게 처리할 수 있도록 함
 */
sealed class DomainError : Exception() {
    
    /**
     * 네트워크 관련 에러
     */
    data class NetworkError(
        val type: NetworkErrorType,
        val httpCode: Int? = null,
        val originalMessage: String? = null
    ) : DomainError() {
        override val message: String
            get() = originalMessage ?: type.defaultMessage
    }
    
    /**
     * 데이터 검증 에러
     */
    data class ValidationError(
        val field: String, 
        val reason: String
    ) : DomainError() {
        override val message: String = ""
            get() = "Validation failed for $field: $reason"
    }
    
    /**
     * 예상하지 못한 에러
     */
    data class UnknownError(
        val originalException: Throwable
    ) : DomainError() {
        override val message: String
            get() = originalException.message ?: "Unknown error occurred"
    }
}

/**
 * 네트워크 에러 타입 분류
 */
enum class NetworkErrorType(val defaultMessage: String) {
    CONNECTION_FAILED("네트워크 연결에 실패했습니다"),
    TIMEOUT("연결 시간이 초과되었습니다"),
    NOT_FOUND("요청한 정보를 찾을 수 없습니다"),
    SERVER_ERROR("서버에 일시적인 문제가 발생했습니다"),
    SSL_ERROR("보안 연결에 실패했습니다"),
    PARSE_ERROR("응답 데이터 형식이 올바르지 않습니다"),
    INVALID_REQUEST("잘못된 요청입니다"),
    INVALID_URL("올바르지 않은 URL입니다")
}
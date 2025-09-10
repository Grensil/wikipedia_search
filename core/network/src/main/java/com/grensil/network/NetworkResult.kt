package com.grensil.network

/**
 * 네트워크 작업의 결과를 나타내는 sealed class
 * 예외 기반이 아닌 Result 패턴으로 에러 처리
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val error: NetworkError) : NetworkResult<T>()
}

/**
 * 네트워크 에러 정보를 담는 데이터 클래스
 * 플랫폼에 독립적이며 재사용 가능한 구조
 */
data class NetworkError(
    val type: NetworkErrorType,
    val httpCode: Int? = null,
    val message: String? = null,
    val cause: Throwable? = null
)

/**
 * 네트워크 에러 타입 분류
 * 모든 플랫폼에서 공통으로 사용 가능한 일반적인 분류
 */
enum class NetworkErrorType {
    /** 연결 실패 */
    CONNECTION_FAILED,
    
    /** 요청 타임아웃 */
    TIMEOUT,
    
    /** 리소스를 찾을 수 없음 (404) */
    NOT_FOUND,
    
    /** 클라이언트 에러 (400-499) */
    CLIENT_ERROR,
    
    /** 서버 에러 (500-599) */
    SERVER_ERROR,
    
    /** SSL/TLS 에러 */
    SSL_ERROR,
    
    /** 응답 파싱 에러 */
    PARSE_ERROR,
    
    /** 잘못된 URL 형식 */
    INVALID_URL,
    
    /** 알 수 없는 에러 */
    UNKNOWN
}
package com.grensil.network

/**
 * 네트워크 관련 예외를 정의하는 sealed class
 */
sealed class NhnNetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /**
     * 연결 실패 예외
     */
    class ConnectionExceptionNhn(message: String, cause: Throwable? = null) : NhnNetworkException(message, cause)

    /**
     * 타임아웃 예외
     */
    class TimeoutExceptionNhn(message: String, cause: Throwable? = null) : NhnNetworkException(message, cause)

    /**
     * HTTP 상태 코드 에러 예외
     */
    class HttpExceptionNhn(val statusCode: Int, message: String, val response: String? = null) : NhnNetworkException(message)

    /**
     * 응답 파싱 실패 예외
     */
    class ParseExceptionNhn(message: String, cause: Throwable? = null) : NhnNetworkException(message, cause)

    /**
     * 잘못된 URL 형식 예외
     */
    class InvalidUrlExceptionNhn(message: String, cause: Throwable? = null) : NhnNetworkException(message, cause)

    /**
     * SSL/TLS 관련 예외
     */
    class SSLExceptionNhn(message: String, cause: Throwable? = null) : NhnNetworkException(message, cause)
}
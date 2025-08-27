package com.grensil.network

/**
 * HTTP 응답 정보를 담는 데이터 클래스
 */
data class HttpResponse(
    val statusCode: Int,
    val headers: Map<String, String>,
    val body: String,
    val isSuccessful: Boolean = statusCode in 200..299
) {

    /**
     * Content-Type 헤더 값 조회
     */
    fun getContentType(): String? {
        return headers.entries.find { it.key.equals("Content-Type", ignoreCase = true) }?.value
    }

    /**
     * Content-Length 헤더 값 조회
     */
    fun getContentLength(): Long? {
        return headers.entries.find { it.key.equals("Content-Length", ignoreCase = true) }?.value?.toLongOrNull()
    }

    /**
     * JSON 응답인지 확인
     */
    fun isJson(): Boolean {
        return getContentType()?.contains("application/json", ignoreCase = true) == true
    }

    /**
     * 클라이언트 에러 (4xx)인지 확인
     */
    fun isClientError(): Boolean {
        return statusCode in 400..499
    }

    /**
     * 서버 에러 (5xx)인지 확인
     */
    fun isServerError(): Boolean {
        return statusCode in 500..599
    }

    /**
     * 리다이렉트 (3xx)인지 확인
     */
    fun isRedirect(): Boolean {
        return statusCode in 300..399
    }
}
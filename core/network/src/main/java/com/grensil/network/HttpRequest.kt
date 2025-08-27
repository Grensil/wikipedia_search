package com.grensil.network

/**
 * HTTP 요청 정보를 담는 데이터 클래스
 */
data class HttpRequest(
    val url: String,
    val method: HttpMethod = HttpMethod.GET,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val timeoutMs: Int = 10000
) {

    /**
     * 요청 유효성 검사
     */
    fun validate() {
        require(url.isNotBlank()) { "URL cannot be empty" }
        require(url.startsWith("http://") || url.startsWith("https://")) {
            "URL must start with http:// or https://"
        }
        require(timeoutMs > 0) { "Timeout must be greater than 0" }

        // POST, PUT 요청인데 body가 null인 경우 경고 (필수는 아님)
        if (method in listOf(HttpMethod.POST, HttpMethod.PUT) && body.isNullOrBlank()) {
            // 로그만 남기고 예외는 발생시키지 않음 (body 없는 POST도 유효할 수 있음)
        }
    }

    /**
     * Content-Type 헤더 확인
     */
    fun hasContentType(): Boolean {
        return headers.keys.any { it.equals("Content-Type", ignoreCase = true) }
    }

    /**
     * Accept 헤더 확인
     */
    fun hasAccept(): Boolean {
        return headers.keys.any { it.equals("Accept", ignoreCase = true) }
    }
}
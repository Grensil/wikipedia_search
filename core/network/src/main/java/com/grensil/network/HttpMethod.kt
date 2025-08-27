package com.grensil.network

/**
 * HTTP 메서드를 정의하는 enum 클래스
 */
enum class HttpMethod(val value: String) {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE")
}
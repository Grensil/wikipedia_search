package com.grensil.network

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * HttpClient 테스트 클래스
 * 
 * 테스트 목적:
 * 1. 과제 1단계: HttpURLConnection 기반 통신 모듈 테스트
 * 2. HTTP 요청/응답 데이터 클래스 동작 검증
 * 3. 예외 처리 및 응답 유틸리티 메소드 검증
 * 
 * 사용 기술: Android API + JUnit 4만 사용 (외부 라이브러리 없음)
 * 주의사항: 실제 네트워크 호출은 통합 테스트에서 처리
 */
class HttpClientTest {

    // 테스트 대상 HttpClient 인스턴스
    private lateinit var httpClient: HttpClient

    /**
     * 각 테스트 실행 전 초기화
     * - HttpClient 인스턴스 생성
     */
    @Before
    fun setup() {
        httpClient = HttpClient()
    }

    /**
     * 📦 데이터 클래스 테스트: HttpResponse 기본 동작 검증
     * 
     * 테스트 시나리오:
     * 1. HTTP 응답 객체를 생성
     * 2. 모든 속성값이 올바르게 설정되는지 확인
     * 3. 성공 응답(200)에 대해 isSuccessful이 true인지 확인
     * 
     * 검증 항목: statusCode, headers, body, isSuccessful 속성
     */
    @Test
    fun `HttpResponse data class works correctly`() {
        // Given: HTTP 응답 데이터 준비
        val headers = mapOf("Accept" to "application/json")
        
        // When: HttpResponse 객체 생성
        val response = HttpResponse(
            statusCode = 200,
            headers = headers,
            body = "test response"
        )

        // Then: 모든 속성이 올바르게 설정되었는지 검증
        assertEquals("상태 코드가 올바르게 설정되어야 함", 200, response.statusCode)
        assertEquals("헤더가 올바르게 설정되어야 함", headers, response.headers)
        assertEquals("응답 본문이 올바르게 설정되어야 함", "test response", response.body)
        assertTrue("200 상태 코드는 성공으로 판단되어야 함", response.isSuccessful)
    }

    /**
     * ✅ 성공 응답 테스트: 2xx 상태 코드에 대한 isSuccessful 검증
     * 
     * 테스트 시나리오:
     * 1. HTTP 2xx 범위의 다양한 상태 코드 테스트
     * 2. 모든 2xx 코드에서 isSuccessful이 true 반환하는지 확인
     * 
     * 테스트 상태 코드:
     * - 200 OK (일반 성공)
     * - 201 Created (생성 성공)
     * - 204 No Content (본문 없는 성공)
     * - 299 Custom Success (2xx 범위 마지막)
     */
    @Test
    fun `HttpResponse isSuccessful returns true for 2xx codes`() {
        assertTrue("200 OK는 성공", HttpResponse(200, emptyMap(), "OK").isSuccessful)
        assertTrue("201 Created는 성공", HttpResponse(201, emptyMap(), "Created").isSuccessful)
        assertTrue("204 No Content는 성공", HttpResponse(204, emptyMap(), "No Content").isSuccessful)
        assertTrue("299는 2xx 범위로 성공", HttpResponse(299, emptyMap(), "Custom Success").isSuccessful)
    }

    @Test
    fun `HttpResponse isSuccessful returns false for non-2xx codes`() {
        assertFalse(HttpResponse(199, emptyMap(), "Custom").isSuccessful)
        assertFalse(HttpResponse(300, emptyMap(), "Multiple Choices").isSuccessful)
        assertFalse(HttpResponse(400, emptyMap(), "Bad Request").isSuccessful)
        assertFalse(HttpResponse(404, emptyMap(), "Not Found").isSuccessful)
        assertFalse(HttpResponse(500, emptyMap(), "Internal Server Error").isSuccessful)
    }

    @Test
    fun `HttpResponse utility methods work correctly`() {
        // Test getContentType
        val jsonResponse = HttpResponse(200, mapOf("Content-Type" to "application/json"), "")
        assertEquals("application/json", jsonResponse.getContentType())

        // Test getContentLength
        val lengthResponse = HttpResponse(200, mapOf("Content-Length" to "123"), "")
        assertEquals(123L, lengthResponse.getContentLength())

        // Test isJson
        assertTrue(jsonResponse.isJson())
        assertFalse(HttpResponse(200, emptyMap(), "").isJson())

        // Test isClientError
        assertTrue(HttpResponse(404, emptyMap(), "").isClientError())
        assertFalse(HttpResponse(200, emptyMap(), "").isClientError())

        // Test isServerError
        assertTrue(HttpResponse(500, emptyMap(), "").isServerError())
        assertFalse(HttpResponse(200, emptyMap(), "").isServerError())

        // Test isRedirect
        assertTrue(HttpResponse(301, emptyMap(), "").isRedirect())
        assertFalse(HttpResponse(200, emptyMap(), "").isRedirect())
    }

    @Test
    fun `NhnNetworkException hierarchy works correctly`() {
        val cause = RuntimeException("Original error")
        
        val connectionException = NhnNetworkException.ConnectionExceptionNhn("Connection failed", cause)
        assertTrue(connectionException is NhnNetworkException)
        assertEquals("Connection failed", connectionException.message)
        assertEquals(cause, connectionException.cause)

        val timeoutException = NhnNetworkException.TimeoutExceptionNhn("Timeout occurred", cause)
        assertTrue(timeoutException is NhnNetworkException)
        assertEquals("Timeout occurred", timeoutException.message)
        assertEquals(cause, timeoutException.cause)

        val sslException = NhnNetworkException.SSLExceptionNhn("SSL error", cause)
        assertTrue(sslException is NhnNetworkException)
        assertEquals("SSL error", sslException.message)
        assertEquals(cause, sslException.cause)

        val invalidUrlException = NhnNetworkException.InvalidUrlExceptionNhn("Invalid URL", cause)
        assertTrue(invalidUrlException is NhnNetworkException)
        assertEquals("Invalid URL", invalidUrlException.message)
        assertEquals(cause, invalidUrlException.cause)

        val httpException = NhnNetworkException.HttpExceptionNhn(404, "HTTP error", "response")
        assertTrue(httpException is NhnNetworkException)
        assertEquals("HTTP error", httpException.message)
        assertEquals(404, httpException.statusCode)
        assertEquals("response", httpException.response)
    }

    @Test
    fun `HttpRequest data class works correctly`() {
        // Given
        val headers = mapOf("Authorization" to "Bearer token")
        
        // When
        val request = HttpRequest(
            url = "https://api.example.com",
            method = HttpMethod.POST,
            headers = headers,
            body = "test body",
            timeoutMs = 5000
        )

        // Then
        assertEquals("https://api.example.com", request.url)
        assertEquals(HttpMethod.POST, request.method)
        assertEquals(headers, request.headers)
        assertEquals("test body", request.body)
        assertEquals(5000, request.timeoutMs)
    }

    @Test
    fun `HttpMethod enum has correct values`() {
        assertEquals("GET", HttpMethod.GET.name)
        assertEquals("POST", HttpMethod.POST.name)
        assertEquals("PUT", HttpMethod.PUT.name)
        assertEquals("DELETE", HttpMethod.DELETE.name)
        
        assertEquals("GET", HttpMethod.GET.value)
        assertEquals("POST", HttpMethod.POST.value)
        assertEquals("PUT", HttpMethod.PUT.value)
        assertEquals("DELETE", HttpMethod.DELETE.value)
    }

    @Test
    fun `HttpResponse extension functions work correctly`() {
        // Test asString
        val response = HttpResponse(200, emptyMap(), "test content")
        assertEquals("test content", response.asString())

        // Test asBytes
        val bytes = response.asBytes()
        assertArrayEquals("test content".toByteArray(Charsets.UTF_8), bytes)

        // Test asInt
        val intResponse = HttpResponse(200, emptyMap(), "123")
        assertEquals(123, intResponse.asInt())

        val invalidIntResponse = HttpResponse(200, emptyMap(), "not a number")
        assertNull(invalidIntResponse.asInt())

        // Test asLong
        val longResponse = HttpResponse(200, emptyMap(), "123456789")
        assertEquals(123456789L, longResponse.asLong())

        // Test asBoolean
        val boolResponse = HttpResponse(200, emptyMap(), "true")
        assertEquals(true, boolResponse.asBoolean())

        val falseBoolResponse = HttpResponse(200, emptyMap(), "false")
        assertEquals(false, falseBoolResponse.asBoolean())
    }

    @Test
    fun `HttpClient instance can be created`() {
        // Just verify that HttpClient can be instantiated without errors
        val client = HttpClient()
        assertNotNull(client)
    }

    // Note: We cannot easily test the actual HTTP methods (get, post, etc.) without
    // a real server or complex mocking of HttpURLConnection. The integration tests
    // handle testing the actual HTTP functionality.
}
package com.grensil.network

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Network Module Unit Test
 * 
 * Tests network layer components:
 * - HTTP client functionality
 * - HTTP response handling
 * - Network error processing
 * - Data transformation utilities
 * 
 * Naming Convention:
 * - Class: NetworkTest
 * - Methods: test_[component]_[condition]_[expectedResult]
 */
class NetworkTest {

    private lateinit var httpClient: HttpClient

    @Before
    fun setup() {
        httpClient = HttpClient()
    }

    // =====================================
    // 🌐 HttpResponse Tests
    // =====================================

    @Test
    fun `HttpResponse data class works correctly`() {
        // Given
        val headers = mapOf("Accept" to "application/json")
        
        // When
        val response = HttpResponse(
            statusCode = 200,
            headers = headers,
            body = "test response"
        )

        // Then
        assertEquals("상태 코드가 올바르게 설정되어야 함", 200, response.statusCode)
        assertEquals("헤더가 올바르게 설정되어야 함", headers, response.headers)
        assertEquals("응답 본문이 올바르게 설정되어야 함", "test response", response.body)
        assertTrue("200 상태 코드는 성공으로 판단되어야 함", response.isSuccessful)
    }

    @Test
    fun `HttpResponse isSuccessful works correctly for all status code ranges`() {
        // 2xx Success codes
        assertTrue("200 OK는 성공", HttpResponse(200, emptyMap(), "OK").isSuccessful)
        assertTrue("201 Created는 성공", HttpResponse(201, emptyMap(), "Created").isSuccessful)
        assertTrue("204 No Content는 성공", HttpResponse(204, emptyMap(), "No Content").isSuccessful)
        assertTrue("299는 2xx 범위로 성공", HttpResponse(299, emptyMap(), "Custom Success").isSuccessful)

        // Non-2xx codes should return false
        assertFalse("199는 2xx 범위 아님", HttpResponse(199, emptyMap(), "Custom").isSuccessful)
        assertFalse("300은 리다이렉트", HttpResponse(300, emptyMap(), "Multiple Choices").isSuccessful)
        assertFalse("400은 클라이언트 에러", HttpResponse(400, emptyMap(), "Bad Request").isSuccessful)
        assertFalse("404는 Not Found", HttpResponse(404, emptyMap(), "Not Found").isSuccessful)
        assertFalse("500은 서버 에러", HttpResponse(500, emptyMap(), "Internal Server Error").isSuccessful)
    }

    @Test
    fun `HttpResponse status code category detection works correctly`() {
        // Client Error (4xx)
        assertTrue("400은 클라이언트 에러", HttpResponse(400, emptyMap(), "Bad Request").isClientError())
        assertTrue("404는 클라이언트 에러", HttpResponse(404, emptyMap(), "Not Found").isClientError())
        assertTrue("499는 4xx 범위", HttpResponse(499, emptyMap(), "Custom client error").isClientError())
        assertFalse("200은 클라이언트 에러 아님", HttpResponse(200, emptyMap(), "OK").isClientError())

        // Server Error (5xx)
        assertTrue("500은 서버 에러", HttpResponse(500, emptyMap(), "Internal Server Error").isServerError())
        assertTrue("502는 서버 에러", HttpResponse(502, emptyMap(), "Bad Gateway").isServerError())
        assertTrue("599는 5xx 범위", HttpResponse(599, emptyMap(), "Custom server error").isServerError())
        assertFalse("200은 서버 에러 아님", HttpResponse(200, emptyMap(), "OK").isServerError())

        // Redirect (3xx)
        assertTrue("300은 리다이렉트", HttpResponse(300, emptyMap(), "Multiple Choices").isRedirect())
        assertTrue("301은 리다이렉트", HttpResponse(301, emptyMap(), "Moved Permanently").isRedirect())
        assertTrue("399는 3xx 범위", HttpResponse(399, emptyMap(), "Custom redirect").isRedirect())
        assertFalse("200은 리다이렉트 아님", HttpResponse(200, emptyMap(), "OK").isRedirect())
    }

    @Test
    fun `HttpResponse content type detection works correctly`() {
        // JSON content type detection
        val jsonResponse = HttpResponse(200, mapOf("Content-Type" to "application/json"), "{}")
        assertTrue("JSON content type 감지", jsonResponse.isJson())
        assertEquals("JSON content type 반환", "application/json", jsonResponse.getContentType())

        // JSON with charset
        val jsonWithCharset = HttpResponse(200, mapOf("Content-Type" to "application/json; charset=utf-8"), "{}")
        assertTrue("JSON with charset 감지", jsonWithCharset.isJson())

        // Case insensitive header
        val lowerCaseHeader = HttpResponse(200, mapOf("content-type" to "text/html"), "<html></html>")
        assertEquals("대소문자 무관하게 content type 반환", "text/html", lowerCaseHeader.getContentType())
        assertFalse("HTML은 JSON 아님", lowerCaseHeader.isJson())

        // No content type
        val noContentType = HttpResponse(200, emptyMap(), "text")
        assertNull("Content-Type 헤더 없음", noContentType.getContentType())
        assertFalse("Content-Type 없으면 JSON 아님", noContentType.isJson())
    }

    @Test
    fun `HttpResponse content length parsing works correctly`() {
        // Valid content length
        val validLength = HttpResponse(200, mapOf("Content-Length" to "1024"), "")
        assertEquals("유효한 Content-Length 파싱", 1024L, validLength.getContentLength())

        // Invalid content length
        val invalidLength = HttpResponse(200, mapOf("Content-Length" to "invalid"), "")
        assertNull("잘못된 Content-Length는 null", invalidLength.getContentLength())

        // Missing content length
        val missingLength = HttpResponse(200, emptyMap(), "")
        assertNull("Content-Length 헤더 없으면 null", missingLength.getContentLength())

        // Case insensitive header
        val lowerCaseLength = HttpResponse(200, mapOf("content-length" to "2048"), "")
        assertEquals("대소문자 무관하게 Content-Length 파싱", 2048L, lowerCaseLength.getContentLength())
    }

    // =====================================
    // 📦 HttpClient Tests
    // =====================================

    @Test
    fun `HttpClient instance can be created`() {
        // Basic instantiation test
        val client = HttpClient()
        assertNotNull("HttpClient 인스턴스 생성 가능", client)
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
        assertEquals("URL 설정 확인", "https://api.example.com", request.url)
        assertEquals("HTTP 메소드 설정 확인", HttpMethod.POST, request.method)
        assertEquals("헤더 설정 확인", headers, request.headers)
        assertEquals("요청 본문 설정 확인", "test body", request.body)
        assertEquals("타임아웃 설정 확인", 5000, request.timeoutMs)
    }

    @Test
    fun `HttpRequest validation works correctly`() {
        // Valid request
        val validRequest = HttpRequest(
            url = "https://api.example.com",
            method = HttpMethod.GET,
            timeoutMs = 1000
        )
        
        // Should not throw exception for valid request
        try {
            validRequest.validate()
        } catch (e: Exception) {
            fail("유효한 요청에서 예외 발생하지 않아야 함: ${e.message}")
        }

        // Invalid URL - empty
        val emptyUrlRequest = HttpRequest(url = "", method = HttpMethod.GET)
        assertThrows("빈 URL은 예외 발생", IllegalArgumentException::class.java) {
            emptyUrlRequest.validate()
        }

        // Invalid URL - no protocol
        val noProtocolRequest = HttpRequest(url = "example.com", method = HttpMethod.GET)
        assertThrows("프로토콜 없는 URL은 예외 발생", IllegalArgumentException::class.java) {
            noProtocolRequest.validate()
        }

        // Invalid timeout
        val invalidTimeoutRequest = HttpRequest(url = "https://example.com", method = HttpMethod.GET, timeoutMs = 0)
        assertThrows("0 타임아웃은 예외 발생", IllegalArgumentException::class.java) {
            invalidTimeoutRequest.validate()
        }
    }

    @Test
    fun `HttpRequest header utility methods work correctly`() {
        // Request with Content-Type
        val requestWithContentType = HttpRequest(
            url = "https://example.com",
            headers = mapOf("Content-Type" to "application/json")
        )
        assertTrue("Content-Type 헤더 존재 확인", requestWithContentType.hasContentType())

        // Request with Accept header
        val requestWithAccept = HttpRequest(
            url = "https://example.com", 
            headers = mapOf("Accept" to "application/json")
        )
        assertTrue("Accept 헤더 존재 확인", requestWithAccept.hasAccept())

        // Request without headers
        val requestWithoutHeaders = HttpRequest(url = "https://example.com")
        assertFalse("Content-Type 헤더 없음", requestWithoutHeaders.hasContentType())
        assertFalse("Accept 헤더 없음", requestWithoutHeaders.hasAccept())

        // Case insensitive header check
        val caseInsensitiveRequest = HttpRequest(
            url = "https://example.com",
            headers = mapOf("content-type" to "text/plain")
        )
        assertTrue("대소문자 무관하게 Content-Type 감지", caseInsensitiveRequest.hasContentType())
    }

    @Test
    fun `HttpMethod enum works correctly`() {
        // Name and value consistency
        assertEquals("GET 메소드 이름", "GET", HttpMethod.GET.name)
        assertEquals("POST 메소드 이름", "POST", HttpMethod.POST.name)
        assertEquals("PUT 메소드 이름", "PUT", HttpMethod.PUT.name)
        assertEquals("DELETE 메소드 이름", "DELETE", HttpMethod.DELETE.name)
        
        assertEquals("GET 메소드 값", "GET", HttpMethod.GET.value)
        assertEquals("POST 메소드 값", "POST", HttpMethod.POST.value)
        assertEquals("PUT 메소드 값", "PUT", HttpMethod.PUT.value)
        assertEquals("DELETE 메소드 값", "DELETE", HttpMethod.DELETE.value)
    }

    // =====================================
    // 🚨 Exception Tests
    // =====================================

    @Test
    fun `NhnNetworkException hierarchy works correctly`() {
        val cause = RuntimeException("Original error")
        
        // Connection Exception
        val connectionException = NhnNetworkException.ConnectionExceptionNhn("Connection failed", cause)
        assertTrue("ConnectionException은 NhnNetworkException", connectionException is NhnNetworkException)
        assertEquals("Connection failed", connectionException.message)
        assertEquals(cause, connectionException.cause)

        // Timeout Exception
        val timeoutException = NhnNetworkException.TimeoutExceptionNhn("Timeout occurred", cause)
        assertTrue("TimeoutException은 NhnNetworkException", timeoutException is NhnNetworkException)
        assertEquals("Timeout occurred", timeoutException.message)
        assertEquals(cause, timeoutException.cause)

        // SSL Exception
        val sslException = NhnNetworkException.SSLExceptionNhn("SSL error", cause)
        assertTrue("SSLException은 NhnNetworkException", sslException is NhnNetworkException)
        assertEquals("SSL error", sslException.message)
        assertEquals(cause, sslException.cause)

        // Invalid URL Exception
        val invalidUrlException = NhnNetworkException.InvalidUrlExceptionNhn("Invalid URL", cause)
        assertTrue("InvalidUrlException은 NhnNetworkException", invalidUrlException is NhnNetworkException)
        assertEquals("Invalid URL", invalidUrlException.message)
        assertEquals(cause, invalidUrlException.cause)

        // HTTP Exception (with additional properties)
        val httpException = NhnNetworkException.HttpExceptionNhn(404, "HTTP error", "response")
        assertTrue("HttpException은 NhnNetworkException", httpException is NhnNetworkException)
        assertEquals("HTTP error", httpException.message)
        assertEquals(404, httpException.statusCode)
        assertEquals("response", httpException.response)

        // Parse Exception
        val parseException = NhnNetworkException.ParseExceptionNhn("Parse error", cause)
        assertTrue("ParseException은 NhnNetworkException", parseException is NhnNetworkException)
        assertEquals("Parse error", parseException.message)
        assertEquals(cause, parseException.cause)
    }

    // =====================================
    // 🔧 Extension Methods Tests
    // =====================================

    @Test
    fun `HttpResponse extension functions work correctly`() {
        // String conversion
        val response = HttpResponse(200, emptyMap(), "test content")
        assertEquals("asString() 변환", "test content", response.asString())

        // Byte array conversion
        val bytes = response.asBytes()
        assertArrayEquals("asBytes() 변환", "test content".toByteArray(Charsets.UTF_8), bytes)

        // Integer conversion
        val intResponse = HttpResponse(200, emptyMap(), "123")
        assertEquals("asInt() 변환 - 유효한 숫자", 123, intResponse.asInt())

        val invalidIntResponse = HttpResponse(200, emptyMap(), "not a number")
        assertNull("asInt() 변환 - 잘못된 숫자", invalidIntResponse.asInt())

        // Long conversion
        val longResponse = HttpResponse(200, emptyMap(), "123456789")
        assertEquals("asLong() 변환", 123456789L, longResponse.asLong())

        // Boolean conversion
        val trueBoolResponse = HttpResponse(200, emptyMap(), "true")
        assertEquals("asBoolean() 변환 - true", true, trueBoolResponse.asBoolean())

        val falseBoolResponse = HttpResponse(200, emptyMap(), "false")
        assertEquals("asBoolean() 변환 - false", false, falseBoolResponse.asBoolean())

        val invalidBoolResponse = HttpResponse(200, emptyMap(), "invalid")
        assertNull("asBoolean() 변환 - 잘못된 불린", invalidBoolResponse.asBoolean())
    }

    @Test
    fun `HttpResponse asJsonString extension works correctly`() {
        // Valid JSON response
        val jsonResponse = HttpResponse(200, mapOf("Content-Type" to "application/json"), "{\"key\":\"value\"}")
        assertEquals("JSON 응답에서 asJsonString() 성공", "{\"key\":\"value\"}", jsonResponse.asJsonString())

        // Non-JSON response should throw ParseException
        val nonJsonResponse = HttpResponse(200, mapOf("Content-Type" to "text/plain"), "plain text")
        assertThrows("JSON이 아닌 응답에서 ParseException", NhnNetworkException.ParseExceptionNhn::class.java) {
            nonJsonResponse.asJsonString()
        }

        // No content type
        val noContentTypeResponse = HttpResponse(200, emptyMap(), "{\"key\":\"value\"}")
        assertThrows("Content-Type 없으면 ParseException", NhnNetworkException.ParseExceptionNhn::class.java) {
            noContentTypeResponse.asJsonString()
        }
    }

    @Test
    fun `HttpResponse extension functions handle edge cases`() {
        // Empty content
        val emptyResponse = HttpResponse(200, emptyMap(), "")
        assertEquals("빈 내용에서 asString()", "", emptyResponse.asString())
        assertArrayEquals("빈 내용에서 asBytes()", byteArrayOf(), emptyResponse.asBytes())
        assertNull("빈 내용에서 asInt()", emptyResponse.asInt())

        // Whitespace content
        val whitespaceResponse = HttpResponse(200, emptyMap(), "   ")
        assertEquals("공백 내용에서 asString()", "   ", whitespaceResponse.asString())
        assertNull("공백 내용에서 asInt()", whitespaceResponse.asInt())

        // Negative numbers
        val negativeResponse = HttpResponse(200, emptyMap(), "-123")
        assertEquals("음수에서 asInt()", -123, negativeResponse.asInt())
        assertEquals("음수에서 asLong()", -123L, negativeResponse.asLong())

        // Boolean edge cases
        val trueCaseResponse = HttpResponse(200, emptyMap(), "TRUE")
        assertNull("대문자 TRUE는 null (strictBooleanOnly)", trueCaseResponse.asBoolean())
    }

    // =====================================
    // 🧪 Integration Tests
    // =====================================

    @Test
    fun `complete HTTP request response cycle data integrity`() {
        // This tests the complete data flow from request creation to response processing
        val originalData = mapOf("key" to "value", "number" to "123")
        val headers = mapOf("Content-Type" to "application/json", "Accept" to "application/json")
        
        // Create request
        val request = HttpRequest(
            url = "https://api.example.com/test",
            method = HttpMethod.POST,
            headers = headers,
            body = originalData.toString(),
            timeoutMs = 5000
        )

        // Validate request
        request.validate() // Should not throw

        // Simulate response
        val response = HttpResponse(
            statusCode = 200,
            headers = mapOf("Content-Type" to "application/json"),
            body = "{\"result\":\"success\",\"data\":\"${originalData}\"}"
        )

        // Verify complete cycle
        assertTrue("요청이 유효함", request.hasContentType())
        assertTrue("응답이 성공적", response.isSuccessful)
        assertTrue("응답이 JSON", response.isJson())
        assertNotNull("JSON 문자열 추출 가능", response.asJsonString())
    }
}
package com.grensil.network

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * 🎯 Network Module 단위 테스트 중심 통합 클래스
 *
 * 특징:
 * - 외부 API 호출 제거
 * - 반복 테스트 최소화
 * - HttpResponse, HttpRequest, HttpClient, Extension 함수, Exception 중심
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
        val headers = mapOf("Accept" to "application/json")
        val response = HttpResponse(200, headers, "test response")

        assertEquals(200, response.statusCode)
        assertEquals(headers, response.headers)
        assertEquals("test response", response.body)
        assertTrue(response.isSuccessful)
    }

    @Test
    fun `HttpResponse status code categories`() {
        assertTrue(HttpResponse(200, emptyMap(), "").isSuccessful)
        assertTrue(HttpResponse(404, emptyMap(), "").isClientError())
        assertTrue(HttpResponse(500, emptyMap(), "").isServerError())
        assertTrue(HttpResponse(301, emptyMap(), "").isRedirect())
    }

    @Test
    fun `HttpResponse content type and length parsing`() {
        val jsonResponse = HttpResponse(200, mapOf("Content-Type" to "application/json", "Content-Length" to "123"), "{}")
        assertTrue(jsonResponse.isJson())
        assertEquals(123L, jsonResponse.getContentLength())
    }

    // =====================================
    // 📦 HttpClient & HttpRequest Tests
    // =====================================

    @Test
    fun `HttpClient instance creation`() {
        assertNotNull(HttpClient())
    }

    @Test
    fun `HttpRequest data class and validation`() {
        val request = HttpRequest("https://example.com", HttpMethod.POST, timeoutMs = 1000)
        request.validate() // Should not throw

        val invalidRequest = HttpRequest("invalid-url", HttpMethod.GET)
        assertThrows(IllegalArgumentException::class.java) { invalidRequest.validate() }
    }

    @Test
    fun `HttpMethod enum values`() {
        assertEquals("GET", HttpMethod.GET.name)
        assertEquals("POST", HttpMethod.POST.value)
    }

    // =====================================
    // 🚨 Exception Tests
    // =====================================

    @Test
    fun `NhnNetworkException hierarchy works`() {
        val cause = RuntimeException()
        val exceptions = listOf(
            NhnNetworkException.ConnectionExceptionNhn("msg", cause),
            NhnNetworkException.TimeoutExceptionNhn("msg", cause),
            NhnNetworkException.SSLExceptionNhn("msg", cause),
            NhnNetworkException.InvalidUrlExceptionNhn("msg", cause),
            NhnNetworkException.ParseExceptionNhn("msg", cause)
        )

        exceptions.forEach { ex ->
            assertTrue(ex is NhnNetworkException)
            assertEquals("msg", ex.message)
            assertEquals(cause, ex.cause)
        }

        val httpEx = NhnNetworkException.HttpExceptionNhn(404, "error", "resp")
        assertEquals(404, httpEx.statusCode)
        assertEquals("resp", httpEx.response)
    }

    // =====================================
    // 🔧 Extension Methods Tests
    // =====================================

    @Test
    fun `HttpResponse extension functions`() {
        val response = HttpResponse(200, emptyMap(), "123")
        assertEquals("123", response.asString())
        assertArrayEquals("123".toByteArray(), response.asBytes())
        assertEquals(123, response.asInt())
        assertEquals(123L, response.asLong())
    }

    @Test
    fun `HttpResponse boolean and JSON extensions`() {
        val boolResponse = HttpResponse(200, emptyMap(), "true")
        assertEquals(true, boolResponse.asBoolean())

        val jsonResponse = HttpResponse(200, mapOf("Content-Type" to "application/json"), "{\"key\":\"val\"}")
        assertEquals("{\"key\":\"val\"}", jsonResponse.asJsonString())

        val invalidJsonResponse = HttpResponse(200, mapOf("Content-Type" to "text/plain"), "text")
        assertThrows(NhnNetworkException.ParseExceptionNhn::class.java) { invalidJsonResponse.asJsonString() }
    }
}

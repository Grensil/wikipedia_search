package com.grensil.network

import org.junit.Test
import org.junit.Assert.*

class HttpResponseTest {

    @Test
    fun `isSuccessful should return true for 2xx status codes`() {
        // Given
        val response200 = HttpResponse(200, emptyMap(), "OK")
        val response201 = HttpResponse(201, emptyMap(), "Created")  
        val response299 = HttpResponse(299, emptyMap(), "Custom success")

        // Then
        assertTrue(response200.isSuccessful)
        assertTrue(response201.isSuccessful)
        assertTrue(response299.isSuccessful)
    }

    @Test
    fun `isSuccessful should return false for non-2xx status codes`() {
        // Given
        val response400 = HttpResponse(400, emptyMap(), "Bad Request")
        val response404 = HttpResponse(404, emptyMap(), "Not Found")
        val response500 = HttpResponse(500, emptyMap(), "Internal Server Error")

        // Then
        assertFalse(response400.isSuccessful)
        assertFalse(response404.isSuccessful)
        assertFalse(response500.isSuccessful)
    }

    @Test
    fun `getContentType should return content type header case insensitive`() {
        // Given
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Other-Header" to "value"
        )
        val response = HttpResponse(200, headers, "")

        // When
        val contentType = response.getContentType()

        // Then
        assertEquals("application/json", contentType)
    }

    @Test
    fun `getContentType should work with different case`() {
        // Given
        val headers = mapOf("content-type" to "text/html")
        val response = HttpResponse(200, headers, "")

        // When
        val contentType = response.getContentType()

        // Then
        assertEquals("text/html", contentType)
    }

    @Test
    fun `isJson should return true for JSON content type`() {
        // Given
        val headers = mapOf("Content-Type" to "application/json; charset=utf-8")
        val response = HttpResponse(200, headers, "{}")

        // Then
        assertTrue(response.isJson())
    }

    @Test
    fun `isJson should return false for non-JSON content type`() {
        // Given
        val headers = mapOf("Content-Type" to "text/html")
        val response = HttpResponse(200, headers, "<html></html>")

        // Then
        assertFalse(response.isJson())
    }

    @Test
    fun `isClientError should return true for 4xx status codes`() {
        // Given
        val response400 = HttpResponse(400, emptyMap(), "Bad Request")
        val response404 = HttpResponse(404, emptyMap(), "Not Found")
        val response499 = HttpResponse(499, emptyMap(), "Custom client error")

        // Then
        assertTrue(response400.isClientError())
        assertTrue(response404.isClientError())
        assertTrue(response499.isClientError())
    }

    @Test
    fun `isServerError should return true for 5xx status codes`() {
        // Given
        val response500 = HttpResponse(500, emptyMap(), "Internal Server Error")
        val response502 = HttpResponse(502, emptyMap(), "Bad Gateway")
        val response599 = HttpResponse(599, emptyMap(), "Custom server error")

        // Then
        assertTrue(response500.isServerError())
        assertTrue(response502.isServerError())
        assertTrue(response599.isServerError())
    }

    @Test
    fun `isRedirect should return true for 3xx status codes`() {
        // Given
        val response300 = HttpResponse(300, emptyMap(), "Multiple Choices")
        val response301 = HttpResponse(301, emptyMap(), "Moved Permanently")
        val response399 = HttpResponse(399, emptyMap(), "Custom redirect")

        // Then
        assertTrue(response300.isRedirect())
        assertTrue(response301.isRedirect())
        assertTrue(response399.isRedirect())
    }

    @Test
    fun `getContentLength should parse content length header`() {
        // Given
        val headers = mapOf("Content-Length" to "1024")
        val response = HttpResponse(200, headers, "")

        // When
        val contentLength = response.getContentLength()

        // Then
        assertEquals(1024L, contentLength)
    }

    @Test
    fun `getContentLength should return null for invalid content length`() {
        // Given
        val headers = mapOf("Content-Length" to "invalid")
        val response = HttpResponse(200, headers, "")

        // When
        val contentLength = response.getContentLength()

        // Then
        assertNull(contentLength)
    }
}
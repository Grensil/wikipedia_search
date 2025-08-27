package com.grensil.network

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.HttpsURLConnection

/**
 * NHN HTTP Client Library
 * HttpURLConnection/HttpsURLConnection 기반 HTTP 클라이언트
 *
 * 과제 요구사항:
 * - HttpURLConnection, HttpsURLConnection 기반 구현
 * - GET, POST, PUT, DELETE 지원
 * - Request Header 설정 가능
 * - Response Data 다양한 형식으로 반환 가능
 * - Request Body 전달 가능
 * - API 접속 timeout 설정 가능
 */
class HttpClient {

    /**
     * GET 요청 실행
     */
    fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        timeoutMs: Int = 10000
    ): HttpResponse {
        val request = HttpRequest(
            url = url,
            method = HttpMethod.GET,
            headers = headers,
            timeoutMs = timeoutMs
        )
        return execute(request)
    }

    /**
     * POST 요청 실행
     */
    fun post(
        url: String,
        body: String = "",
        headers: Map<String, String> = emptyMap(),
        timeoutMs: Int = 10000
    ): HttpResponse {
        val request = HttpRequest(
            url = url,
            method = HttpMethod.POST,
            headers = headers,
            body = body,
            timeoutMs = timeoutMs
        )
        return execute(request)
    }

    /**
     * PUT 요청 실행
     */
    fun put(
        url: String,
        body: String = "",
        headers: Map<String, String> = emptyMap(),
        timeoutMs: Int = 10000
    ): HttpResponse {
        val request = HttpRequest(
            url = url,
            method = HttpMethod.PUT,
            headers = headers,
            body = body,
            timeoutMs = timeoutMs
        )
        return execute(request)
    }

    /**
     * DELETE 요청 실행
     */
    fun delete(
        url: String,
        headers: Map<String, String> = emptyMap(),
        timeoutMs: Int = 10000
    ): HttpResponse {
        val request = HttpRequest(
            url = url,
            method = HttpMethod.DELETE,
            headers = headers,
            timeoutMs = timeoutMs
        )
        return execute(request)
    }

    /**
     * 범용 요청 실행
     */
    fun execute(request: HttpRequest): HttpResponse {
        // 요청 유효성 검사
        request.validate()

        var connection: HttpURLConnection? = null

        try {
            // URL 생성 및 Connection 열기
            val url = createURL(request.url)
            connection = createConnection(url)

            // Connection 설정
            setupConnection(connection, request)

            // Request Body 전송 (POST, PUT인 경우)
            if (request.body != null && request.method in listOf(HttpMethod.POST, HttpMethod.PUT)) {
                sendRequestBody(connection, request.body)
            }

            // Response 읽기
            return readResponse(connection)

        } catch (e: SocketTimeoutException) {
            throw NhnNetworkException.TimeoutExceptionNhn(
                "Request timeout after ${request.timeoutMs}ms: ${request.url}",
                e
            )
        } catch (e: UnknownHostException) {
            throw NhnNetworkException.ConnectionExceptionNhn("Unknown host: ${request.url}", e)
        } catch (e: NhnNetworkException.SSLExceptionNhn) {
            throw NhnNetworkException.SSLExceptionNhn("SSL connection failed: ${request.url}", e)
        } catch (e: MalformedURLException) {
            throw NhnNetworkException.InvalidUrlExceptionNhn("Invalid URL format: ${request.url}", e)
        } catch (e: NhnNetworkException) {
            throw e // 이미 우리 예외는 그대로 전파
        } catch (e: Exception) {
            throw NhnNetworkException.ConnectionExceptionNhn("Unexpected network error: ${e.message}", e)
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * URL 객체 생성
     */
    private fun createURL(urlString: String): URL {
        return try {
            URL(urlString)
        } catch (e: MalformedURLException) {
            throw NhnNetworkException.InvalidUrlExceptionNhn("Invalid URL: $urlString", e)
        }
    }

    /**
     * HttpURLConnection 또는 HttpsURLConnection 생성
     */
    private fun createConnection(url: URL): HttpURLConnection {
        return when (url.protocol.lowercase()) {
            "https" -> url.openConnection() as HttpsURLConnection
            "http" -> url.openConnection() as HttpURLConnection
            else -> throw NhnNetworkException.InvalidUrlExceptionNhn("Unsupported protocol: ${url.protocol}")
        }
    }

    /**
     * Connection 기본 설정
     */
    private fun setupConnection(connection: HttpURLConnection, request: HttpRequest) {
        connection.apply {
            // 기본 설정
            requestMethod = request.method.value
            connectTimeout = request.timeoutMs
            readTimeout = request.timeoutMs
            useCaches = false
            instanceFollowRedirects = true // 리다이렉트 자동 처리

            // Request Headers 설정
            request.headers.forEach { (key, value) ->
                setRequestProperty(key, value)
            }

            // 기본 헤더 설정 (사용자가 설정하지 않은 경우에만)
            setDefaultHeaders(request)

            // Request Body가 있는 경우 출력 활성화
            if (request.body != null && request.method in listOf(HttpMethod.POST, HttpMethod.PUT)) {
                doOutput = true
            }

            // GET, DELETE는 입력만 허용
            doInput = true
        }
    }

    /**
     * 기본 헤더 설정
     */
    private fun HttpURLConnection.setDefaultHeaders(request: HttpRequest) {
        // Content-Type 기본 설정 (POST, PUT에서 body가 있고 사용자가 설정하지 않은 경우)
        if (request.method in listOf(HttpMethod.POST, HttpMethod.PUT) &&
            request.body != null &&
            !request.hasContentType()) {
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        }

        // Accept 기본 설정 (사용자가 설정하지 않은 경우)
        if (!request.hasAccept()) {
            setRequestProperty("Accept", "application/json, text/plain, */*")
        }

        // User-Agent 설정
        if (getRequestProperty("User-Agent") == null) {
            setRequestProperty("User-Agent", "NHN-HTTP-Client/1.0.0")
        }
    }

    /**
     * Request Body 전송
     */
    private fun sendRequestBody(connection: HttpURLConnection, body: String) {
        OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
            writer.write(body)
            writer.flush()
        }
    }

    /**
     * Response 읽기 및 파싱
     */
    private fun readResponse(connection: HttpURLConnection): HttpResponse {
        val statusCode = connection.responseCode

        // Response Headers 읽기
        val headers = readResponseHeaders(connection)

        // Response Body 읽기
        val responseBody = readResponseBody(connection, statusCode)

        // HTTP 에러 상태 체크 및 예외 발생
        if (statusCode >= 400) {
            throw NhnNetworkException.HttpExceptionNhn(
                statusCode = statusCode,
                message = "HTTP $statusCode Error",
                response = responseBody
            )
        }

        return HttpResponse(
            statusCode = statusCode,
            headers = headers,
            body = responseBody
        )
    }

    /**
     * Response Headers 읽기
     */
    private fun readResponseHeaders(connection: HttpURLConnection): Map<String, String> {
        val headers = mutableMapOf<String, String>()

        connection.headerFields?.forEach { (key, values) ->
            if (key != null && values.isNotEmpty()) {
                // 여러 값이 있는 경우 쉼표로 구분하여 합침
                headers[key] = values.joinToString(", ")
            }
        }

        return headers
    }

    /**
     * Response Body 읽기
     */
    private fun readResponseBody(connection: HttpURLConnection, statusCode: Int): String {
        // 에러인 경우 errorStream, 정상인 경우 inputStream 사용
        val inputStream = if (statusCode >= 400) {
            connection.errorStream ?: connection.inputStream
        } else {
            connection.inputStream
        }

        return inputStream?.use { stream ->
            BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                reader.readText()
            }
        } ?: ""
    }
}

// ========== 확장 함수들 (선택사항) ==========
/**
 * HttpResponse 확장 함수들
 */

/**
 * Response body를 String으로 반환
 */
fun HttpResponse.asString(): String = body

/**
 * Response body를 ByteArray로 반환
 */
fun HttpResponse.asBytes(): ByteArray = body.toByteArray(Charsets.UTF_8)

/**
 * Response body를 Int로 파싱 (숫자인 경우)
 */
fun HttpResponse.asInt(): Int? = body.toIntOrNull()

/**
 * Response body를 Long으로 파싱 (숫자인 경우)
 */
fun HttpResponse.asLong(): Long? = body.toLongOrNull()

/**
 * Response body를 Boolean으로 파싱
 */
fun HttpResponse.asBoolean(): Boolean? = body.toBooleanStrictOrNull()

/**
 * JSON 응답인지 확인하고 body 반환
 */
fun HttpResponse.asJsonString(): String {
    if (!isJson()) {
        throw NhnNetworkException.ParseExceptionNhn("Response is not JSON format. Content-Type: ${getContentType()}")
    }
    return body
}
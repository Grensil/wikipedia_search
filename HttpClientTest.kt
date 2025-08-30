package com.grensil.test

import com.grensil.network.HttpClient

/**
 * NHN HTTP Client 간단한 테스트
 */
fun main() {
    val client = HttpClient()
    
    try {
        println("=== HTTP Client 테스트 시작 ===")
        
        // 1. 간단한 GET 요청 테스트
        println("\n1. GET 요청 테스트 (httpbin.org)")
        val getResponse = client.get("https://httpbin.org/get")
        println("Status: ${getResponse.statusCode}")
        println("Response: ${getResponse.body.take(200)}...")
        
        // 2. POST 요청 테스트  
        println("\n2. POST 요청 테스트")
        val postResponse = client.post(
            url = "https://httpbin.org/post",
            body = """{"test": "data", "message": "Hello NHN HTTP Client"}""",
            headers = mapOf("Content-Type" to "application/json")
        )
        println("Status: ${postResponse.statusCode}")
        println("Response: ${postResponse.body.take(200)}...")
        
        // 3. Headers 테스트
        println("\n3. 커스텀 헤더 테스트")
        val headerResponse = client.get(
            url = "https://httpbin.org/headers",
            headers = mapOf(
                "X-Custom-Header" to "NHN-Test",
                "User-Agent" to "NHN-HTTP-Client-Test/1.0"
            )
        )
        println("Status: ${headerResponse.statusCode}")
        println("Response: ${headerResponse.body.take(300)}...")
        
        println("\n=== 모든 테스트 성공! ===")
        
    } catch (e: Exception) {
        println("에러 발생: ${e.message}")
        e.printStackTrace()
    }
}
package com.grensil.navigation

import org.junit.Assert.*
import org.junit.Test
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * 🧭 Navigation Module 통합 테스트 클래스
 * 
 * 테스트 목적:
 * 1. Routes 객체의 경로 생성 로직 검증
 * 2. URL 인코딩/디코딩 처리 검증
 * 3. 다양한 검색어 형태 처리 검증
 * 4. Navigation 안전성 검증
 * 
 * 특징:
 * - Pure JUnit 테스트 (Android 의존성 없음)
 * - URL 인코딩 처리 로직 중점 검증
 * - 다국어 검색어 지원 확인
 */
class NavigationTest {

    // =====================================
    // 🔍 Search Routes Tests
    // =====================================

    /**
     * 📱 Search 경로 생성 기본 테스트
     * 
     * 테스트 시나리오:
     * 1. 일반적인 영어 검색어로 경로 생성
     * 2. 올바른 형식의 경로 반환 확인
     */
    @Test
    fun `Search createRoute generates correct path for basic query`() {
        // Given
        val searchQuery = "Android"
        
        // When
        val route = Routes.Search.createRoute(searchQuery)
        
        // Then
        assertEquals("기본 검색어 경로 생성", "search/Android", route)
    }

    /**
     * 🌐 Search 경로 생성 공백 포함 검색어 테스트
     * 
     * 테스트 시나리오:
     * 1. 공백이 포함된 검색어로 경로 생성
     * 2. URL 인코딩이 적용된 경로 반환 확인
     */
    @Test
    fun `Search createRoute handles spaces in query`() {
        // Given
        val searchQuery = "Android Development"
        
        // When
        val route = Routes.Search.createRoute(searchQuery)
        
        // Then
        assertTrue("공백은 URL 인코딩되어야 함", route.contains("Android") && route.contains("Development"))
        assertTrue("search 경로로 시작해야 함", route.startsWith("search/"))
        // URL 인코딩된 형태 확인 (실제 인코딩 결과에 따라 다를 수 있음)
        assertTrue("URL 인코딩된 공백 포함", route.contains("%20") || route.contains("+"))
    }

    /**
     * 🔤 Search 경로 생성 특수문자 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. 특수문자가 포함된 검색어로 경로 생성
     * 2. 특수문자가 올바르게 인코딩되는지 확인
     */
    @Test
    fun `Search createRoute handles special characters`() {
        // Given
        val testCases = mapOf(
            "C++" to "search/C%2B%2B",
            "C#" to "search/C%23",
            "Node.js" to "search/Node.js", // 점은 인코딩되지 않음
            "&" to "search/%26",
            "?" to "search/%3F"
        )
        
        testCases.forEach { (query, expectedRoute) ->
            // When
            val route = Routes.Search.createRoute(query)
            
            // Then
            assertEquals("특수문자 '$query' 처리", expectedRoute, route)
        }
    }

    /**
     * 🗾 Search 경로 생성 다국어 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. 한글, 일본어 등 다국어 검색어로 경로 생성
     * 2. 유니코드 문자가 올바르게 인코딩되는지 확인
     */
    @Test
    fun `Search createRoute handles unicode characters`() {
        // Given
        val testCases = listOf(
            "안드로이드" to "search/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C",
            "コトリン" to "search/%E3%82%B3%E3%83%88%E3%83%AA%E3%83%B3",
            "程序" to "search/%E7%A8%8B%E5%BA%8F"
        )
        
        testCases.forEach { (query, expectedRoute) ->
            // When
            val route = Routes.Search.createRoute(query)
            
            // Then
            assertEquals("유니코드 '$query' 처리", expectedRoute, route)
        }
    }

    /**
     * 🏁 Search 초기 경로 테스트
     * 
     * 테스트 시나리오:
     * 1. 검색어가 없는 초기 상태의 경로 생성
     * 2. 올바른 초기 경로 반환 확인
     */
    @Test
    fun `Search createInitialRoute returns correct initial path`() {
        // When
        val route = Routes.Search.createInitialRoute()
        
        // Then
        assertEquals("초기 검색 경로", "search/", route)
    }

    // =====================================
    // 📄 Detail Routes Tests
    // =====================================

    /**
     * 📱 Detail 경로 생성 기본 테스트
     * 
     * 테스트 시나리오:
     * 1. 일반적인 검색어로 Detail 경로 생성
     * 2. 올바른 형식의 경로 반환 확인
     */
    @Test
    fun `Detail createRoute generates correct path for basic query`() {
        // Given
        val searchQuery = "Kotlin"
        
        // When
        val route = Routes.Detail.createRoute(searchQuery)
        
        // Then
        assertEquals("기본 Detail 경로 생성", "detail/Kotlin", route)
    }

    /**
     * 🌐 Detail 경로 생성 복합 검색어 테스트
     * 
     * 테스트 시나리오:
     * 1. 공백과 특수문자가 포함된 검색어로 Detail 경로 생성
     * 2. URL 인코딩이 적용된 경로 반환 확인
     */
    @Test
    fun `Detail createRoute handles complex queries`() {
        // Given
        val searchQuery = "React Native & Flutter"
        
        // When
        val route = Routes.Detail.createRoute(searchQuery)
        
        // Then
        assertTrue("복합 검색어 인코딩", route.startsWith("detail/"))
        // 실제 URL 인코딩 결과를 확인하지 않고 기본 구조만 확인
        assertTrue("검색어가 포함되어야 함", route.length > "detail/".length)
        assertFalse("원본 검색어가 그대로 있으면 안됨", route.contains("React Native & Flutter"))
    }

    // =====================================
    // 🔧 URL 디코딩 Tests
    // =====================================

    /**
     * 🔓 검색어 추출 및 디코딩 기본 테스트
     * 
     * 테스트 시나리오:
     * 1. 인코딩된 검색어를 디코딩
     * 2. 원본 검색어가 올바르게 복원되는지 확인
     */
    @Test
    fun `extractSearchQuery decodes basic encoded query`() {
        // Given
        val originalQuery = "Android Development"
        val encodedQuery = URLEncoder.encode(originalQuery, "UTF-8")
        
        // When
        val decodedQuery = Routes.extractSearchQuery(encodedQuery)
        
        // Then
        assertEquals("기본 디코딩", originalQuery, decodedQuery)
    }

    /**
     * 🔓 검색어 추출 특수문자 디코딩 테스트
     * 
     * 테스트 시나리오:
     * 1. 특수문자가 인코딩된 검색어를 디코딩
     * 2. 특수문자가 올바르게 복원되는지 확인
     */
    @Test
    fun `extractSearchQuery decodes special characters`() {
        // Given
        val testCases = mapOf(
            "C++" to URLEncoder.encode("C++", "UTF-8"),
            "Node.js & React" to URLEncoder.encode("Node.js & React", "UTF-8"),
            "What?" to URLEncoder.encode("What?", "UTF-8")
        )
        
        testCases.forEach { (originalQuery, encodedQuery) ->
            // When
            val decodedQuery = Routes.extractSearchQuery(encodedQuery)
            
            // Then
            assertEquals("특수문자 '$originalQuery' 디코딩", originalQuery, decodedQuery)
        }
    }

    /**
     * 🔓 검색어 추출 유니코드 디코딩 테스트
     * 
     * 테스트 시나리오:
     * 1. 유니코드 문자가 인코딩된 검색어를 디코딩
     * 2. 다국어 문자가 올바르게 복원되는지 확인
     */
    @Test
    fun `extractSearchQuery decodes unicode characters`() {
        // Given
        val testCases = listOf(
            "안드로이드 개발",
            "プログラミング",
            "编程语言"
        )
        
        testCases.forEach { originalQuery ->
            // Given
            val encodedQuery = URLEncoder.encode(originalQuery, "UTF-8")
            
            // When
            val decodedQuery = Routes.extractSearchQuery(encodedQuery)
            
            // Then
            assertEquals("유니코드 '$originalQuery' 디코딩", originalQuery, decodedQuery)
        }
    }

    /**
     * 🚫 검색어 추출 null 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. null 입력에 대한 처리
     * 2. null 반환 확인
     */
    @Test
    fun `extractSearchQuery handles null input`() {
        // When
        val result = Routes.extractSearchQuery(null)
        
        // Then
        assertNull("null 입력은 null 반환", result)
    }

    /**
     * 🛡️ 검색어 추출 잘못된 인코딩 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. 잘못된 인코딩 문자열 입력
     * 2. 원본 문자열 fallback 확인
     */
    @Test
    fun `extractSearchQuery handles invalid encoding gracefully`() {
        // Given: 잘못된 URL 인코딩 (% 뒤에 올바르지 않은 문자)
        val invalidEncoded = "invalid%GG%encoding"
        
        // When
        val result = Routes.extractSearchQuery(invalidEncoded)
        
        // Then: 디코딩 실패 시 원본 반환
        assertEquals("잘못된 인코딩은 원본 반환", invalidEncoded, result)
    }

    /**
     * 🔄 검색어 추출 빈 문자열 처리 테스트
     * 
     * 테스트 시나리오:
     * 1. 빈 문자열 입력
     * 2. 빈 문자열 반환 확인
     */
    @Test
    fun `extractSearchQuery handles empty string`() {
        // When
        val result = Routes.extractSearchQuery("")
        
        // Then
        assertEquals("빈 문자열은 빈 문자열 반환", "", result)
    }

    // =====================================
    // 🔄 통합 Round-trip Tests
    // =====================================

    /**
     * 🔄 인코딩-디코딩 라운드트립 테스트
     * 
     * 테스트 시나리오:
     * 1. 원본 검색어 → 경로 생성 → 검색어 추출
     * 2. 원본과 추출된 검색어가 동일한지 확인
     */
    @Test
    fun `round_trip encoding and decoding preserves original query`() {
        // Given
        val testQueries = listOf(
            "Android",
            "Android Development",
            "C++ Programming",
            "Node.js & React",
            "안드로이드 개발",
            "프로그래밍 언어!",
            "What? Why & How?",
            "Multiple   Spaces"
        )
        
        testQueries.forEach { originalQuery ->
            // When: Search 경로 생성 후 검색어 추출
            val searchRoute = Routes.Search.createRoute(originalQuery)
            val extractedFromSearch = extractQueryFromRoute(searchRoute)
            val decodedFromSearch = Routes.extractSearchQuery(extractedFromSearch)
            
            // When: Detail 경로 생성 후 검색어 추출
            val detailRoute = Routes.Detail.createRoute(originalQuery)
            val extractedFromDetail = extractQueryFromRoute(detailRoute)
            val decodedFromDetail = Routes.extractSearchQuery(extractedFromDetail)
            
            // Then
            assertEquals("Search 라운드트립 - '$originalQuery'", originalQuery, decodedFromSearch)
            assertEquals("Detail 라운드트립 - '$originalQuery'", originalQuery, decodedFromDetail)
        }
    }

    /**
     * 🔧 테스트 헬퍼: 경로에서 인코딩된 검색어 추출
     */
    private fun extractQueryFromRoute(route: String): String {
        return route.substringAfter("/")
    }

    // =====================================
    // 🚨 Edge Cases Tests
    // =====================================

    /**
     * 🚨 극단적인 케이스 테스트
     * 
     * 테스트 시나리오:
     * 1. 매우 긴 검색어
     * 2. 특수한 유니코드 문자들
     * 3. 시스템 한계 테스트
     */
    @Test
    fun `handles edge cases gracefully`() {
        // Given: 매우 긴 검색어
        val longQuery = "A".repeat(1000)
        
        // When & Then: 긴 검색어 처리
        val longRoute = Routes.Search.createRoute(longQuery)
        assertTrue("긴 검색어 처리", longRoute.startsWith("search/"))
        
        val decodedLong = Routes.extractSearchQuery(longRoute.substringAfter("/"))
        assertEquals("긴 검색어 라운드트립", longQuery, decodedLong)
        
        // Given: 이모지 포함 검색어
        val emojiQuery = "Android 🤖 Development 💻"
        
        // When & Then: 이모지 처리
        val emojiRoute = Routes.Detail.createRoute(emojiQuery)
        val extractedEmoji = extractQueryFromRoute(emojiRoute)
        val decodedEmoji = Routes.extractSearchQuery(extractedEmoji)
        assertEquals("이모지 포함 검색어 처리", emojiQuery, decodedEmoji)
    }
}
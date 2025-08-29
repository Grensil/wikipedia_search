package com.grensil.navigation

import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Navigation Routes 정의
 * 모든 모듈에서 공통으로 사용하는 라우트 정보
 */
object Routes {
    const val SEARCH = "search"
    const val SEARCH_TEMPLATE = "search/{searchQuery}"
    const val DETAIL_TEMPLATE = "detail/{searchQuery}"
    
    object Search {
        /**
         * Search 화면으로 이동하는 안전한 Route 생성
         */
        fun createRoute(searchQuery: String): String {
            return createRouteWithQuery("search", searchQuery)
        }
    }
    
    object Detail {
        /**
         * Detail 화면으로 이동하는 안전한 Route 생성
         */
        fun createRoute(searchQuery: String): String {
            return createRouteWithQuery("detail", searchQuery)
        }
    }
    
    /**
     * 공통 Route 생성 유틸리티
     */
    private fun createRouteWithQuery(baseRoute: String, searchQuery: String): String {
        val encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString())
        return "$baseRoute/$encodedQuery"
    }
    
    /**
     * URL에서 검색어 추출 및 디코딩 (공통 유틸리티)
     */
    fun extractSearchQuery(encodedQuery: String?): String? {
        return encodedQuery?.let { 
            try {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } catch (e: Exception) {
                encodedQuery // fallback to original if decoding fails
            }
        }
    }
}
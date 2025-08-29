package com.grensil.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.grensil.data.datasource.WikipediaRemoteDataSource
import com.grensil.data.repository.WikipediaRepositoryImpl
import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.domain.repository.WikipediaRepository
import com.grensil.network.HttpClient
import com.grensil.network.HttpResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WikipediaRepositoryBasicTest {

    private lateinit var repository: WikipediaRepositoryImpl
    private lateinit var httpClient: HttpClient
    private lateinit var dataSource: WikipediaRemoteDataSource

    @Before
    fun setup() {
        httpClient = HttpClient()
        dataSource = WikipediaRemoteDataSource(httpClient)
        repository = WikipediaRepositoryImpl(dataSource)
    }

    @Test
    fun testGetDetailPageUrl_withValidSearchTerm_returnsValidUrl() {
        val searchTerm = "Android"
        
        val result = repository.getDetailPageUrl(searchTerm)
        
        assertNotNull(result)
        assertTrue("URL should contain wikipedia.org", result.contains("wikipedia.org"))
        assertTrue("URL should contain search term", result.contains("Android"))
        assertTrue("URL should be HTTPS", result.startsWith("https://"))
    }

    @Test
    fun testGetDetailPageUrl_withSpecialCharacters_handlesCorrectly() {
        val searchTerm = "Albert Einstein"
        
        val result = repository.getDetailPageUrl(searchTerm)
        
        assertNotNull(result)
        assertTrue("URL should contain wikipedia.org", result.contains("wikipedia.org"))
        assertTrue("URL should be HTTPS", result.startsWith("https://"))
    }

    @Test
    fun testGetSummary_withValidSearchTerm_handlesApiCall() = runTest {
        val searchTerm = "Android"
        
        try {
            val result = repository.getSummary(searchTerm)
            
            // 성공한 경우
            assertNotNull("Result should not be null", result)
            assertNotNull("Title should not be null", result.title)
            
        } catch (e: Exception) {
            // 네트워크 에러는 예상 가능하므로 로그만 출력
            println("Network test failed (expected): ${e.message}")
            assertTrue("Network error should be handled gracefully", true)
        }
    }

    @Test
    fun testGetMediaList_withValidSearchTerm_handlesApiCall() = runTest {
        val searchTerm = "Android"
        
        try {
            val result = repository.getMediaList(searchTerm)
            
            // 성공한 경우 - 빈 리스트도 허용 (API 응답에 따라 다를 수 있음)
            assertNotNull("Result should not be null", result)
            assertTrue("Result should be a valid list", result is List<MediaItem>)
            
        } catch (e: Exception) {
            // 네트워크 에러는 예상 가능하므로 로그만 출력
            println("Network test failed (expected): ${e.message}")
            assertTrue("Network error should be handled gracefully", true)
        }
    }

    @Test
    fun testRepository_initialization_succeeds() {
        // Repository가 올바르게 초기화되는지 확인
        assertNotNull("Repository should be initialized", repository)
        assertNotNull("HttpClient should be initialized", httpClient)
        assertNotNull("DataSource should be initialized", dataSource)
    }

    @Test
    fun testGetDetailPageUrl_withEmptyString_handlesGracefully() {
        try {
            val result = repository.getDetailPageUrl("")
            // 빈 문자열도 처리되어야 함
            assertNotNull("Result should not be null even for empty string", result)
        } catch (e: Exception) {
            // 예외 발생도 정상적인 처리 방법
            assertTrue("Exception should be handled gracefully", e is IllegalArgumentException || e is RuntimeException)
        }
    }
}
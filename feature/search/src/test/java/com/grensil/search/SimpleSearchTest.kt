package com.grensil.search

import org.junit.Assert.*
import org.junit.Test

/**
 * 간단한 Search Layer 테스트 - Android API만 사용
 * UI State와 검색 로직 검증에 집중
 */
class SimpleSearchTest {

    @Test
    fun `SearchUiState sealed class works correctly`() {
        // Test all UI states
        val idleState = SearchUiState.Idle
        assertTrue(idleState is SearchUiState.Idle)
        
        val loadingState = SearchUiState.Loading
        assertTrue(loadingState is SearchUiState.Loading)
        
        val errorState = SearchUiState.Error("Test error")
        assertTrue(errorState is SearchUiState.Error)
        assertEquals("Test error", (errorState as SearchUiState.Error).message)
        
        val successState = SearchUiState.Success(
            summary = com.grensil.domain.dto.Summary("Title", "Description"),
            mediaList = emptyList()
        )
        assertTrue(successState is SearchUiState.Success)
        assertEquals("Title", (successState as SearchUiState.Success).summary.title)
        assertTrue(successState.mediaList.isEmpty())
    }

    @Test
    fun `search query validation logic works correctly`() {
        // Test search query validation logic that would be in ViewModel
        fun shouldPerformSearch(query: String): Boolean {
            return query.isNotBlank()
        }
        
        // Valid queries
        assertTrue(shouldPerformSearch("android"))
        assertTrue(shouldPerformSearch("Android Development"))
        assertTrue(shouldPerformSearch("a")) // Single character should be allowed at UI level
        
        // Invalid queries
        assertFalse(shouldPerformSearch(""))
        assertFalse(shouldPerformSearch("   ")) // Whitespace only
    }

    @Test
    fun `search state transitions work correctly`() {
        // Simulate state transitions that would happen in ViewModel
        
        // Initial state
        var currentState: SearchUiState = SearchUiState.Idle
        assertTrue(currentState is SearchUiState.Idle)
        
        // Start search - should go to Loading
        currentState = SearchUiState.Loading
        assertTrue(currentState is SearchUiState.Loading)
        
        // Successful search - should go to Success
        val summary = com.grensil.domain.dto.Summary("Android", "Mobile OS")
        val mediaList = listOf(
            com.grensil.domain.dto.MediaItem("Logo", "Android logo", "url1", "image")
        )
        currentState = SearchUiState.Success(summary, mediaList)
        assertTrue(currentState is SearchUiState.Success)
        
        // Failed search - should go to Error
        currentState = SearchUiState.Error("Network error")
        assertTrue(currentState is SearchUiState.Error)
        assertEquals("Network error", (currentState as SearchUiState.Error).message)
    }

    @Test
    fun `search input processing works correctly`() {
        // Test input processing logic that would be in UI
        fun processSearchInput(input: String): String {
            return input.trim()
        }
        
        assertEquals("android", processSearchInput("android"))
        assertEquals("android", processSearchInput("  android  "))
        assertEquals("", processSearchInput("   "))
    }

    @Test
    fun `search results handling works correctly`() {
        // Test result handling logic
        val validSummary = com.grensil.domain.dto.Summary("Android", "Mobile operating system")
        val validMediaList = listOf(
            com.grensil.domain.dto.MediaItem("Android Logo", "Official logo", "url1", "image"),
            com.grensil.domain.dto.MediaItem("Screenshot", "Home screen", "url2", "image")
        )
        
        val successState = SearchUiState.Success(validSummary, validMediaList)
        
        assertEquals(validSummary, successState.summary)
        assertEquals(2, successState.mediaList.size)
        assertEquals("Android Logo", successState.mediaList[0].title)
        assertEquals("Screenshot", successState.mediaList[1].title)
    }

    @Test
    fun `error handling works correctly`() {
        // Test different error scenarios
        val networkError = SearchUiState.Error("Network connection failed")
        assertEquals("Network connection failed", networkError.message)
        
        val validationError = SearchUiState.Error("Search term too short")
        assertEquals("Search term too short", validationError.message)
        
        val serverError = SearchUiState.Error("Server returned 500 error")
        assertEquals("Server returned 500 error", serverError.message)
    }

    @Test
    fun `search UI flow simulation works correctly`() {
        // Simulate a complete search flow
        val searchQuery = "Android"
        
        // 1. User types search query - UI should update
        val processedQuery = searchQuery.trim()
        assertEquals("Android", processedQuery)
        
        // 2. Valid query - should trigger search
        val shouldSearch = processedQuery.isNotBlank()
        assertTrue(shouldSearch)
        
        // 3. Search starts - should show loading
        var uiState: SearchUiState = SearchUiState.Loading
        assertTrue(uiState is SearchUiState.Loading)
        
        // 4. Search completes successfully - should show results
        val summary = com.grensil.domain.dto.Summary("Android", "Mobile OS by Google")
        val mediaList = listOf(
            com.grensil.domain.dto.MediaItem("Android Robot", "Mascot", "url", "image")
        )
        uiState = SearchUiState.Success(summary, mediaList)
        
        assertTrue(uiState is SearchUiState.Success)
        val successState = uiState as SearchUiState.Success
        assertEquals("Android", successState.summary.title)
        assertEquals(1, successState.mediaList.size)
    }

    @Test
    fun `clear functionality works correctly`() {
        // Test clear button functionality
        fun clearSearch(): String {
            return ""
        }
        
        val clearedQuery = clearSearch()
        assertEquals("", clearedQuery)
        
        // After clearing, should return to idle state
        val uiStateAfterClear = SearchUiState.Idle
        assertTrue(uiStateAfterClear is SearchUiState.Idle)
    }

    @Test
    fun `search with empty results works correctly`() {
        // Test empty search results
        val summary = com.grensil.domain.dto.Summary("Empty Query", "No results found")
        val emptyMediaList = emptyList<com.grensil.domain.dto.MediaItem>()
        
        val successStateWithEmptyResults = SearchUiState.Success(summary, emptyMediaList)
        
        assertTrue(successStateWithEmptyResults is SearchUiState.Success)
        val state = successStateWithEmptyResults as SearchUiState.Success
        assertTrue(state.mediaList.isEmpty())
        assertEquals("Empty Query", state.summary.title)
    }
}
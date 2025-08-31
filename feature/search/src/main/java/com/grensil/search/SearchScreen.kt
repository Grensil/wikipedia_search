package com.grensil.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.grensil.navigation.Routes
import com.grensil.search.component.SearchContent
import com.grensil.search.component.SearchTextField
import com.grensil.search.component.toContentState
import com.grensil.ui.component.DismissKeyboardOnTouch

/**
 * 개선된 SearchScreen - 컴포넌트 분리로 단순화
 * - 책임 분리: 상태 관리와 UI 렌더링 분리
 * - 가독성 향상: 핵심 로직에 집중
 * - 테스트 용이성: 컴포넌트별 독립 테스트 가능
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel, navController: NavHostController) {
    // State collection
    val searchedData by viewModel.searchedData.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // UI State
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    val focusManager = LocalFocusManager.current
    var isProgrammaticScroll by rememberSaveable { mutableStateOf(false) }

    // Side effects
    LaunchedEffect(Unit) {
        viewModel.scrollToTopEvent.collect {
            Log.d("Logd","scrollToTopEvent")
            isProgrammaticScroll = true
            listState.animateScrollToItem(0)
            isProgrammaticScroll = false
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress && !isProgrammaticScroll) {
            focusManager.clearFocus()
        }
    }

    // Event handlers
    val handleBackClick: () -> Unit = {
        val previousEntry = navController.previousBackStackEntry
        if (previousEntry != null) {
            navController.popBackStack()
        }
    }

    val handleSummaryClick: (String) -> Unit = { searchTerm ->
        try {
            val route = Routes.Detail.createRoute(searchTerm)
            navController.navigate(route)
        } catch (e: Exception) {
            Log.e("SearchScreen", "Navigation failed: ${e.message}")
        }
    }

    val handleMediaItemClick: (String?) -> Unit = { keyword ->
        keyword?.takeIf { it.isNotBlank() }?.let {
            val route = Routes.Search.createRoute(it)
            navController.navigate(route)
        }
    }

    val handleRefresh: () -> Unit = {
        if (searchQuery.isNotBlank()) {
            viewModel.refreshSearch(searchQuery)
        }
    }

    // UI
    DismissKeyboardOnTouch {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = handleRefresh,
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 상단 여백
                Spacer(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth()
                )

                // 검색 입력 영역
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    SearchTextField(
                        query = searchQuery,
                        onQueryChange = viewModel::search,
                        onBackClick = handleBackClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 콘텐츠 영역
                SearchContent(
                    contentState = searchedData.toContentState(),
                    listState = listState,
                    isRefreshing = isRefreshing,
                    onSummaryClick = handleSummaryClick,
                    onMediaItemClick = handleMediaItemClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@Preview
@Composable
fun SearchScreenPreview() {

}
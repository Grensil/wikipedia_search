package com.grensil.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.navigation.Routes
import com.grensil.ui.component.CachedImage
import com.grensil.ui.component.DismissKeyboardOnTouch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel, navController: NavHostController) {

    val searchedData by viewModel.searchedData.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    val focusManager = LocalFocusManager.current

    var isProgrammaticScroll by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(searchedData) {
        when (searchedData) {
            is SearchUiState.Success -> {
                isProgrammaticScroll = true
                listState.animateScrollToItem(0)
                isProgrammaticScroll = false
            }

            else -> {}
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            if (!isProgrammaticScroll) {
                focusManager.clearFocus()
            }
        }
    }

    DismissKeyboardOnTouch {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                if (searchQuery.isNotBlank()) {
                    viewModel.refreshSearch(searchQuery)
                }
            },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    SearchTextField(
                        query = searchQuery, onQueryChange = viewModel::search, onBackClick = {
                            val previousEntry = navController.previousBackStackEntry
                            Log.d(
                                "SearchScreen", "Previous entry exists: ${previousEntry != null}"
                            )
                            Log.d(
                                "SearchScreen",
                                "Previous route: ${previousEntry?.destination?.route}"
                            )

                            if (previousEntry != null) {
                                Log.d("SearchScreen", "Popping back stack")
                                navController.popBackStack()
                            } else {
                                Log.d(
                                    "SearchScreen", "No previous entry, navigating to search"
                                )
                            }
                        }, modifier = Modifier.fillMaxWidth()
                    )
                }

                // 콘텐츠 영역
                when (searchedData) {
                    is SearchUiState.Idle -> {}
                    is SearchUiState.Loading -> {
                        if (!isRefreshing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(40.dp))
                            }
                        }
                    }

                    is SearchUiState.Success -> {
                        val data = searchedData as SearchUiState.Success
                        SearchSuccessContent(
                            summary = data.summary,
                            mediaList = data.mediaList,
                            searchQuery = searchQuery,
                            listState = listState,
                            navController = navController,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    is SearchUiState.PartialSuccess -> {
                        val data = searchedData as SearchUiState.PartialSuccess
                        SearchPartialSuccessContent(
                            partialData = data,
                            searchQuery = searchQuery,
                            listState = listState,
                            navController = navController,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    is SearchUiState.Error -> {
                        val data = searchedData as SearchUiState.Error
                        Text(
                            "Error: ${data.message}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SearchSuccessContent(
    summary: Summary,
    mediaList: List<MediaItem>,
    searchQuery: String,
    listState: LazyListState,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp)
                        .clickable {
                            try {
                                val route = Routes.Detail.createRoute(searchQuery)
                                navController.navigate(route)
                            } catch (e: Exception) {
                                Log.e("SearchScreen", "Navigation failed: ${e.message}")
                            }
                        }, verticalArrangement = Arrangement.Top
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CachedImage(
                            url = summary.thumbnailUrl,
                            modifier = Modifier
                                .width(120.dp)
                                .height(80.dp)
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                    )

                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = summary.title,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = summary.extract,
                        textAlign = TextAlign.Start,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            items(
                count = mediaList.size, key = { index ->
                    "${mediaList[index].title}_${mediaList[index].caption}_${index}"
                }) { index ->
                MediaItemView(
                    mediaItem = mediaList[index], itemOnClick = {
                        val keyword = mediaList[index].extractedKeywords ?: ""
                        if (keyword.isNotBlank()) {
                            val route = Routes.Search.createRoute(keyword)
                            navController.navigate(route)
                        }
                    })
            }
        }
    }
}

@Composable
fun SearchPartialSuccessContent(
    partialData: SearchUiState.PartialSuccess,
    searchQuery: String,
    listState: LazyListState,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Summary 섹션 (항상 있음)
        val summary = partialData.summary
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
                .clickable {
                    try {
                        val route = Routes.Detail.createRoute(searchQuery)
                        navController.navigate(route)
                    } catch (e: Exception) {
                        Log.e("SearchScreen", "Navigation failed: ${e.message}")
                    }
                }, verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CachedImage(
                    url = summary.thumbnailUrl, modifier = Modifier
                        .width(120.dp)
                        .height(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                modifier = Modifier.wrapContentSize(),
                text = summary.title,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                modifier = Modifier.wrapContentSize(),
                text = summary.extract,
                textAlign = TextAlign.Start,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        // MediaList 섹션 - 비어있을 때는 로딩 인디케이터 표시
        val mediaList = partialData.mediaList
        if (mediaList.isNotEmpty()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(
                    count = mediaList.size, key = { index ->
                        "${mediaList[index].title}_${mediaList[index].caption}_${index}"
                    }) { index ->
                    MediaItemView(
                        mediaItem = mediaList[index], itemOnClick = {
                            val keyword = mediaList[index].extractedKeywords ?: ""
                            if (keyword.isNotBlank()) {
                                val route = Routes.Search.createRoute(keyword)
                                navController.navigate(route)
                            }
                        })
                }
            }
        } else {
            // MediaList 로딩 중 인디케이터 표시
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "미디어 로딩 중...", style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun MediaItemView(mediaItem: MediaItem, itemOnClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(16.dp)
            .clickable { itemOnClick.invoke() }, verticalAlignment = Alignment.CenterVertically
    ) {
        CachedImage(url = mediaItem.imageUrl, modifier = Modifier.size(80.dp))

        Spacer(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight()
        )

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = mediaItem.title,
                modifier = Modifier.wrapContentSize(),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )

            Text(
                text = mediaItem.caption,
                modifier = Modifier.wrapContentSize(),
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

    }
}

@Composable
fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("텍스트를  입력하세요") },
        leadingIcon = {

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onBackClick.invoke()
                    }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back"
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Search, contentDescription = "검색"
                )
            }


        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear, contentDescription = "지우기"
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        )
    )
}

@Preview
@Composable
fun SearchScreenPreview() {

}
package com.grensil.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.grensil.domain.dto.MediaItem
import com.grensil.ui.component.CachedImage

@Composable
fun SearchScreen(viewModel: SearchViewModel, navController: NavHostController) {

    val searchedData by viewModel.searchedData.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val extractedKeyword by viewModel.extractedKeyword.collectAsState()

    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {

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
                    navController.popBackStack()
                }, modifier = Modifier.fillMaxWidth()
            )
        }

        when (searchedData) {
            is SearchUiState.Idle -> {}
            is SearchUiState.Loading -> CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .align(Alignment.CenterHorizontally)
            )

            is SearchUiState.Success -> {
                val data = searchedData as SearchUiState.Success

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp)
                        .clickable {
                            navController.navigate("detail/${searchQuery}") {
                                launchSingleTop = true
                            }
                        },
                    verticalArrangement = Arrangement.Top
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CachedImage(
                            url = data.summary.thumbnailUrl,
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
                        text = data.summary.title,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = data.summary.extract,
                        textAlign = TextAlign.Start,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(count = data.mediaList.size) { index ->
                        MediaItemView(
                            mediaItem = data.mediaList[index],
                            itemOnClick = {
                                viewModel.getExtractorKeyword(caption = data.mediaList[index].caption)
                            })
                    }
                }
            }

            is SearchUiState.Error -> Text(
                "Error: ${(searchedData as SearchUiState.Error).message}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}


@Composable
fun SearchContent(uiState: SearchUiState) {

}

@Composable
fun MediaItemView(mediaItem: MediaItem, itemOnClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(16.dp)
            .clickable {
                itemOnClick.invoke()
            },
        verticalAlignment = Alignment.CenterVertically
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
package com.grensil.search

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.grensil.domain.dto.MediaItem
import com.grensil.ui.component.CachedImage

@Composable
fun SearchScreen(viewModel: SearchViewModel, navController: NavHostController) {

    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val listState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {

        Spacer(modifier = Modifier
            .height(16.dp)
            .fillMaxWidth())

        Row(modifier = Modifier.fillMaxWidth()) {

            SearchTextField(
                query = searchQuery,
                onQueryChange = viewModel::search,
                onBackClick = {
                    navController.popBackStack()
                }, modifier = Modifier.fillMaxWidth()
            )
        }

        when (uiState) {
            is SearchUiState.Idle -> { }
            is SearchUiState.Loading -> CircularProgressIndicator()
            is SearchUiState.Success -> {
                val data = uiState as SearchUiState.Success

                Column(modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(vertical = 16.dp), verticalArrangement = Arrangement.Top) {

                    Row(modifier = Modifier.fillMaxWidth().wrapContentHeight(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center)  {
                        CachedImage(url = data.summary.thumbnailUrl, modifier = Modifier.width(120.dp).height(80.dp))
                    }

                    Spacer(modifier = Modifier.fillMaxWidth().height(16.dp))

                    Text(data.summary.title, modifier = Modifier.wrapContentSize(), textAlign = TextAlign.Start, fontWeight = FontWeight.Bold)

                    Text(data.summary.extract, modifier = Modifier.wrapContentSize(), textAlign = TextAlign.Start)
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(count = data.mediaList.size) { index ->

                        MediaItemView(data.mediaList[index])
                    }
                }
            }

            is SearchUiState.Error -> Text("Error: ${(uiState as SearchUiState.Error).message}")
        }
    }
}

@Composable
fun SearchContent(uiState: SearchUiState) {

}

@Composable
fun MediaItemView(mediaItem: MediaItem) {
    Row(modifier = Modifier.fillMaxWidth().height(80.dp).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        CachedImage(url = mediaItem.imageUrl, modifier = Modifier.size(60.dp))

        Spacer(modifier = Modifier.width(24.dp).fillMaxHeight())

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(mediaItem.title, modifier = Modifier.wrapContentSize(), textAlign = TextAlign.Start, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.fillMaxWidth().height(4.dp))

            Text(mediaItem.caption, modifier = Modifier.wrapContentSize(), textAlign = TextAlign.Start)
        }

    }
}

@Composable
fun SearchTextField(
    query: String, onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("텍스트를  입력하세요") },
        leadingIcon = {

            if(query.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onBackClick.invoke()
                    }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back"
                    )
                }
            }
            else {
                Icon(
                    imageVector = Icons.Default.Search, contentDescription = "검색"
                )
            }


        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange(query) }) {
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
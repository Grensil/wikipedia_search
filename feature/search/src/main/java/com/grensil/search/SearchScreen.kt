package com.grensil.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun SearchScreen(viewModel: SearchViewModel, navController: NavHostController) {

    LaunchedEffect(Unit) {
        viewModel.search("google")
    }

    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (uiState) {
            is SearchUiState.Idle -> Text("검색어를 입력하세요")
            is SearchUiState.Loading -> CircularProgressIndicator()
            is SearchUiState.Success -> {
                val data = uiState as SearchUiState.Success
                Text("Summary: ${data.summary}")
                Text("Images: ${data.mediaList.joinToString()}")
            }
            is SearchUiState.Error -> Text("Error: ${(uiState as SearchUiState.Error).message}")
        }
    }

}

@Preview
@Composable
fun SearchScreenPreview() {

    Scaffold(modifier = Modifier.fillMaxSize()) {  }
}
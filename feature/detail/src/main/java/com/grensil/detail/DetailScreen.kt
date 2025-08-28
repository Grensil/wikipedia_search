package com.grensil.detail

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController

@Composable
fun DetailScreen(
    viewModel: DetailViewModel, navController: NavHostController, keyword: String? = null
) {

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getDetailPageUrl(keyword = keyword ?: "")
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {

        Spacer(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center // Box 안에서 수평+수직 중앙
        ) {
            Row(
                modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back"
                    )
                }
            }

            Text(
                text = keyword ?: "",
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        when (uiState) {
            is DetailUiState.Idle -> { CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .align(Alignment.CenterHorizontally)
            )}
            is DetailUiState.Loading -> CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp)
                    .align(Alignment.CenterHorizontally)
            )

            is DetailUiState.Success -> {
                WebPage(url = (uiState as DetailUiState.Success).webUrl)
            }

            is DetailUiState.Error -> {
                Text(
                    "Error: ${(uiState as DetailUiState.Error).message}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPage(url: String) {
    AndroidView(
        factory = { context ->
        WebView(context).apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            loadUrl(url)
        }
    }, update = { webView ->
        webView.loadUrl(url)
    }, modifier = Modifier.fillMaxSize()
    )
}
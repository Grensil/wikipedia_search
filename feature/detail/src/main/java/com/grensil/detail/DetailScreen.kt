package com.grensil.detail

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    LaunchedEffect(keyword) {
        if (!keyword.isNullOrBlank()) {
            viewModel.getDetailPageUrl(keyword = keyword)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 고정 헤더 - 스크롤에 영향받지 않음
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val previousEntry = navController.previousBackStackEntry


                            if (previousEntry != null) {
                                navController.popBackStack()
                            }
                        }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }

                    Text(
                        text = keyword ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // 오른쪽 공간을 위한 Spacer
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            // 컨텐츠 영역
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    is DetailUiState.Idle -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(16.dp)
                                .size(40.dp)
                                .align(Alignment.Center)
                        )
                    }

                    is DetailUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(16.dp)
                                .size(40.dp)
                                .align(Alignment.Center)
                        )
                    }

                    is DetailUiState.Success -> {
                        WebPage(url = (uiState as DetailUiState.Success).webUrl, navController)
                    }

                    is DetailUiState.Error -> {
                        Text(
                            "Error: ${(uiState as DetailUiState.Error).message}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPage(url: String, navController: NavHostController) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Compose 백버튼 처리
    BackHandler(enabled = true) {
        webViewRef?.let { webView ->
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                val previousEntry = navController.previousBackStackEntry

                if (previousEntry != null) {
                    navController.popBackStack()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    loadUrl(url)
                    webViewRef = this
                }
            },
            update = { webView ->
                webView.loadUrl(url)
                webViewRef = webView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
package com.grensil.search.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary

/**
 * 검색 결과 콘텐츠 컴포넌트
 * - 상태별 UI 렌더링 로직 분리
 * - 재사용성과 테스트 용이성 향상
 */
@Composable
fun SearchContent(
    contentState: SearchContentState,
    listState: LazyListState,
    isRefreshing: Boolean,
    onSummaryClick: (String) -> Unit,
    onMediaItemClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    when (contentState) {
        is SearchContentState.Idle -> {
            // 초기 상태 - 빈 화면
        }
        
        is SearchContentState.Loading -> {
            if (!isRefreshing) {
                LoadingIndicator(modifier = modifier)
            }
        }
        
        is SearchContentState.Success -> {
            SuccessContent(
                summary = contentState.summary,
                mediaList = contentState.mediaList,
                listState = listState,
                onSummaryClick = onSummaryClick,
                onMediaItemClick = onMediaItemClick,
                modifier = modifier
            )
        }
        
        is SearchContentState.Error -> {
            ErrorContent(
                message = contentState.message,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun ErrorContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "오류: $message",
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
private fun SuccessContent(
    summary: Summary,
    mediaList: List<MediaItem>,
    listState: LazyListState,
    onSummaryClick: (String) -> Unit,
    onMediaItemClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 요약 카드
        item {
            SummaryCard(
                summary = summary,
                onClick = { onSummaryClick(summary.title) }
            )
        }

        // 미디어 아이템들
        items(
            count = mediaList.size,
            key = { index -> 
                "${mediaList[index].title}_${mediaList[index].caption}_$index"
            }
        ) { index ->
            val mediaItem = mediaList[index]
            MediaItemCard(
                mediaItem = mediaItem,
                onClick = { 
                    onMediaItemClick(mediaItem.extractedKeywords)
                }
            )
        }
    }
}
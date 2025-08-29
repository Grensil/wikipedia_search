package com.grensil.search.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grensil.domain.dto.Summary
import com.grensil.ui.component.CachedImage

/**
 * 재사용 가능한 요약 카드 컴포넌트
 * - 단일 책임: Summary 표시만 담당
 * - 재사용성: 다른 화면에서도 사용 가능
 * - 커스터마이징 가능: modifier, onClick, 이미지 크기 등
 */
@Composable
fun SummaryCard(
    summary: Summary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageWidth: Int = 120,
    imageHeight: Int = 80,
    maxExtractLines: Int = 3
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // 이미지 섹션 (중앙 정렬)
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
                    .width(imageWidth.dp)
                    .height(imageHeight.dp)
            )
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        )

        // 제목
        Text(
            modifier = Modifier.wrapContentSize(),
            text = summary.title,
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 설명 (추출 텍스트)
        if (summary.extract.isNotBlank()) {
            Text(
                modifier = Modifier.wrapContentSize(),
                text = summary.extract,
                textAlign = TextAlign.Start,
                maxLines = maxExtractLines,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
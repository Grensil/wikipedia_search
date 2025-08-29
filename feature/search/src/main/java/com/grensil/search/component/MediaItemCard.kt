package com.grensil.search.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grensil.domain.dto.MediaItem
import com.grensil.ui.component.CachedImage

/**
 * 재사용 가능한 미디어 아이템 카드 컴포넌트
 * - 단일 책임: MediaItem 표시만 담당
 * - 재사용성: 다른 리스트에서도 사용 가능
 * - 커스터마이징 가능: modifier, onClick 등
 */
@Composable
fun MediaItemCard(
    mediaItem: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageSize: Int = 80,
    showSubtitle: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 이미지 섹션
        CachedImage(
            url = mediaItem.imageUrl, 
            modifier = Modifier.size(imageSize.dp)
        )

        Spacer(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight()
        )

        // 텍스트 섹션
        Column(
            modifier = Modifier.weight(1f), 
            verticalArrangement = Arrangement.Center
        ) {
            // 제목
            Text(
                text = mediaItem.title,
                modifier = Modifier.wrapContentSize(),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 부제목 (옵셔널)
            if (showSubtitle && mediaItem.caption.isNotBlank()) {
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
}
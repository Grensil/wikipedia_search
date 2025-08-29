package com.grensil.data.mapper

import com.grensil.data.entity.MediaListEntity
import com.grensil.data.entity.SummaryEntity
import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary

object WikipediaMapper {

    /**
     * SummaryDto -> Summary 변환
     */
    fun mapToSummary(dto: SummaryEntity): Summary {
        return Summary(
            title = dto.title ?: "",
            description = dto.description ?: dto.extract ?: "",
            thumbnailUrl = dto.thumbnail?.source,
            originalImageUrl = dto.originalimage?.source,
            pageId = dto.pageid ?: 0,
            extract = dto.extract ?: "",
            timestamp = dto.timestamp
        )
    }

    /**
     * MediaListDto -> List<MediaItem> 변환
     */
    fun mapToMediaItemList(dto: MediaListEntity): List<MediaItem> {
        return dto.items?.mapNotNull { itemDto ->
            mapToMediaItem(itemDto)
        } ?: emptyList()
    }

    /**
     * MediaItemDto -> MediaItem 변환
     */
    private fun mapToMediaItem(entity: MediaListEntity.MediaItemEntity): MediaItem? {
        // 필수 필드 검증
        val title = entity.title
        if (title.isNullOrBlank()) return null

        return MediaItem(
            title = title,
            caption = entity.caption?.text ?: "",
            extractedKeywords = null,
            imageUrl = entity.srcset?.firstOrNull()?.src,
            type = entity.type ?: "unknown"
        )
    }
}
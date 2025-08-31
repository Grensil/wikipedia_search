package com.grensil.data.datasource

import com.grensil.data.entity.MediaListEntity
import com.grensil.data.entity.SummaryEntity
import com.grensil.data.mapper.WikipediaMapper
import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.network.HttpClient
import com.grensil.network.NhnNetworkException

/**
 * Wikipedia API 원격 데이터 소스
 */
class WikipediaRemoteDataSource(
    private val httpClient: HttpClient
) {

    companion object {
        private const val BASE_URL = "https://en.wikipedia.org/api/rest_v1/page"
        private const val SUMMARY_URL = "$BASE_URL/summary"
        private const val MEDIA_LIST_URL = "$BASE_URL/media-list"
        private const val HTML_URL = "$BASE_URL/html"
    }

    /**
     * 요약 정보 조회
     */
    suspend fun getSummary(searchTerm: String): Summary {
        try {
            val url = "$SUMMARY_URL/${searchTerm.trim()}"
            val response = httpClient.get(
                url = url,
                headers = mapOf(
                    "Accept" to "application/json",
                    "User-Agent" to "NHN-Assignment-App/1.0"
                )
            )

            // JSON 수동 파싱 (Android 기본 API만 사용)
            val summaryDto = parseJsonToSummary(response.body)
            return WikipediaMapper.mapToSummary(summaryDto)

        } catch (e: NhnNetworkException) {
            throw e // 네트워크 예외는 그대로 전파
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * 미디어 목록 조회
     */
    suspend fun getMediaList(searchTerm: String): List<MediaItem> {
        try {
            val url = "$MEDIA_LIST_URL/${searchTerm.trim()}"
            val response = httpClient.get(
                url = url,
                headers = mapOf(
                    "Accept" to "application/json",
                    "User-Agent" to "NHN-Assignment-App/1.0"
                )
            )

            // JSON 수동 파싱 (Android 기본 API만 사용)
            val mediaListDto = parseJsonToMediaList(response.body)

            return WikipediaMapper.mapToMediaItemList(mediaListDto)

        } catch (e: NhnNetworkException) {
            throw e // 네트워크 예외는 그대로 전파
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * 상세 페이지 HTML URL 생성
     */
    fun getDetailHtmlUrl(searchTerm: String): String {
        return "$HTML_URL/${searchTerm.trim()}"
    }

    /**
     * JSON을 SummaryDto로 수동 파싱 (순수 문자열 파싱으로 JSONObject 의존성 제거)
     */
    private fun parseJsonToSummary(jsonString: String): SummaryEntity {
        try {
            // Thumbnail 파싱
            val thumbnail = extractNestedObject(jsonString, "thumbnail")?.let { thumbJson ->
                SummaryEntity.ThumbnailEntity(
                    source = extractStringValue(thumbJson, "source"),
                    width = extractIntValue(thumbJson, "width"),
                    height = extractIntValue(thumbJson, "height")
                )
            }

            // Original Image 파싱
            val originalImage = extractNestedObject(jsonString, "originalimage")?.let { imgJson ->
                SummaryEntity.OriginalImageEntity(
                    source = extractStringValue(imgJson, "source"),
                    width = extractIntValue(imgJson, "width"),
                    height = extractIntValue(imgJson, "height")
                )
            }

            return SummaryEntity(
                type = extractStringValue(jsonString, "type"),
                title = extractStringValue(jsonString, "title"),
                displaytitle = extractStringValue(jsonString, "displaytitle"),
                pageid = extractIntValue(jsonString, "pageid"),
                extract = extractStringValue(jsonString, "extract"),
                extractHtml = extractStringValue(jsonString, "extract_html"),
                thumbnail = thumbnail,
                originalimage = originalImage,
                lang = extractStringValue(jsonString, "lang"),
                dir = extractStringValue(jsonString, "dir"),
                timestamp = extractStringValue(jsonString, "timestamp"),
                description = extractStringValue(jsonString, "description")
            )

        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * JSON을 MediaListDto로 수동 파싱 (순수 문자열 파싱으로 JSONObject 의존성 제거)
     */

    private fun parseJsonToMediaList(jsonString: String): MediaListEntity {
        try {
            val jsonObject = org.json.JSONObject(jsonString)
            val items = mutableListOf<MediaListEntity.MediaItemEntity>()

            if (jsonObject.has("items")) {
                val itemsArray = jsonObject.getJSONArray("items")

                for (i in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.getJSONObject(i)

                    // Caption 파싱
                    val caption = if (itemObj.has("caption")) {
                        val captionObj = itemObj.getJSONObject("caption")
                        MediaListEntity.MediaItemEntity.CaptionEntity(
                            text = captionObj.optString("text").takeIf { it.isNotEmpty() },
                            html = captionObj.optString("html").takeIf { it.isNotEmpty() }
                        )
                    } else null

                    // SrcSet 파싱
                    val srcSet = if (itemObj.has("srcset")) {
                        val srcSetArray = itemObj.getJSONArray("srcset")
                        val srcList = mutableListOf<MediaListEntity.MediaItemEntity.SrcSetEntity>()

                        for (j in 0 until srcSetArray.length()) {
                            val srcObj = srcSetArray.getJSONObject(j)
                            srcList.add(
                                MediaListEntity.MediaItemEntity.SrcSetEntity(
                                    src = srcObj.optString("src").takeIf { it.isNotEmpty() },
                                    scale = srcObj.optString("scale").takeIf { it.isNotEmpty() }
                                )
                            )
                        }
                        srcList
                    } else emptyList()

                    items.add(
                        MediaListEntity.MediaItemEntity(
                            title = itemObj.optString("title").takeIf { it.isNotEmpty() },
                            section_id = itemObj.optInt("section_id").takeIf { it != 0 },
                            type = itemObj.optString("type").takeIf { it.isNotEmpty() },
                            caption = caption,
                            srcset = srcSet
                        )
                    )
                }
            }

            return MediaListEntity(items = items)

        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * SrcSet 배열을 간단히 추출하는 헬퍼 함수
     */
    private fun extractSrcSet(itemJson: String): List<MediaListEntity.MediaItemEntity.SrcSetEntity> {
        val srcSetPattern = """"src"\s*:\s*"([^"]*)"""".toRegex()
        val scalePattern = """"scale"\s*:\s*"([^"]*)"""".toRegex()

        val srcMatches = srcSetPattern.findAll(itemJson).map { it.groupValues[1] }.toList()
        val scaleMatches = scalePattern.findAll(itemJson).map { it.groupValues[1] }.toList()

        return srcMatches.zip(scaleMatches) { src, scale ->
            MediaListEntity.MediaItemEntity.SrcSetEntity(src = src, scale = scale)
        }
    }

    // =================================
    // 순수 문자열 JSON 파싱 유틸리티
    // =================================

    /**
     * JSON 문자열에서 문자열 값 추출
     */
    private fun extractStringValue(json: String, key: String): String? {
        val pattern = """"$key"\s*:\s*"([^"]*)""""
        val match = Regex(pattern).find(json)
        return match?.groupValues?.get(1)?.takeIf { it.isNotEmpty() }
    }

    /**
     * JSON 문자열에서 정수 값 추출
     */
    private fun extractIntValue(json: String, key: String): Int? {
        val pattern = """"$key"\s*:\s*(\d+)"""
        val match = Regex(pattern).find(json)
        return match?.groupValues?.get(1)?.toIntOrNull()?.takeIf { it != 0 }
    }

    /**
     * JSON 문자열에서 중첩 객체 추출
     */
    private fun extractNestedObject(json: String, key: String): String? {
        val pattern = """"$key"\s*:\s*(\{[^{}]*(?:\{[^{}]*\}[^{}]*)*\})"""
        val match = Regex(pattern).find(json)
        return match?.groupValues?.get(1)
    }

}
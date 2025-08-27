package com.grensil.data.datasource

import com.grensil.data.mapper.WikipediaMapper
import com.grensil.data.entity.MediaListEntity
import com.grensil.data.entity.SummaryEntity
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
     * JSON을 SummaryDto로 수동 파싱 (Android 기본 JSONObject 사용)
     */
    private fun parseJsonToSummary(jsonString: String): SummaryEntity {
        try {
            val jsonObject = org.json.JSONObject(jsonString)

            // Thumbnail 파싱
            val thumbnail = if (jsonObject.has("thumbnail")) {
                val thumbObj = jsonObject.getJSONObject("thumbnail")
                SummaryEntity.ThumbnailEntity(
                    source = thumbObj.optString("source").takeIf { it.isNotEmpty() },
                    width = thumbObj.optInt("width").takeIf { it != 0 },
                    height = thumbObj.optInt("height").takeIf { it != 0 }
                )
            } else null

            // Original Image 파싱
            val originalImage = if (jsonObject.has("originalimage")) {
                val imgObj = jsonObject.getJSONObject("originalimage")
                SummaryEntity.OriginalImageEntity(
                    source = imgObj.optString("source").takeIf { it.isNotEmpty() },
                    width = imgObj.optInt("width").takeIf { it != 0 },
                    height = imgObj.optInt("height").takeIf { it != 0 }
                )
            } else null

            return SummaryEntity(
                type = jsonObject.optString("type").takeIf { it.isNotEmpty() },
                title = jsonObject.optString("title").takeIf { it.isNotEmpty() },
                displaytitle = jsonObject.optString("displaytitle").takeIf { it.isNotEmpty() },
                pageid = jsonObject.optInt("pageid").takeIf { it != 0 },
                extract = jsonObject.optString("extract").takeIf { it.isNotEmpty() },
                extractHtml = jsonObject.optString("extract_html").takeIf { it.isNotEmpty() },
                thumbnail = thumbnail,
                originalimage = originalImage,
                lang = jsonObject.optString("lang").takeIf { it.isNotEmpty() },
                dir = jsonObject.optString("dir").takeIf { it.isNotEmpty() },
                timestamp = jsonObject.optString("timestamp").takeIf { it.isNotEmpty() },
                description = jsonObject.optString("description").takeIf { it.isNotEmpty() }
            )

        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * JSON을 MediaListDto로 수동 파싱 (Android 기본 JSONObject 사용)
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
}
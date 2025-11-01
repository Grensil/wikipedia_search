package com.grensil.data.repository

import android.util.Log
import com.grensil.data.datasource.WikipediaRemoteDataSource
import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.domain.repository.WikipediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Wikipedia Repository 구현체
 */
class WikipediaRepositoryImpl(
    private val remoteDataSource: WikipediaRemoteDataSource
) : WikipediaRepository {

    override suspend fun getSummary(searchTerm: String): Summary {
        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            Log.d("Repository", "[getSummary] 시작 - ${getCurrentTime()}")

            // 실제 API 호출 (또는 시뮬레이션)
            delay(3000) // 3초 걸리는 API 시뮬레이션
            val result = remoteDataSource.getSummary(searchTerm)

            val elapsed = System.currentTimeMillis() - startTime
            Log.d("Repository", "[getSummary] 완료 - ${getCurrentTime()} (${elapsed}ms 소요)")

            result
        }
    }

    override suspend fun getMediaList(searchTerm: String): List<MediaItem> {
        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            Log.d("Repository", "[getMediaList] 시작 - ${getCurrentTime()}")

            // 실제 API 호출 (또는 시뮬레이션)
            delay(2000) // 2초 걸리는 API 시뮬레이션
            val result = remoteDataSource.getMediaList(searchTerm)

            val elapsed = System.currentTimeMillis() - startTime
            Log.d("Repository", "[getMediaList] 완료 - ${getCurrentTime()} (${elapsed}ms 소요)")

            result
        }
    }

    override fun getDetailPageUrl(searchTerm: String): String {
        return remoteDataSource.getDetailHtmlUrl(searchTerm)
    }

    private fun getCurrentTime(): String {
        val format = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        return format.format(Date())
    }
}
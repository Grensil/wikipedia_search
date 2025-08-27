package com.grensil.data.repository

import com.grensil.data.datasource.WikipediaRemoteDataSource
import com.grensil.domain.dto.MediaItem
import com.grensil.domain.dto.Summary
import com.grensil.domain.repository.WikipediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wikipedia Repository 구현체
 */
class WikipediaRepositoryImpl(
    private val remoteDataSource: WikipediaRemoteDataSource
) : WikipediaRepository {

    override suspend fun getSummary(searchTerm: String): Summary {
        return withContext(Dispatchers.IO) {
            remoteDataSource.getSummary(searchTerm)
        }
    }

    override suspend fun getMediaList(searchTerm: String): List<MediaItem> {
        return withContext(Dispatchers.IO) {
            remoteDataSource.getMediaList(searchTerm)
        }
    }

    override fun getDetailPageUrl(searchTerm: String): String {
        return remoteDataSource.getDetailHtmlUrl(searchTerm)
    }
}
package com.grensil.nhn_gmail

import android.app.Application
import com.grensil.data.datasource.WikipediaRemoteDataSource
import com.grensil.data.repository.WikipediaRepositoryImpl
import com.grensil.domain.repository.WikipediaRepository
import com.grensil.domain.usecase.GetSummaryUseCase
import com.grensil.network.HttpClient
import com.grensil.nhn_gmail.di.WikipediaModule

class NhnApplication : Application() {

    private lateinit var wikipediaModule: WikipediaModule

    override fun onCreate() {
        super.onCreate()
        val httpClient = HttpClient()
        wikipediaModule = WikipediaModule(httpClient)
    }

    fun getWikipediaModule(): WikipediaModule = wikipediaModule
}
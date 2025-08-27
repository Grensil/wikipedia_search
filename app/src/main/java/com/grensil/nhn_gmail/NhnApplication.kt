package com.grensil.nhn_gmail

import android.app.Application
import com.grensil.network.HttpClient
import com.grensil.nhn_gmail.di.AppModule

class NhnApplication : Application() {

    private lateinit var wikipediaModule: AppModule

    override fun onCreate() {
        super.onCreate()
        val httpClient = HttpClient()
        wikipediaModule = AppModule(httpClient)
    }

    fun getWikipediaModule(): AppModule = wikipediaModule
}
package com.grensil.nhn_gmail

import android.app.Application
import com.grensil.network.HttpClient
import com.grensil.nhn_gmail.di.AppModule

class NhnApplication : Application() {

    private val appModule: AppModule by lazy {
        AppModule.getInstance(HttpClient())
    }

    override fun onCreate() {
        super.onCreate()
        // AppModule은 lazy로 초기화되므로 별도 초기화 불필요
    }

    fun getWikipediaModule(): AppModule = appModule
}
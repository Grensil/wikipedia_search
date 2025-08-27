package com.grensil.data.di

/**
 * Data 레이어 Manual DI Module
 */
object DataModule {

    /**
     * Data 레이어 의존성 초기화
     */
    fun initialize() {
        // HttpClient 등록 (아직 없는 경우에만)
        if (!DependencyContainer.contains<HttpClient>()) {
            DependencyContainer.register(HttpClient())
        }

        // Remote DataSource 등록
        if (!DependencyContainer.contains<WikipediaRemoteDataSource>()) {
            val httpClient = DependencyContainer.get<HttpClient>()
            DependencyContainer.register(WikipediaRemoteDataSource(httpClient))
        }

        // Repository 등록
        if (!DependencyContainer.contains<WikipediaRepository>()) {
            val remoteDataSource = DependencyContainer.get<WikipediaRemoteDataSource>()
            DependencyContainer.register<WikipediaRepository>(
                WikipediaRepositoryImpl(remoteDataSource)
            )
        }
    }

    /**
     * Repository 제공 (편의 메서드)
     */
    fun provideWikipediaRepository(): WikipediaRepository {
        initialize()
        return DependencyContainer.get()
    }

    /**
     * Remote DataSource 제공 (테스트용)
     */
    fun provideWikipediaRemoteDataSource(): WikipediaRemoteDataSource {
        initialize()
        return DependencyContainer.get()
    }

    /**
     * HttpClient 제공 (테스트용)
     */
    fun provideHttpClient(): HttpClient {
        initialize()
        return DependencyContainer.get()
    }
}
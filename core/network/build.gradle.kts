plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.grensil.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    
    buildFeatures {
        buildConfig = true
    }
}


// 컴파일된 클래스와 소스코드를 모두 포함한 단일 JAR 생성
tasks.register<Jar>("packageJar") {
    archiveBaseName.set("nhn-http-client")
    archiveVersion.set("1.0.0")
    dependsOn("compileReleaseKotlin")
    
    // 컴파일된 클래스 파일들 포함
    from("${layout.buildDirectory.get()}/tmp/kotlin-classes/release")
    
    // 소스코드도 함께 포함 (선택사항)
    // from(android.sourceSets.getByName("main").java.srcDirs)
    
    // Manifest 정보 추가
    manifest {
        attributes(
            "Implementation-Title" to "NHN HTTP Client",
            "Implementation-Version" to "1.0.0",
            "Implementation-Vendor" to "NHN Corp"
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.nhn.core"
            artifactId = "network"
            version = "1.0.0"

            // 단일 JAR만 배포
            artifact(tasks.named("packageJar"))
            
            // POM 파일에 의존성 정보 포함
            pom {
                name.set("NHN HTTP Client")
                description.set("HTTP client library for NHN projects")
                
                // 의존성이 있다면 여기에 추가
            }
        }
    }
}

dependencies {

    // Test dependencies - Android API only
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
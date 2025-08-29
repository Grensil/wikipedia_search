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


tasks.register<Jar>("packageJar") {
    archiveBaseName.set("nhn-http-client")
    archiveVersion.set("1.0.0")
    from(android.sourceSets.getByName("main").java.srcDirs)
    include("**/*.kt")
    include("**/*.java")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.nhn.core"
            artifactId = "network"
            version = "1.0.0"

            artifact(tasks.named("packageJar"))
        }
    }
}

dependencies {

    // Test dependencies - Android API only
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
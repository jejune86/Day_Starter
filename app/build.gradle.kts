import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.day_starter"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.day_starter"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "WEATHER_API_KEY", "\"${gradleLocalProperties(rootDir, providers).getProperty("WEATHER_API_KEY")}\"")
        buildConfigField("String", "NEWS_API_KEY", "\"${gradleLocalProperties(rootDir, providers).getProperty("NEWS_API_KEY")}\"")

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // GSON Converter
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.6")

    implementation("com.google.android.gms:play-services-location:21.2.0")

//    // YouTube API 관련 종속성 수정
//    implementation("com.google.api-client:google-api-client-android:1.32.1") {
//        exclude(group = "org.apache.httpcomponents")
//        exclude(group = "com.google.guava", module = "guava-jdk5")
//    }
//    implementation("com.google.apis:google-api-services-youtube:v3-rev222-1.25.0")
//
//    // 필수 종속성
//    implementation("com.google.oauth-client:google-oauth-client:1.32.1")
//    implementation("com.google.http-client:google-http-client-android:1.32.1")
//    implementation("com.google.http-client:google-http-client-gson:1.32.1")
//    implementation("com.google.guava:guava:31.1-android")

    
}

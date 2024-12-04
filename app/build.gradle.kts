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
        buildConfigField("String", "OAUTH_CLIENT_ID", "\"${gradleLocalProperties(rootDir, providers).getProperty("OAUTH_CLIENT_ID")}\"")
        manifestPlaceholders["appAuthRedirectScheme"] = "com.example.day_starter"
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

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
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

    implementation("androidx.credentials:credentials:1.2.2")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    implementation("net.openid:appauth:0.11.1")

    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
}

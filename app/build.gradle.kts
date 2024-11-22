plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.seefood"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.seefood"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    packagingOptions {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildToolsVersion = "35.0.0"
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.mediarouter)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.google.api.client)
    implementation(libs.google.oauth.client.jetty)
    implementation(libs.google.http.client.jackson2)
    implementation(libs.google.api.client)
    implementation(libs.play.services.auth)
    implementation(libs.google.oauth.client.jetty)
    implementation(libs.google.http.client.jackson2)
    implementation(libs.google.api.services.oauth2)
}
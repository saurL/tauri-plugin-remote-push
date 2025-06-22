plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "app.tauri.remotepush"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    // The core Tauri Android libraries are bundled with the plugin to ensure
    // they are available at compile time. This is a workaround for dependency
    // resolution issues in some environments.
    implementation("app.tauri:core-android:0.5.2")
    // Dependency for Firebase Cloud Messaging, used in the plugin's Kotlin code
    implementation("com.google.firebase:firebase-messaging:23.4.1")
} 
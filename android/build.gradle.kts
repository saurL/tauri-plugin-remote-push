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
    // The core Tauri Android libraries are provided by the parent project,
    // so we use compileOnly to avoid bundling them.
    compileOnly(project(":core-android"))
    // Dependency for Firebase Cloud Messaging, used in the plugin's Kotlin code
    implementation("com.google.firebase:firebase-messaging:23.4.1")
} 
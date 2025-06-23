# Tauri Plugin: Remote Push Notifications

A plugin for Tauri v2 that enables applications to receive remote push notifications via Firebase Cloud Messaging (FCM) on Android and Apple Push Notification Service (APNs) on iOS.

This plugin is self-contained and handles its own native dependencies. However, you must still perform some **manual modification of your native host application code** to integrate the necessary notification services.

## Prerequisites

- A working Tauri v2 project.
- A Firebase project for Android.
- An Apple Developer account with push notification capabilities for iOS.
- You must have generated the native mobile projects by running `tauri android init` and `tauri ios init`.

## Setup

### 1. Install Plugin Package

```sh
# Add the rust part
cargo add tauri-plugin-remote-push
```

```sh
# Add the javascript part
npm install tauri-plugin-remote-push-api
# or
yarn add tauri-plugin-remote-push-api
# or
pnpm add tauri-plugin-remote-push-api
# or
bun add tauri-plugin-remote-push-api
```

### 2. Register the Plugin

You must register the plugin with Tauri in your `src-tauri/src/lib.rs` file:

```rust
// src-tauri/src/lib.rs
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_remote_push::init())
        // ... other setup
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
```

### 3. Configure the Plugin (Android)

In your `src-tauri/tauri.conf.json` file, you must add the plugin configuration and provide your Firebase **Sender ID**. This allows the plugin to identify your application with Google's messaging services.

You can find your Sender ID in the Firebase Console under **Project settings > Cloud Messaging**.

```json
// src-tauri/tauri.conf.json
{
  "plugins": {
    "remote-push": {
      "senderId": "YOUR_SENDER_ID_HERE"
    }
  }
}
```

---

## Platform-Specific Configuration

This is the **critical manual step** required to make the plugin functional.

### iOS Configuration

1.  **Add Capabilities in Xcode**: Open your `src-tauri/gen/apple/app.xcodeproj` project in Xcode.
    *   Select the root project, then your app target.
    *   Go to the "Signing & Capabilities" tab.
    *   Click `+ Capability` and add **Push Notifications**.
    *   Click `+ Capability` again and add **Background Modes**. In the expanded section, check **Remote notifications**.

2.  **Modify your AppDelegate**: Open `src-tauri/src/ios/app/AppDelegate.swift` and make the following changes to register for notifications and forward them to the plugin.

    ```swift
    import UIKit
    import Tauri
    import UserNotifications // 1. Import UserNotifications
    import tauri_plugin_remote_push // 2. Import your plugin

    class AppDelegate: TauriAppDelegate {
      override func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // 3. Set the notification center delegate
        UNUserNotificationCenter.current().delegate = self

        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
      }

      // 4. Add the token registration handlers
      override func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        PushNotificationPlugin.instance?.handleToken(deviceToken)
      }

      override func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("Failed to register for remote notifications: \(error.localizedDescription)")
      }

      // 5. Add the notification-handling delegate method
      override func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        PushNotificationPlugin.instance?.handleNotification(notification.request.content.userInfo)
        // You can customize the presentation options here
        completionHandler([.banner, .sound, .badge])
      }
    }
    ```

### Android Configuration

This section is **critical** for Android to function. If you misconfigure this, your app will fail to initialize Firebase and will likely show a **blank white screen** on startup.

**1. Configure Gradle**

You need to add the Google Services plugin to your Android project's Gradle configuration. Your project may use the modern Kotlin `build.gradle.kts` syntax or the older Groovy `build.gradle` syntax. Make sure you edit the correct files.

**A) Project-Level Gradle File**

This file is located at `src-tauri/gen/android/[YOUR_APP_NAME]/build.gradle.kts` (or `.gradle`).

*If you have a `build.gradle.kts` (Kotlin) file:*
```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    // 1. Add this line
    id("com.google.gms.google-services") version "4.4.1" apply false
}
```

*If you have a `build.gradle` (Groovy) file:*
```groovy
buildscript {
    repositories {
        // Make sure you have google() here
        google()
        mavenCentral()
    }
    dependencies {
        // ... other classpaths
        // 1. Add this line
        classpath 'com.google.gms:google-services:4.4.1'
    }
}
```

**B) App-Level Gradle File**

This file is located at `src-tauri/gen/android/[YOUR_APP_NAME]/app/build.gradle.kts` (or `.gradle`).

*If you have a `build.gradle.kts` (Kotlin) file:*
```kotlin
// 1. Add this block at the top of the file
plugins {
    id("com.google.gms.google-services")
}

// ... rest of the file
android {
    // ...
}
```

*If you have a `build.gradle` (Groovy) file:*
```groovy
// 1. Add this line at the very top of the file
apply plugin: 'com.google.gms.google-services'

android {
    // ...
}
```

**2. Add `google-services.json`**

This step is the same for all projects.

*   Go to your Firebase project settings. In the "General" tab, under "Your apps", select your Android application.
*   Download the `google-services.json` file.
*   Place this file in your app's module directory: `src-tauri/gen/android/[YOUR_APP_NAME]/app/`.

**3. Register the Notification Service**

Open `src-tauri/gen/android/[YOUR_APP_NAME]/app/src/main/AndroidManifest.xml` and register the `FCMService` inside the `<application>` tag. This allows your app to receive notifications when it's in the background.

```xml
<application ...>
    ...
    <service
        android:name="app.tauri.remotepush.FCMService"
        android:exported="false">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
    </service>
    ...
</application>
```

---

## API

```typescript
import {
  getToken,
  requestPermission,
  onNotificationReceived,
  onTokenRefresh
} from 'tauri-plugin-remote-push-api';

// Request user permission for notifications
const permission = await requestPermission();
if (permission.granted) {
  // Get the device token
  const token = await getToken();
  console.log('Device token:', token);
}

// Listen for incoming notifications
const unsubscribe = await onNotificationReceived((notification) => {
  console.log('Received notification:', notification);
});

// Listen for token refreshes
const unsubscribeToken = await onTokenRefresh((token) => {
  console.log('Token refreshed:', token);
});
```

## License

This project is licensed under the MIT License.

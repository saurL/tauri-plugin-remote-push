# Tauri Plugin: Remote Push Notifications

A plugin for Tauri v2 that enables applications to receive remote push notifications via Firebase Cloud Messaging (FCM) on Android and Apple Push Notification Service (APNs) on iOS.

Due to the security sandboxing of the Tauri plugin system, a fully automated setup is not possible. This plugin requires **manual modification of your native host application code** to integrate the necessary notification services.

## Prerequisites

- A working Tauri v2 project.
- A Firebase project for Android.
- An Apple Developer account with push notification capabilities for iOS.

## Setup

There are two parts to the setup: installing the plugin package and configuring the native host project.

### 1. Install Plugin Package

Add the plugin to your `Cargo.toml` and install the JavaScript package:

```sh
cargo add tauri-plugin-remote-push
```

```sh
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

---

## Platform-Specific Configuration

This is the **critical manual step** required to make the plugin functional. Before you begin, ensure you have generated the native mobile projects by running:

```sh
tauri android init
tauri ios init
```

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

1.  **Add `google-services.json`**:
    *   Go to your Firebase project settings.
    *   Download the `google-services.json` file for your Android app.
    *   Place this file in the `src-tauri/gen/android/[YOUR_APP_NAME]/app/` directory.

2.  **Modify `build.gradle` files**:
    *   **Project-level** (`src-tauri/gen/android/[YOUR_APP_NAME]/build.gradle`): Add the Google services classpath.
        ```groovy
        // buildscript -> dependencies
        dependencies {
            classpath 'com.google.gms:google-services:4.4.1'
        }
        ```
    *   **App-level** (`src-tauri/gen/android/[YOUR_APP_NAME]/app/build.gradle`): Add the Google services plugin and Firebase Messaging dependency.
        ```groovy
        // At the top of the file
        apply plugin: 'com.google.gms.google-services'

        // dependencies { ... }
        dependencies {
            implementation 'com.google.firebase:firebase-messaging:23.4.1'
        }
        ```

3.  **Modify `AndroidManifest.xml`**: Open `src-tauri/gen/android/[YOUR_APP_NAME]/app/src/main/AndroidManifest.xml` and register the `FCMService` inside the `<application>` tag.

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

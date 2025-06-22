# Tauri 2 Push Notifications Plugin - Technical Specification

## Overview
A cross-platform push notification plugin for Tauri 2 applications supporting iOS (APNs), Android (FCM), and desktop platforms. The plugin provides a unified JavaScript API while leveraging native push notification services.

## Architecture

### Plugin Structure
```
tauri-plugin-push-notifications/
├── src/
│   ├── lib.rs              # Plugin entry, runtime detection
│   ├── commands.rs         # Tauri command handlers
│   ├── desktop.rs          # Desktop fallback implementation
│   ├── mobile.rs           # Mobile bridge implementation
│   ├── models.rs           # Shared data structures
│   └── error.rs            # Error types
├── android/                
│   └── src/main/java/.../
│       ├── PushNotificationPlugin.kt
│       └── FCMService.kt
├── ios/
│   └── Sources/
│       ├── PushNotificationPlugin.swift
│       └── Info.plist
├── guest-js/
│   └── index.ts            # JavaScript API
├── permissions/
│   └── default.toml        # Permission definitions
├── Cargo.toml
└── package.json
```

### Core Components

#### 1. JavaScript/TypeScript API (`guest-js/index.ts`)

```typescript
export interface PushNotification {
  title?: string;
  body?: string;
  data: Record<string, any>;
  badge?: number;
  sound?: string;
  channelId?: string; // Android
  category?: string;  // iOS
}

// Core Functions
export async function getToken(): Promise<string>;
export async function requestPermission(): Promise<{ granted: boolean }>;

// Event Listeners
export async function onNotificationReceived(
  handler: (notification: PushNotification) => void
): Promise<PluginListener>;

export async function onTokenRefresh(
  handler: (token: string) => void
): Promise<PluginListener>;

export async function onNotificationTapped(
  handler: (notification: PushNotification) => void
): Promise<PluginListener>;
```

#### 2. Rust Bridge (`src/mobile.rs`)

The Rust layer acts as a pure bridge between JavaScript and native code:

- **Commands**: Forward JS calls to native via `run_mobile_plugin`
- **Events**: Receive native callbacks via JNI/Swift and emit to JS via Tauri events
- **No business logic**: All push notification logic stays in native code

```rust
// Example bridge function
pub fn get_token(&self) -> Result<String> {
    self.app
        .run_mobile_plugin("getToken", Value::Null)
        .map(|value| value.as_str().unwrap_or_default().to_string())
        .map_err(Into::into)
}
```

#### 3. Native Implementations

##### Android (Kotlin)
- **PushNotificationPlugin.kt**: Main plugin class extending Tauri's Plugin
  - Singleton pattern for FCMService callbacks
  - Permission handling for Android 13+
  - Token retrieval via FCM SDK
- **FCMService.kt**: Firebase Messaging Service
  - `onMessageReceived`: Forward to plugin via singleton
  - `onNewToken`: Handle token refresh

##### iOS (Swift)
- **PushNotificationPlugin.swift**: Main plugin class
  - UNUserNotificationCenterDelegate implementation
  - APNs token management
  - Notification handling in foreground/background

## Data Flow

### Getting Token
1. JS: `const token = await getToken()`
2. Rust: Invoke native `getToken` via `run_mobile_plugin`
3. Native: Retrieve token from FCM/APNs
4. Native → Rust → JS: Return token string

### Receiving Notification (Foreground)
1. Push service → Native: Notification arrives
2. Native: Parse notification payload
3. Native → Rust: Call bridge function via JNI/Swift
4. Rust → JS: Emit `notification-received` event
5. JS: Trigger registered event handlers

### Token Refresh
1. Push service → Native: New token generated
2. Native → Rust: Call bridge function
3. Rust → JS: Emit `token-refresh` event
4. JS: Update stored token

## Implementation Requirements

### Android
- **Dependencies**:
  - Firebase Messaging SDK
  - Kotlin Coroutines
  - AndroidX libraries
- **Manifest**:
  - FCMService registration
  - POST_NOTIFICATIONS permission (Android 13+)
- **Configuration**:
  - google-services.json required
  - Firebase project setup

### iOS
- **Dependencies**:
  - UserNotifications framework
  - No additional pods required
- **Capabilities**:
  - Push Notifications capability
  - Background Modes (remote notifications)
- **Configuration**:
  - APNs certificates/keys
  - Proper provisioning profiles

### Permissions

#### Android
```kotlin
@TauriPlugin(
    permissions = [
        Permission(
            strings = [Manifest.permission.POST_NOTIFICATIONS],
            alias = "notifications"
        )
    ]
)
```

#### iOS
- Request authorization via `UNUserNotificationCenter`
- Handle provisional authorization
- Check notification settings

## Configuration

### tauri.conf.json
```json
{
  "plugins": {
    "push-notifications": {
      "android": {
        "icon": "notification_icon",
        "color": "#000000",
        "defaultChannelId": "default"
      },
      "ios": {
        "presentationOptions": ["badge", "sound", "banner"]
      }
    }
  }
}
```

### Capabilities Required
```json
{
  "permissions": [
    "push-notifications:default"
  ]
}
```

## Critical Implementation Notes

1. **JNI Bridge**: The Rust mobile implementation must expose JNI functions that the Android FCMService can call directly
2. **Singleton Pattern**: Android plugin must be accessible from FCMService
3. **Event Names**: Use consistent event names across platforms (e.g., `notification-received`)
4. **Token Format**: Preserve platform-specific token formats (FCM vs APNs)
5. **Lifecycle**: Handle app states properly (foreground, background, terminated)
6. **Error Handling**: Graceful degradation when services unavailable

## Testing Checklist

- [ ] Token retrieval on fresh install
- [ ] Token refresh handling
- [ ] Foreground notification receipt
- [ ] Background notification handling
- [ ] Notification tap actions
- [x] Permission request flows (scaffolded)
- [ ] Android 13+ permission handling
- [ ] iOS provisional authorization
- [ ] Deep linking from notifications
- [ ] Rich notifications (images, actions)

## Security Considerations

1. Never expose server keys in client code
2. Validate notification payloads
3. Implement certificate pinning for sensitive apps
4. Store tokens securely
5. Handle token rotation properly

## Platform-Specific Notes

### Android
- FCM tokens are ~152 characters
- Tokens may refresh periodically
- Channel management required for Android 8+
- Notification icons must be monochrome

### iOS
- APNs tokens are 64 hexadecimal characters
- Tokens refresh on app reinstall
- Categories define notification actions
- Rich notifications require service extension

## Development Priority Order

1. [x] **Core infrastructure**: Rust bridge setup
2. [x] **Android implementation**: FCM integration (scaffolded)
3. [x] **iOS implementation**: APNs integration (scaffolded)
4. [x] **JavaScript API**: Unified interface
5. [x] **Testing & Documentation**: Examples and verification
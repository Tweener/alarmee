[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.tweener/alarmee?color=orange)](https://central.sonatype.com/artifact/io.github.tweener/alarmee)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.9.2-blue.svg?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)
![gradle-version](https://img.shields.io/badge/gradle-8.13.0-blue?logo=gradle)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

[![Website](https://img.shields.io/badge/Author-vivienmahe.com-purple)](https://vivienmahe.com/)
[![X/Twitter](https://img.shields.io/twitter/follow/VivienMahe)](https://twitter.com/VivienMahe)

<br>

![Alarmee logo](https://github.com/user-attachments/assets/c5e72a35-6269-4b29-933e-6ed3e27900f1#gh-light-mode-only)
![Alarmee logo](https://github.com/user-attachments/assets/bcc0da27-1616-4758-a1cb-1d7601734988#gh-dark-mode-only)

---

# Alarmee

**Alarmee** is a Kotlin/Compose Multiplatform library designed to simplify scheduling alarms and notifications on both Android and iOS platforms. With Alarmee, you can schedule one-time or repeating alarms, display platform-specific notifications, and now supports push notifications using Firebase Cloud Messaging (Android) and Apple Push Notification service (iOS).

> [!WARNING]
> **Upgrading from v1.x?**  
> Check out the [Migration Guide](#-migration-guide-from-alarmee-1x-to-20) to update your code for version 2.0.

<br>

Be sure to show your support by starring ⭐️ this repository, and feel free to [contribute](#-contributing) if you're interested!

---

## 🌟 Features

- 📅 **One-off alarm**: Schedule an alarm to trigger at a specific date and time.
- 🔁 **Repeating alarm**: Schedule recurring alarms with intervals: hourly, daily, weekly, monthly, yearly or custom (providing a duration).
- ⚡️ **Instant notifications**: Send notifications immediately without scheduling them.
- ☁️ **Push notifications**: Handle remote notifications via FCM/APNs.
- 🔘 **Action buttons**: Add interactive action buttons to notifications (Android & iOS).
- 🎨 **Extensible Configuration**: Customize alarms and notifications with platform-specific settings.

![Group 4](https://github.com/user-attachments/assets/4e455c6c-6d45-4ca6-b292-8f8e57d4f799)

---

## 🚀 Used in production

Alarmee powers notifications in real-world apps:

- [**KMPShip**](https://www.kmpship.app/): a Kotlin Multiplatform boilerplate to build mobile apps faster.
- [**Bloomeo**](https://bloomeo.app/): a personal finance app.

---

## 🛠️ Installation

1. In your `settings.gradle.kts` file, add Maven Central to your repositories:
```Groovy
repositories {
  mavenCentral()
}
```

2. Then add Alarmee dependency to your module.

- For **local notifications only**, use `alarmee` dependency.
- For **both local and push notifications**, use `alarmee-push` dependency.

<details>
	<summary>With version catalog:</summary>

Open `libs.versions.toml`:

```Groovy
[versions]
alarmee = "2.4.0" // Check latest version

[libraries]
alarmee = { group = "io.github.tweener", name = "alarmee", version.ref = "alarmee" } // For local notifications only
alarmee = { group = "io.github.tweener", name = "alarmee-push", version.ref = "alarmee" } // For both local & push notifications

```

Then in your module `build.gradle.kts` add:
```Groovy
dependencies {
    // Only one of these is needed, depending on your use case
    implementation(libs.alarmee)
    implementation(libs.alarmee.push)
}
```
</details>

<details>
	<summary>Without version catalog:</summary>

In your module `build.gradle.kts` add:
```Groovy
dependencies {
    val alarmee_version = "2.4.0" // Check latest version

    // Only one of these is needed, depending on your use case
    implementation("io.github.tweener:alarmee:$alarmee_version") // For local notifications only
    implementation("io.github.tweener:alarmee-push:$alarmee_version") // For both local & push notifications
}
```
</details>

The latest version is: [![Maven Central Version](https://img.shields.io/maven-central/v/io.github.tweener/alarmee?color=orange)](https://central.sonatype.com/artifact/io.github.tweener/alarmee)

---

## 🔧 Configuration

To get started with Alarmee, you need to provide a platform-specific configuration for Android and iOS. Follow these steps.

### 1. Declare an expect function in `commonMain`

In your `commonMain` source set, declare the following function to provide platform-specific configuration:

```kotlin
expect fun createAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration
```

### 2. Provide the actual implementation in `androidMain`

In the `androidMain` source set, implement the actual function and return an `AlarmeeAndroidPlatformConfiguration`:

```kotlin
actual fun createAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration =
    AlarmeeAndroidPlatformConfiguration(
        notificationIconResId = R.drawable.ic_notification,
        notificationIconColor = androidx.compose.ui.graphics.Color.Red, // Defaults to Color.Transparent is not specified
        useExactScheduling = true, // Enable exact alarm scheduling for more precise timing (Android 12+, requires SCHEDULE_EXACT_ALARM permission)
        notificationChannels = listOf(
            AlarmeeNotificationChannel(
                id = "dailyNewsChannelId",
                name = "Daily news notifications",
                importance = NotificationManager.IMPORTANCE_HIGH,
                soundFilename = "notifications_sound",
            ),
            AlarmeeNotificationChannel(
                id = "breakingNewsChannelId",
                name = "Breaking news notifications",
                importance = NotificationManager.IMPORTANCE_LOW,
            ),
            // List all the notification channels you need here
        )
    )
```

### 3. Provide the actual implementation in `iosMain`

In the `iosMain` source set, implement the actual function and return an `AlarmeeIosPlatformConfiguration`:

```kotlin
val platformConfiguration: AlarmeePlatformConfiguration = AlarmeeIosPlatformConfiguration
```

### 4. Initialize `AlarmeeService`

There are multiple ways to initialize Alarmee depending on your setup.

---

#### ✅ For all targets (Android, iOS, desktop, etc.)

**With Compose:**
```kotlin
val alarmService: AlarmeeService = rememberAlarmeeService(
    platformConfiguration = createAlarmeePlatformConfiguration()
)
```

**Without Compose:**
```kotlin
val alarmeeService = createAlarmeeService()
alarmeeService.initialize(platformConfiguration = createAlarmeePlatformConfiguration())
```

---

#### 📱 For mobile targets only (Android & iOS)

Alarmee also supports push notifications on mobile via Firebase (Android) or APNs (iOS). If you're already using Firebase in your app, you can pass your own Firebase instance to avoid initializing it twice.

**With Compose:**

- If you're **not using Firebase yet**:
```kotlin
val alarmService: MobileAlarmeeService = rememberAlarmeeMobileService(
    platformConfiguration = createAlarmeePlatformConfiguration()
)
```

- If you're **already using Firebase** elsewhere in your app:
```kotlin
val alarmService: MobileAlarmeeService = rememberAlarmeeMobileService(
    platformConfiguration = createAlarmeePlatformConfiguration(),
    firebase = Firebase
)
```

**Without Compose:**

- If you're **not using Firebase yet**:
```kotlin
val alarmeeService = createAlarmeeMobileService()
alarmeeService.initialize(platformConfiguration = createAlarmeePlatformConfiguration())
```

- If you're **already using Firebase**:
```kotlin
val alarmeeService = createAlarmeeMobileService()
alarmeeService.initialize(
    platformConfiguration = createAlarmeePlatformConfiguration(),
    firebase = Firebase
)
```

You can then use this instance to schedule or cancel alarms from your shared code.

---

## 🧑‍💻  Usage

> [!IMPORTANT]
> Before using Alarmee, make sure the Notifications permission is granted on the target platform (Android [official documentation](https://developer.android.com/develop/ui/views/notifications/notification-permission), iOS [official documentation](https://developer.apple.com/documentation/usernotifications/asking-permission-to-use-notifications)).
>
> Alternativally, you can use [`moko-permissions`](https://github.com/icerockdev/moko-permissions) to easily handle permissions for you.

After initializing `AlarmeeService`, you can access the notification services:

##### Local Notifications (all platforms)

To send local notifications, use the local service:

```kotlin
val localService = alarmService.local
localService.schedule(...) // For instance
```

This is available on all targets (Android, iOS, desktop, web, etc.).

##### Push Notifications (mobile only)

To access push notifications (e.g. Firebase):

```kotlin
val pushService = alarmService.push
```

This is only available on Android and iOS. On non-mobile targets, pushService will be null.

### Local Notification Service

#### 1. Scheduling a one-off alarm
You can schedule an alarm to be triggered at a specific time of the day, using `AlarmeeService#schedule(...)`. When the alarm is triggered, a notification will be displayed.

For instance, to schedule an alarm on January 12th, 2025, at 5 PM:
```Kotlin
localService.schedule(
    alarmee = Alarmee(
        uuid = "myAlarmId",
        notificationTitle = "🎉 Congratulations! You've scheduled an Alarmee!",
        notificationBody = "This is the notification that will be displayed at the specified date and time.",
        scheduledDateTime = LocalDateTime(year = 2025, month = Month.JANUARY, dayOfMonth = 12, hour = 17, minute = 0),
        deepLinkUri = "https://www.example.com", // A deep link URI to be retrieved in MainActivity#onNewIntent() on Android and in AppDelegate#userNotificationCenter() on iOS
        imageUrl = "https://rickandmortyapi.com/api/character/avatar/1.jpeg", // Optional parameter to display an image within the notification 
        androidNotificationConfiguration = AndroidNotificationConfiguration( // Required configuration for Android target only (this parameter is ignored on iOS)
            priority = AndroidNotificationPriority.HIGH,
            channelId = "dailyNewsChannelId",
        ),
        iosNotificationConfiguration = IosNotificationConfiguration(),
    )
)
```

#### 2. Scheduling a repeating alarm
You can specify a [`RepeatInterval`](https://github.com/Tweener/alarmee/blob/main/alarmee/src/commonMain/kotlin/com/tweener/alarmee/RepeatInterval.kt) parameter, which allows scheduling an alarm to repeat hourly, daily, weekly, monthly, yearly or custom, based on the specified scheduledDateTime.

##### Fixed repeat interval
You can use a fixed repeat interval to schedule an Alarmee every hour, day, week, month, or year.
For instance, to schedule an alarm to repeat every day at 9:30 AM, you can use `RepeatInterval.Daily`:
```Kotlin
localService.schedule(
    alarmee = Alarmee(
        uuid = "myAlarmId",
        notificationTitle = "🔁 Congratulations! You've scheduled a daily repeating Alarmee!",
        notificationBody = "This notification will be displayed every day at 09:30.",
        scheduledDateTime = LocalDateTime(year = 2025, month = Month.JANUARY, dayOfMonth = 12, hour = 9, minute = 30),
        repeatInterval = RepeatInterval.Daily, // Will repeat every day
        androidNotificationConfiguration = AndroidNotificationConfiguration( // Required configuration for Android target only (this parameter is ignored on iOS)
            priority = AndroidNotificationPriority.DEFAULT,
            channelId = "dailyNewsChannelId",
        ),
        iosNotificationConfiguration = IosNotificationConfiguration(),
    )
)
```

##### Custom repeat interval
You can also set a custom repeat interval using `RepeatInterval.Custom(duration)` to schedule an Alarmee at a specified duration interval.
For example, to schedule an alarm to repeat every 15 minutes, you can use `RepeatInterval.Custom(duration = 15.minutes)`:
```Kotlin
localService.schedule(
    alarmee = Alarmee(
        uuid = "myAlarmId",
        notificationTitle = "🔁 Congratulations! You've scheduled a custom repeating Alarmee!",
        notificationBody = "This notification will be displayed every 15 minutes",
        repeatInterval = RepeatInterval.Custom(duration = 15.minutes), // Will repeat every 15 minutes
        androidNotificationConfiguration = AndroidNotificationConfiguration( // Required configuration for Android target only (this parameter is ignored on iOS)
            priority = AndroidNotificationPriority.DEFAULT,
            channelId = "otherChannelId",
        ),
        iosNotificationConfiguration = IosNotificationConfiguration(),
    )
)
```

#### 3. Cancelling an alarm
An alarm can be cancelled using its uuid, using `Alarmee#cancel(...)`. If an alarm with the specified uuid is found, it will be canceled, preventing any future notifications from being triggered for that alarm.
```Kotlin
localService.cancel(uuid = "myAlarmId")
```

#### 4. Cancelling all alarms
You can cancel all scheduled alarms at once using `cancelAll()`. This will cancel all pending alarms that have been scheduled through this service.
```Kotlin
localService.cancelAll()
```

> [!NOTE]
> On Android, this cancels all displayed notifications. Due to AlarmManager API limitations, scheduled alarms cannot be canceled in bulk - they must be canceled individually using their specific identifiers.

#### 5. Trigger an alarm right away
You can trigger an alarm to instantly display a notification without scheduling it for a specific time:
```Kotlin
localService.immediate(
    alarmee = Alarmee(
        uuid = "myAlarmId",
        notificationTitle = "🚀 Congratulations! You've pushed an Alarmee right now!",
        notificationBody = "This notification will be displayed right away",
        androidNotificationConfiguration = AndroidNotificationConfiguration( // Required configuration for Android target only (this parameter is ignored on iOS)
            priority = AndroidNotificationPriority.DEFAULT,
            channelId = "immediateChannelId",
        ),
        iosNotificationConfiguration = IosNotificationConfiguration(),
    )
)
```

#### 6. Notification customization
##### Notification sound
You can customize the notification sound on both Android and iOS.

> [!WARNING]
> Custom sounds must be under 30 seconds in length on both Android and iOS. If the sound exceeds this limit, the system will fall back to the default notification sound..

<details>
	<summary>🤖 Android</summary>

Notification sounds are set via `AlarmeeNotificationChannel`, which allows you to define the sound file for a specific notification channel.

1. Place your custom sound file in the `res/raw` directory of your app (e.g., `res/raw/notifications_sound.obb`).
2. Define a custom notification channel and provide the sound file name:
```Kotlin
AlarmeeNotificationChannel(
    id = "dailyNewsChannelId",
    name = "Daily news notifications",
    importance = NotificationManager.IMPORTANCE_HIGH,
    soundFilename = "notifications_sound", // file name without the extension
)
```
</details>

<details>
	<summary>🍎 iOS</summary>

Notification sounds are set in the `IosNotificationConfiguration` by providing the file name of the sound located in the app's bundle.

1. Add your sound file to your Xcode project under the `main` bundle.
2. Reference the sound file with its exact name and extension:
```Kotlin
Alarmee(
    // ...
    iosNotificationConfiguration = IosNotificationConfiguration(
        soundFilename = "notifications_sound.wav",
    ),
)
```
</details>

##### Notification icon
<details>
	<summary>🤖 Android</summary>

* **Global icon customization**: You can set a default notification icon color and drawable for all notifications for your app.
```Kotlin
AlarmeeAndroidPlatformConfiguration(
    notificationIconResId = R.drawable.ic_notification,
    notificationIconColor = Color.Yellow,
    // ...
)
```

* **Per-alarm icon customization**: Override the global defaults by specifying the icon color and drawable for individual notifications.
```Kotlin
localService.schedule(
    alarmee = Alarmee(
        androidNotificationConfiguration = AndroidNotificationConfiguration(
            notificationIconResId = R.drawable.ic_another_notification,
            notificationIconColor = Color.Red,
            // ...
        ),
        // ...
    )
)
```
</details>

<details>
	<summary>🍎 iOS</summary>

On iOS, customizing icon colors and drawables is not supported.
</details>

##### Notification badge
<details>
	<summary>🤖 Android</summary>

On Android, badge numbers are managed by the system and direct control over the badge number is not available in the notification API. The system automatically handles badge updates based on notifications.
</details>

<details>
	<summary>🍎 iOS</summary>

You can customize the badge number displayed on the app icon for notifications. This is done using the `IosNotificationConfiguration`:
```Kotlin
Alarmee(
    // ...
    iosNotificationConfiguration = IosNotificationConfiguration(
        badge = 4,
    ),
)
```

If `badge = 0`, the badge will be cleared from the app icon. If `badge = null`, the badge will not be updated.
</details>

##### Notification action buttons
You can add up to 3 action buttons to your notifications. When a user taps an action button, your app receives a callback with the action ID.

**Adding action buttons to a notification:**
```kotlin
localService.immediate(
    alarmee = Alarmee(
        uuid = "messageNotificationId",
        notificationTitle = "📩 New Message",
        notificationBody = "You have a new message!",
        actions = listOf(
            NotificationAction(id = "reply", label = "Reply"),
            NotificationAction(id = "mark_read", label = "Mark as Read"),
            NotificationAction(id = "dismiss", label = "Dismiss"),
        ),
        androidNotificationConfiguration = AndroidNotificationConfiguration(
            priority = AndroidNotificationPriority.HIGH,
            channelId = "messagesChannelId",
        ),
        iosNotificationConfiguration = IosNotificationConfiguration(),
    )
)
```

**Handling action button clicks:**

To receive callbacks when a user taps an action button, register a callback using the `onActionClicked` extension function:

```kotlin
import com.tweener.alarmee.onActionClicked

// Register the callback early in your app lifecycle
alarmService.local.onActionClicked { event ->
    println("Action clicked: ${event.actionId} on notification ${event.notificationUuid}")

    when (event.actionId) {
        "reply" -> { /* Handle reply action */ }
        "mark_read" -> { /* Handle mark as read action */ }
        "dismiss" -> { /* Handle dismiss action */ }
    }
}
```

> [!IMPORTANT]
> - Only one callback can be registered at a time. Subsequent calls replace the previous callback.
> - Register the callback early in your app lifecycle (e.g., in `Application.onCreate()` on Android or a `LaunchedEffect` in your root composable).
> - On Android, the notification is automatically dismissed when an action button is tapped.

<details>
	<summary>🍎 iOS additional setup</summary>

On iOS, you need to update your `AppDelegate.swift` to forward action button taps to the Alarmee callback registry:

```swift
import alarmee

// In your AppDelegate class that conforms to UNUserNotificationCenterDelegate:

func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
    let userInfo = response.notification.request.content.userInfo
    let actionIdentifier = response.actionIdentifier

    // Handle notification action button taps
    if actionIdentifier != UNNotificationDefaultActionIdentifier && actionIdentifier != UNNotificationDismissActionIdentifier {
        if let notificationUuid = userInfo["notificationUuid"] as? String {
            NotificationActionHelper().onActionClicked(notificationUuid: notificationUuid, actionId: actionIdentifier)
        }
    }

    completionHandler()
}
```
</details>

---

### Push Notification Service

> [!WARNING]
> On iOS, make sure to add Firebase as a dependency (`https://github.com/firebase/firebase-ios-sdk`) to your Xcode project.
>
> Then, in your target, add `Background Modes` (check `Remote notifications`) and `Push notifications` capabilities.

The `PushNotificationService` handles push notifications for mobile platforms only (Android & iOS). It is available via the `MobileAlarmeeService` interface.

You can access it like this:

```kotlin
val pushService = alarmService.push
```

This is only available on **Android** and **iOS**. On other targets, `pushService` will be `null`.

#### Getting the Firebase Installation ID

You can retrieve the Firebase Installation ID for the current app instance:

```kotlin
val installationIdResult = pushService.getInstallationId()
installationIdResult.onSuccess { installationId ->
    println("Firebase Installation ID: $installationId")
}.onFailure { error ->
    println("Failed to get Installation ID: $error")
}
```

The Installation ID uniquely identifies the app installation on the device and is useful for analytics, targeting specific devices, or debugging.

#### Firebase Token Management

Firebase Cloud Messaging (FCM) tokens can change when:
- The app is restored on a new device
- The app is restored from backup
- The app data is cleared
- The token is periodically refreshed by Firebase

You can retrieve the current FCM token:

```kotlin
val tokenResult = pushService.getToken()
tokenResult.onSuccess { token ->
    println("FCM Token: $token")
    // Send token to your server
}.onFailure { error ->
    println("Failed to get token: $error")
}
```

To handle token updates, register a callback that will be notified whenever a new token is generated:

```kotlin
pushService.onNewToken { newToken ->
    // Update your server with the new token
    sendTokenToServer(newToken)
}
```

You can also manually refresh the token for testing purposes:

```kotlin
pushService.forceTokenRefresh()
```

#### Push Message Callbacks

To handle incoming push message payloads in your app (e.g., for custom processing, updating UI, or syncing data), register a callback that will be invoked whenever a push message is received:

```kotlin
pushService.onPushMessageReceived { payload ->
    println("📩 Push message received with payload: $payload")
    // Handle custom logic here, e.g., update UI, sync data, etc.
}
```

This callback receives the complete key-value payload from the push message, allowing you to access any custom data sent with the notification. The callback is triggered before the notification is displayed to the user.

**Note:** You can register multiple callbacks - all registered callbacks will be invoked when a push message is received.

#### Add the Notification Service Extension (iOS only)

To display images in push notifications on iOS, create a **Notification Service Extension** and paste the provided `NotificationService.swift` file:

1. In Xcode, go to **File > New > Target...**
2. Choose **Notification Service Extension**
3. Name it `AlarmeeNotificationService`
4. Replace the content of the created file with [`NotificationService.swift`](ios/NotificationServiceExtension/NotificationService.swift)

#### Sending a test push notification

To send a push notification manually using Postman or `curl`, you can call the FCM v1 HTTP API with the following:

**URL:**
```
https://fcm.googleapis.com/v1/projects/{YOUR_FIREBASE_PROJECT_ID}/messages:send
```

Replace `{YOUR_PROJECT_ID}` with your Firebase project ID.

**Example payload:**
```json
{
  "message": {
    "token": "DEVICE_FCM_TOKEN",
    "apns": {
      "payload": {
        "aps": {
          "alert": {
            "title": "Title for iOS",
            "body": "This is the body of the iOS notification"
          },
          "mutable-content": 1
        }
      },
      "headers": {
        "apns-priority": "10"
      }
    },
    "data": {
      "title": "Title for Android",
      "body": "This is the body of the Android notification",
      "deepLinkUri": "app://open/target", // Used on both Android & iOS
      "imageUrl": "https://rickandmortyapi.com/api/character/avatar/1.jpeg" // Used on both Android & iOS
    }
  }
}
```

- **`token`**: the FCM token of the target device.
- **`apns.payload.aps.mutable-content`**: required for displaying images on iOS.
- **`data.imageUrl`**: optional parameter to display an image within the notification.
- **`apns.headers.apns-priority = 10`** ensures the push is delivered immediately.

##### Authentication (Bearer Token)

To authenticate requests to the FCM HTTP v1 API, you must include a **Bearer token** in the `Authorization` header.

1. Make sure you have the `gcloud` CLI installed and authenticated.
2. Set the correct project with:
```bash
gcloud config set project YOUR_FIREBASE_PROJECT_ID
```
3. Get an access token with:
```bash
gcloud auth print-access-token
```
4. In Postman or cURL, set the header:
```http
Authorization: Bearer YOUR_ACCESS_TOKEN
```

That's it! Your push notifications will now support images on iOS.

---

## 🔄 Migration Guide: From Alarmee 1.x to 2.0

Version `2.0` introduces a new API structure with a focus on clearer service boundaries and support for both local and push notifications. Follow these steps to migrate your existing code:

---

### 1. Replace `AlarmeeScheduler` with `AlarmeeService`

In `1.x`, the entry point was:

```kotlin
val alarmeeScheduler: AlarmeeScheduler = rememberAlarmeeScheduler(
    platformConfiguration = platformConfiguration
)
```

In `2.0`, it has been **replaced** by:

```kotlin
val alarmService: AlarmeeService = rememberAlarmeeService(
    platformConfiguration = platformConfiguration
)
```

### 2. Update function calls

All method calls on `AlarmeeScheduler` should now be redirected to the **local notification service** from `AlarmeeService`:

#### Replace:
```kotlin
alarmeeScheduler.schedule(alarmee)
```

#### With:
```kotlin
alarmService.local.schedule(alarmee)
```

Similarly, any other function calls (e.g., `cancel(...)`, `immediate(...)`, etc.) should follow this pattern:

```kotlin
// Before
alarmeeScheduler.cancel(alarmee)

// After
alarmService.local.cancel(alarmee)
```

### 3. Rename `push(alarmee)` to `immediate(alarmee)`

In `1.x`, to instantly trigger a local notification, you called:

```kotlin
alarmeeScheduler.push(alarmee)
```

In `2.0`, this method has been renamed to:

```kotlin
alarmService.local.immediate(alarmee)
```

### ✅ Summary of Changes

| 1.x                                        | 2.0                                             |
|--------------------------------------------|-------------------------------------------------|
| `AlarmeeScheduler`                         | `AlarmeeService`                                |
| `rememberAlarmeeScheduler(...)`            | `rememberAlarmeeService(...)`                   |
| `alarmeeScheduler.schedule(...)`           | `alarmService.local.schedule(...)`              |
| `alarmeeScheduler.cancel(...)`             | `alarmService.local.cancel(...)`                |
| `alarmeeScheduler.push(...)`               | `alarmService.local.immediate(...)`             |

---

## 👨‍💻 Contributing

We love your input and welcome any contributions! Please read our [contribution guidelines](https://github.com/Tweener/alarmee/blob/master/CONTRIBUTING.md) before submitting a pull request.

---

## 🙏 Credits

- Logo by [Freeicons](https://freeicons.io/essential-collection/alarm-icon-icon-2)

---

## 📜 Licence

Alarmee is licensed under the [Apache-2.0](https://github.com/Tweener/alarmee?tab=Apache-2.0-1-ov-file#readme).

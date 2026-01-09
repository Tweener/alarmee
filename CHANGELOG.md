# Changelog

## [2.6.0] - January 9, 2026
- ✨ **`[FEATURE]`**: Added notification action buttons support for both Android and iOS. Notifications can now display up to 3 action buttons that users can tap to trigger callbacks in your app.
- 🛠 **`[IMPROVEMENT]`**: 🤖 On Android, added support for passing custom data from Firebase Cloud Messaging messages through the notification creation process and into the launched activity via intent extras.

## [2.5.2] - November 6, 2025
- 🛠 **`[IMPROVMENT]`**: Refactored `MobileAlarmeeService` initialization methods for better clarity. Split the initialize method into two overloads: one accepting only `platformConfiguration` and another accepting both `platformConfiguration` and `Firebase` instance, improving flexibility and code organization.

## [2.5.1] - November 5, 2025
- 🐛 **`[FIX]`**: 🍎 On iOS, fixed weekly repeating notifications to correctly convert ISO 8601 day numbers (Monday=1, Sunday=7) to NSCalendar weekday numbers (Sunday=1, Monday=2, etc.). This ensures notifications trigger on the correct day of the week regardless of device locale settings.
- 🔄 Update Kotlin to 2.2.21.
- 🔄 Update Compose Multiplatform to 1.9.2.
- 🔄 Update Material3 to 1.9.0.
- 🔄 Update Dokka to 2.1.0.
- 🔄 Update KMPKit to 1.0.13.
- 🔄 Update GitLiveApp Firebase to 2.4.0.
- 🔄 Update Google Services plugin to 4.4.4.
- 🔄 Update Dependency Versions plugin to 0.53.0.

## [2.5.0] - September 30, 2025
- ✨ **`[FEATURE]`**: Added `PushNotificationService#getInstallationId()` method to retrieve the Firebase Installation ID for the app instance on mobile targets (Android & iOS).

## [2.4.2] - September 24, 2025
- ✨ **`[FIX]`**: 🍎 On iOS, Set FirebaseMessaging pod to linkOnly in iOS config. This change ensures the pod is only linked and not embedded, which may help resolve integration or duplication issues.

## [2.4.1] - September 23, 2025
- ✨ **`[FIX]`**: 🤖 On Android, Remove exact alarm permissions from AndroidManifest. You should add the permission in your app's manifest if you really need it.

## [2.4.0] - September 22, 2025
- ✨ **`[FEATURE]`**: 🤖 On Android, added exact alarm scheduling support using `setExactAndAllowWhileIdle` with new `useExactScheduling` parameter in `AlarmeeAndroidPlatformConfiguration`. Automatically checks for `SCHEDULE_EXACT_ALARM` permission and gracefully falls back to standard scheduling when not available.
- ✨ **`[FEATURE]`**: Added `cancelAll()` method to cancel all scheduled alarms at once. 🤖 On Android, cancels all displayed notifications (AlarmManager doesn't provide API to cancel all scheduled alarms).
- 🔄 Update Kotlin to 2.2.20.
- 🔄 Update Compose Multiplatform to 1.9.0.
- 🔄 Update Gradle to 8.13.0.
- 🔄 Update GitLiveApp to 2.3.0.
- 🔄 Update Android Activity to 1.11.0.

## [2.3.2] - September 22, 2025
- 🛠 **`[IMPROVMENT]`**: Update namespace for alarmee-push module
- 🐛 **`[FIX]`**: Move Firebase messaging service to push module

## [2.3.1] - August 21, 2025
- ✨ **`[FEATURE]`**: Added push message callback support for custom payload handling on mobile targets (Android & iOS).
  - New method `PushNotificationService#onPushMessageReceived(callback)` allows registering callbacks to receive push message payloads
  - Renamed internal method from `onMessageReceived` to `handleIncomingMessage` to clarify API boundaries

## [2.3.0] - August 21, 2025
- ✨ **`[FEATURE]`**: Added Firebase token update callbacks for push notifications on mobile targets (Android & iOS).
  - New method `PushNotificationService#onNewToken(callback)` allows registering callbacks to be notified when Firebase generates a new FCM token
  - Callbacks are triggered automatically when tokens are refreshed (e.g., app reinstall, backup restore, or periodic refresh)
  - Added `PushNotificationService#forceTokenRefresh()` method for manual token refresh (useful for testing and development)

## [2.2.0] - July 22, 2025
* 🚨 **`[BREAKING]`** Local & push notifications features are now separated into two dependencies:
  * For local notifications only (all targets): use `io.github.tweener:alarmee:$alarmee_version`
  * For local & push notifications (mobile targets only): use `io.github.tweener:alarmee-push:$alarmee_version`
* 🚨 **`[BREAKING]`** [Initialization of Alarmee](https://github.com/Tweener/alarmee?tab=readme-ov-file#4-initialize-alarmeeservice) varies if using local notifications only or local & push notifications:
  * For local notifications only (all targets): use `rememberAlarmeeService(...)` (with Compose) or `createAlarmeeService()` (without Compose)
  * For local & push notifications (mobile targets only): use `rememberAlarmeeMobileService(...)` (with Compose) or `createAlarmeeMobileService()` (without Compose).
* 🛠 **`[IMPROVMENT]`**: 🍎 On iOS, if you're only using local notifications, you can skip adding Firebase as a dependency and don’t need to enable the  `Background Modes` or `Push notifications` capabilities. (Requested by [@[kalinjul](https://github.com/[kalinjul) in Issue #25)

## [2.1.4] - July 21, 2025
- 🐛 **`[FIX]`**: Use deep link URI hashCode as requestCode in PendingIntent creation (PR [#26](https://github.com/Tweener/alarmee/pull/26) by [@robholmes](https://github.com/robholmes))
- 🐛 **`[FIX]`**: Use correct Gradle distributionUrl pointing to version 8.14.3.

## [2.1.3] - July 15, 2025
- 🔄 Update KMPKit to 1.0.10, which uses latest version of `kotlinx-datetime:0.7.1`.

## [2.1.2] - July 13, 2025
- 🛠 **`[IMPROVMENT]`**: It is now possible to initialize Alarmee via Compose, on mobile targets, with an already existing Firebase instance.

## [2.1.1] - July 7, 2025
- 🐛 **`[FIX]`**: Fix Alarmee missing initialize method when a Firebase instance already exists.

## [2.1.0] - July 6, 2025
- ✨ **`[FEATURE]`**: Notifications can now display images, from both local and push sources.
- 🔄 Update Kotlin to 2.2.0.
- 🔄 Update Compose Multiplatform to 1.8.2.

## [2.0.2] - June 12, 2025
- 🛠 **`[IMPROVMENT]`**: 🤖 On Android, create a default channel notification if none was provided in the `AlarmeeAndroidPlatformConfiguration`, to make sure there is always at least one.

## [2.0.1] - June 9, 2025
- 🛠 **`[IMPROVMENT]`**: Allow Alarmee to be initialized by passing a Firebase instance, for mobile targets (Android & iOS).
- 🛠 **`[IMPROVMENT]`**: Exposes the FCM token via `PushNotificationService#getToken()`, for mobile targets (Android & iOS).

## [2.0.0] - June 9, 2025
🚀 Alarmee 2.0 is here!
- ✨ **`[FEATURE]`**: Push notifications on iOS and Android!
- 🛠 **`[IMPROVMENT]`**: The API structure has been revamped with a focus on clearer service boundaries and support for both local and push notifications.

Check out the [migration guide from version 1.x to 2.0](https://github.com/Tweener/alarmee?tab=readme-ov-file#-migration-guide-from-alarmee-1x-to-20).

## [1.6.4] - May 25, 2025
- ✨ **`[FEATURE]`**: Add `deepLinkUri` support to `Alarmee` (Requested by [@M1r7](https://github.com/M1r7) in [https://github.com/Tweener/alarmee/pull/7](https://github.com/Tweener/alarmee/issues/18))

## [1.6.3] - May 13, 2025
- 🔄 Update Kotlin to 2.1.21.
- 🔄 Update Compose Multiplatform to 1.8.0.
- 🔄 Update KMPKit to 1.0.7.

## [1.6.2] - April 15, 2025
- 🔄 Update Kotlin to 2.1.20.

## [1.6.1] - January 24, 2025
- 🔄 Update Kotlin to 2.1.0.
- 🔄 Update Compose Multiplatform to 1.7.3.
- 🔄 Update Gradle to 8.11.1.
- 🛠 **`[IMPROVMENT]`**: 🍎 On iOS, badge is now optional to avoid automatically setting it to 0 when not provided.

## [1.6.0] - January 12, 2025
- 🚨 **`[BREAKING]`**: 🤖 On Android, `AndroidNotificationConfiguration#notificationIconResId` and `AndroidNotificationConfiguration#notificationIconColor` have been renamed respectively `AndroidNotificationConfiguration#iconResId` and `AndroidNotificationConfiguration#iconColor` for naming consistency.
- ✨ **`[FEATURE]`**: Set custom notification sounds 🔈 on both Android and iOS. Details [here](https://github.com/Tweener/alarmee/blob/main/README.md#notification-sound).
- ✨ **`[FEATURE]`**: 🍎 On iOS, set the badge number on notifications. Details [here](https://github.com/Tweener/alarmee/blob/main/README.md#notification-badge).

## [1.5.0] - January 4, 2025
- ✨ **`[FEATURE]`**: Directly push Alarmees right away without scheduling by [@Zubayer204](https://github.com/Zubayer204) in https://github.com/Tweener/alarmee/pull/7
- 🛠 **`[IMPROVMENT]`**: Improves validation of an Alarmee and adjusting date in future.
- 🐛 **`[FIX]`**: 🍎 On iOS, fixes Alarmees being duplicated in some cases, by keeping only one trigger.

## [1.4.1] - December 21, 2024
- ✨ **`[FEATURE]`**: 🤖 On Android, you can now customize the notification icon color and drawable for all notifications in your app or on a per-notification basis.
- 🛠 **`[IMPROVMENT]`**: Uses KMPKit library instead of deprecated kmp-common.

## [1.4.0] - December 17, 2024
- 🚨 **`[BREAKING]`**: `RepeatInterval` is now a sealed class instead of an enum class.
- 🛠 **`[IMPROVMENT]`**: Schedule a repeating Alarmee with a specific `Duration` interval, using [RepeatInterval.Custom](https://github.com/Tweener/alarmee/blob/main/alarmee/src/commonMain/kotlin/com/tweener/alarmee/RepeatInterval.kt#L18). See [sample](https://github.com/Tweener/alarmee/blob/main/sample/composeApp/src/commonMain/kotlin/com/tweener/alarmee/sample/App.kt#L78-L95).

## [1.3.4] - December 16, 2024
- 🛠 **`[IMPROVMENT]`**: 🍎 On iOS, add granularity for seconds when scheduling a repeating Alarmee.
- 🛠 **`[IMPROVMENT]`**: Sample: Update iOS sample to show notifications when the app is in foreground (See [documentation](https://developer.apple.com/documentation/usernotifications/scheduling-a-notification-locally-from-your-app#overview)).

## [1.3.3] - November 29, 2024
- ✨ **`[FEATURE]`**: Add support for JVM, JS, and WasmJS targets.
> [!WARNING]
> Proper implementations for these targets are pending future updates. This is added so projects targeting these platforms can use Alarmee.

## [1.3.2] - November 27, 2024
- 📦 **`[BUILD]`**: Removed core library desugaring.

## [1.3.1] - November 27, 2024
- 🛠 **`[IMPROVMENT]`**: Add log when an alarm is scheduled in the past and rescheduled it to tomorrow.
- 🛠 **`[IMPROVMENT]`**:🍎 On iOS, add granularity for seconds when scheduling an Alarmee.
- 🐛 **`[FIX]`**: 🤖 On Android, ensure specified channel ID for Alarmee exists before scheduling the Alarmee to prevent lost notifications.

## [1.3.0] - November 26, 2024
- 🚨 **`[BREAKING]`**: `Alarmee#schedule(...)` and `Alarmee#cancel(...)` are not Composable functions anymore.
- 🛠 **`[IMPROVMENT]`**: 🤖 On Android, it is now possible to configure a channel's importance and a notification's priority.
- 🛠 **`[IMPROVMENT]`**: 🤖 On Android, if not using Compose Multiplatform, a `Context` must be passed to the `AlarmeeSchedulerAndroid` instance.

## [1.2.0] - November 20, 4
- 🚨 **`[BREAKING]`**: `AlarmeePlatformConfiguration.Android` and `AlarmeePlatformConfiguration.Ios` have been replaced respectively by `AlarmeeAndroidPlatformConfiguration` and `AlarmeeIosPlatformConfiguration`. Details [here](https://github.com/Tweener/alarmee?tab=readme-ov-file#platform-configurations).
- ✨ **`[FEATURE]`**: Introduced repeating alarms with intervals: hourly, daily, weekly, monthly, and yearly.
- 🛠 **`[IMPROVMENT]`**: 🤖 On Android, you can create more than one notification channel. See [AlarmeeAndroidPlatformConfiguration](https://github.com/Tweener/alarmee/blob/main/alarmee/src/androidMain/kotlin/com/tweener/alarmee/configuration/AlarmeeAndroidPlatformConfiguration.kt)

## [1.1.0] - November 14, 2024
- 🚨 **`[BREAKING]`** Creating an instance of `AlarmeeScheduler` now requires to pass a `AlarmeePlatformConfiguration` which is platform dependent. Details [here](https://github.com/Tweener/alarmee?tab=readme-ov-file#platform-configurations).
- ✨ **`[FEATURE]`**: **Alarmee is now compatible with Compose Multiplatform! 🚀** Create an instance of `AlarmeeScheduler` and remember it during recomposition. Details [here](https://github.com/Tweener/alarmee?tab=readme-ov-file#platform-configurations).
- ✨ **`[FEATURE]`**: Added one-off alarms that trigger at a specific date and time.

## [1.0.1] - November 13, 2024
- 📦 **`[BUILD]`**: Napier dependency has been removed and logs are now using println.

## [1.0.0] - November 7, 2024

### 🚀 Initial Release

The first official release of **Alarmee**.

#### Features
  - Unified API for scheduling alarms and notifications across Android and iOS.
  - Support for one-off alarms.
  - Extensible configuration for platform-specific settings.

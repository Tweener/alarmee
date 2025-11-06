package com.tweener.alarmee

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import dev.gitlive.firebase.Firebase

@Composable
fun rememberAlarmeeMobileService(
    platformConfiguration: AlarmeePlatformConfiguration,
    firebase: Firebase? = null,
): MobileAlarmeeService =
    remember(platformConfiguration, firebase) {
        createAlarmeeMobileService().apply {
            if (firebase == null) initialize(platformConfiguration = platformConfiguration)
            else initialize(platformConfiguration = platformConfiguration, firebase = firebase)
        }
    }

/**
 * Platform-specific extension of [AlarmeeService] for mobile platforms (Android and iOS).
 *
 * This interface adds support for remote push notifications via [PushNotificationService].
 * It is only available on `mobileMain`, which includes Android and iOS targets.
 *
 * To access push notifications from the shared [AlarmeeService], you can safely cast it:
 *
 * ```kotlin
 * val pushService = (alarmeeService as? MobileAlarmeeService)?.push
 * ```
 *
 * This allows platform-specific code (e.g., Firebase or APNs handlers) to trigger custom behavior
 * when a remote push message is received.
 *
 * @author Vivien Mahe
 * @since 05/06/2025
 */
interface MobileAlarmeeService : AlarmeeService {

    /**
     * Service for handling remote push notifications.
     *
     * Only available on Android and iOS targets.
     */
    val push: PushNotificationService

    /**
     * Initializes the Alarmee service with platform-specific configuration.
     *
     * This method **must be called once when the app is launched**. For example, in your root Composable:
     *
     * ```kotlin
     * val alarmeeService = rememberAlarmeeService(createAlarmeePlatformConfiguration())
     * ```
     *
     * @param platformConfiguration The platform-specific configuration for Alarmee.
     */
    override fun initialize(platformConfiguration: AlarmeePlatformConfiguration)

    /**
     * Initializes the Alarmee service with platform-specific configuration and Firebase instance.
     *
     * This method **must be called once when the app is launched**. For example, in your root Composable:
     *
     * ```kotlin
     * val alarmeeService = rememberAlarmeeService(createAlarmeePlatformConfiguration(), Firebase)
     * ```
     *
     * Use this method if your app already uses a Firebase instance.
     *
     * @param platformConfiguration The platform-specific configuration for Alarmee.
     * @param firebase The Firebase instance to use for push notifications.
     */
    fun initialize(platformConfiguration: AlarmeePlatformConfiguration, firebase: Firebase)

}

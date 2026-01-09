package com.tweener.alarmee.sample

import com.tweener.alarmee.LocalNotificationService
import com.tweener.alarmee.configuration.AlarmeeIosPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration

/**
 * @author Vivien Mahe
 * @since 26/11/2024
 */

private val alarmeePlatformConfiguration = AlarmeeIosPlatformConfiguration

actual fun createAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration = alarmeePlatformConfiguration

actual fun LocalNotificationService.registerActionCallback() {
    // No-op on iOS for now. iOS action support will be added later.
}

package com.tweener.alarmee.sample

import com.tweener.alarmee.LocalNotificationService
import com.tweener.alarmee.configuration.AlarmeeIosPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.alarmee.onActionClicked

/**
 * @author Vivien Mahe
 * @since 26/11/2024
 */

private val alarmeePlatformConfiguration = AlarmeeIosPlatformConfiguration

actual fun createAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration = alarmeePlatformConfiguration

actual fun LocalNotificationService.registerActionCallback() {
    onActionClicked { event ->
        println("Notification action clicked! UUID: ${event.notificationUuid}, Action: ${event.actionId}")
    }
}

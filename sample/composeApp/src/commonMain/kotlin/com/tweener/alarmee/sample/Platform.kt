package com.tweener.alarmee.sample

import com.tweener.alarmee.LocalNotificationService
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration

/**
 * @author Vivien Mahe
 * @since 26/11/2024
 */

expect fun createAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration

/**
 * Registers a callback for notification action button clicks.
 * This is Android-specific; on other platforms it's a no-op.
 */
expect fun LocalNotificationService.registerActionCallback()

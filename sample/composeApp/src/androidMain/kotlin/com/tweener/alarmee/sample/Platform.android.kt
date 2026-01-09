package com.tweener.alarmee.sample

import android.app.NotificationManager
import androidx.compose.ui.graphics.Color
import com.tweener.alarmee.LocalNotificationService
import com.tweener.alarmee.channel.AlarmeeNotificationChannel
import com.tweener.alarmee.configuration.AlarmeeAndroidPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.alarmee.onActionClicked

/**
 * @author Vivien Mahe
 * @since 26/11/2024
 */

private val alarmeePlatformConfiguration =
    AlarmeeAndroidPlatformConfiguration(
        notificationIconResId = R.drawable.ic_stat_name,
        notificationIconColor = Color.Yellow,
        notificationChannels = listOf(
            AlarmeeNotificationChannel(id = "dailyNewsChannelId", name = "Daily news notifications"),
            AlarmeeNotificationChannel(id = "breakingNewsChannelId", name = "Breaking news notifications", importance = NotificationManager.IMPORTANCE_HIGH),
            AlarmeeNotificationChannel(id = "immediateChannelId", name = "Immediate notifications", importance = NotificationManager.IMPORTANCE_HIGH),
            AlarmeeNotificationChannel(id = "soundChannelId", name = "Sound notifications", importance = NotificationManager.IMPORTANCE_HIGH, soundFilename = "hello"),
        ),
        useExactScheduling = true, // Enable exact scheduling for more precise alarms
    )

actual fun createAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration = alarmeePlatformConfiguration

actual fun LocalNotificationService.registerActionCallback() {
    onActionClicked { event ->
        println("Notification action clicked! UUID: ${event.notificationUuid}, Action: ${event.actionId}")
    }
}

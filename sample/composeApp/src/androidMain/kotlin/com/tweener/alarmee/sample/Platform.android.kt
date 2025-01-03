package com.tweener.alarmee.sample

import android.app.NotificationManager
import androidx.compose.ui.graphics.Color
import com.tweener.alarmee.channel.AlarmeeNotificationChannel
import com.tweener.alarmee.configuration.AlarmeeAndroidPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration

/**
 * @author Vivien Mahe
 * @since 26/11/2024
 */

actual fun createAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration =
    AlarmeeAndroidPlatformConfiguration(
        notificationIconResId = R.drawable.ic_stat_name,
        notificationIconColor = Color.Yellow,
        notificationChannels = listOf(
            AlarmeeNotificationChannel(id = "dailyNewsChannelId", name = "Daily news notifications"),
            AlarmeeNotificationChannel(id = "breakingNewsChannelId", name = "Breaking news notifications", importance = NotificationManager.IMPORTANCE_HIGH),
            AlarmeeNotificationChannel(id = "immediateChannelId", name = "Immediate Notification", importance = NotificationManager.IMPORTANCE_HIGH),
        )
    )

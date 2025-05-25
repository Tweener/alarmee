package com.tweener.alarmee

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import com.tweener.alarmee.channel.AlarmeeNotificationChannel
import com.tweener.alarmee.channel.NotificationChannelRegister
import com.tweener.alarmee.configuration.AlarmeeAndroidPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.alarmee.notification.NotificationFactory
import com.tweener.kmpkit.kotlinextensions.getAlarmManager
import com.tweener.kmpkit.kotlinextensions.getNotificationManager
import com.tweener.kmpkit.kotlinextensions.now
import com.tweener.kmpkit.kotlinextensions.toEpochMilliseconds
import kotlinx.datetime.LocalDateTime

/**
 * @author Vivien Mahe
 * @since 06/11/2024
 */

@Composable
actual fun rememberAlarmeeScheduler(platformConfiguration: AlarmeePlatformConfiguration): AlarmeeScheduler {
    requirePlatformConfiguration(providedPlatformConfiguration = platformConfiguration, targetPlatformConfiguration = AlarmeeAndroidPlatformConfiguration::class)

    val context = LocalContext.current

    return remember {
        AlarmeeSchedulerAndroid(
            context = context,
            configuration = platformConfiguration,
        )
    }
}

@SuppressLint("ComposableNaming")
class AlarmeeSchedulerAndroid(
    private val context: Context,
    private val configuration: AlarmeeAndroidPlatformConfiguration,
) : AlarmeeScheduler() {

    override fun scheduleAlarm(alarmee: Alarmee, onSuccess: () -> Unit) {
        createNotificationChannels()
        validateNotificationChannelId(alarmee = alarmee)

        val pendingIntent = getPendingIntent(alarmee = alarmee)

        // Schedule the alarm
        context.getAlarmManager()?.let { alarmManager ->
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmee.scheduledDateTime!!.toEpochMilliseconds(timeZone = alarmee.timeZone), pendingIntent)

            // Notification scheduled successfully
            onSuccess()
        }
    }

    override fun scheduleRepeatingAlarm(alarmee: Alarmee, repeatInterval: RepeatInterval, onSuccess: () -> Unit) {
        createNotificationChannels()
        validateNotificationChannelId(alarmee = alarmee)

        val pendingIntent = getPendingIntent(alarmee = alarmee)

        // Schedule the alarm according to the repeat interval
        val intervalMillis = when (repeatInterval) {
            is RepeatInterval.Hourly -> AlarmManager.INTERVAL_HOUR
            is RepeatInterval.Daily -> AlarmManager.INTERVAL_DAY
            is RepeatInterval.Weekly -> AlarmManager.INTERVAL_DAY * 7
            is RepeatInterval.Monthly -> AlarmManager.INTERVAL_DAY * 30
            is RepeatInterval.Yearly -> AlarmManager.INTERVAL_DAY * 30 * 12
            is RepeatInterval.Custom -> repeatInterval.duration.inWholeMilliseconds
        }

        context.getAlarmManager()?.let { alarmManager ->
            val triggerAtMillis = (alarmee.scheduledDateTime ?: LocalDateTime.now(timeZone = alarmee.timeZone)).toEpochMilliseconds(timeZone = alarmee.timeZone)
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, pendingIntent)

            // Notification scheduled successfully
            onSuccess()
        }
    }

    override fun cancelAlarm(uuid: String) {
        // Create the receiver intent with the alarm parameters
        val receiverIntent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
            action = NotificationBroadcastReceiver.ALARM_ACTION
            putExtra(NotificationBroadcastReceiver.KEY_UUID, uuid)
        }

        // Create the broadcast pending intent
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            uuid.hashCode(),
            receiverIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Cancel the alarm with AlarmManager
        context.getAlarmManager()?.let { alarmManager ->
            alarmManager.cancel(pendingIntent)

            // Notification canceled successfully
            println("Notification with ID '$uuid' canceled.")
        }
    }

    override fun pushAlarm(alarmee: Alarmee, onSuccess: () -> Unit) {
        createNotificationChannels()
        validateNotificationChannelId(alarmee)

        alarmee.androidNotificationConfiguration.channelId?.let { channelId ->
            val priority = mapPriority(priority = alarmee.androidNotificationConfiguration.priority)

            // If the alarmee doesn't have a specific icon or color, use the default configuration
            val notificationResId = alarmee.androidNotificationConfiguration.iconResId ?: configuration.notificationIconResId
            val notificationIconColor = alarmee.androidNotificationConfiguration.iconColor ?: configuration.notificationIconColor

            val notification = NotificationFactory.create(
                context = context,
                channelId = channelId,
                title = alarmee.notificationTitle,
                body = alarmee.notificationBody,
                priority = priority,
                iconResId = notificationResId,
                iconColor = notificationIconColor.toArgb(),
                soundFilename = configuration.notificationChannels.firstOrNull { it.id == channelId }?.soundFilename,
                deepLinkUri = alarmee.deepLinkUri,
            )

            context.getNotificationManager()?.let { notificationManager ->
                if (notificationManager.areNotificationsEnabled()) {
                    notificationManager.notify(alarmee.uuid.hashCode(), notification)

                    // Notification sent
                    onSuccess()
                } else {
                    println("Notifications permission is not granted! Can't show the notification.")
                }
            }
        }
    }

    private fun createNotificationChannels() {
        // Create a notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            require(configuration.notificationChannels.isNotEmpty()) { "At least one ${AlarmeeNotificationChannel::class.simpleName} must be provided." }

            val notificationChannelRegister = NotificationChannelRegister(context = context)

            configuration.notificationChannels.forEach { channel ->
                notificationChannelRegister.register(id = channel.id, name = channel.name, importance = channel.importance, soundFilename = channel.soundFilename)
            }
        }
    }

    /**
     * Makes sure the notification channel ID exists (only for devices running on Android 0 and above).
     */
    private fun validateNotificationChannelId(alarmee: Alarmee) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Make sure channelId is not null
            requireNotNull(alarmee.androidNotificationConfiguration.channelId) { "androidNotificationConfiguration.channelId must not be null to schedule an Alarmee." }

            // Make sure channel exists
            context.getNotificationManager()?.let { notificationManager ->
                val channelExists = notificationManager.notificationChannels.any { it.id == alarmee.androidNotificationConfiguration.channelId }
                require(channelExists) { "The Alarmee is set with a notification channel (ID: ${alarmee.androidNotificationConfiguration.channelId}) that doest not exist." }
            }
        }
    }

    private fun getPendingIntent(alarmee: Alarmee): PendingIntent {
        val priority = mapPriority(priority = alarmee.androidNotificationConfiguration.priority)
        val soundFilename = configuration.notificationChannels.firstOrNull { it.id == alarmee.androidNotificationConfiguration.channelId }?.soundFilename

        // If the alarmee doesn't have a specific icon or color, use the default configuration
        val notificationResId = alarmee.androidNotificationConfiguration.iconResId ?: configuration.notificationIconResId
        val notificationIconColor = alarmee.androidNotificationConfiguration.iconColor ?: configuration.notificationIconColor

        // Create the receiver intent with the alarm parameters
        val receiverIntent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
            action = NotificationBroadcastReceiver.ALARM_ACTION
            putExtra(NotificationBroadcastReceiver.KEY_UUID, alarmee.uuid)
            putExtra(NotificationBroadcastReceiver.KEY_TITLE, alarmee.notificationTitle)
            putExtra(NotificationBroadcastReceiver.KEY_BODY, alarmee.notificationBody)
            putExtra(NotificationBroadcastReceiver.KEY_PRIORITY, priority)
            putExtra(NotificationBroadcastReceiver.KEY_CHANNEL_ID, alarmee.androidNotificationConfiguration.channelId)
            putExtra(NotificationBroadcastReceiver.KEY_ICON_RES_ID, notificationResId)
            putExtra(NotificationBroadcastReceiver.KEY_ICON_COLOR, notificationIconColor.toArgb())
            putExtra(NotificationBroadcastReceiver.KEY_SOUND_FILENAME, soundFilename)
            putExtra(NotificationBroadcastReceiver.KEY_DEEP_LINK_URI, alarmee.deepLinkUri)
        }

        // Create the broadcast pending intent
        return PendingIntent.getBroadcast(
            context,
            alarmee.uuid.hashCode(),
            receiverIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun mapPriority(priority: AndroidNotificationPriority): Int =
        when (priority) {
            AndroidNotificationPriority.MINIMUM -> NotificationCompat.PRIORITY_MIN
            AndroidNotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            AndroidNotificationPriority.DEFAULT -> NotificationCompat.PRIORITY_DEFAULT
            AndroidNotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            AndroidNotificationPriority.MAXIMUM -> NotificationCompat.PRIORITY_MAX
        }
}

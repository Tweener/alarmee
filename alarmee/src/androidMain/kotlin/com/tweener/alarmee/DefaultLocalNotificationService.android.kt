package com.tweener.alarmee

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.tweener.alarmee._internal.applicationContext
import com.tweener.alarmee.channel.NotificationChannelRegister
import com.tweener.alarmee.configuration.AlarmeeAndroidPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.alarmee.model.Alarmee
import com.tweener.alarmee.model.AndroidNotificationPriority
import com.tweener.alarmee.model.NotificationAction
import com.tweener.alarmee.model.RepeatInterval
import org.json.JSONArray
import org.json.JSONObject
import com.tweener.alarmee.notification.NotificationFactory
import com.tweener.alarmee.reveicer.NotificationBroadcastReceiver
import com.tweener.kmpkit.kotlinextensions.getAlarmManager
import com.tweener.kmpkit.kotlinextensions.getNotificationManager
import com.tweener.kmpkit.kotlinextensions.now
import com.tweener.kmpkit.kotlinextensions.toEpochMilliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

/**
 * @author Vivien Mahe
 * @since 06/11/2024
 */

const val DEFAULT_NOTIFICATION_CHANNEL_ID = "defaultNotificationChannelId"
private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

actual fun createLocalNotificationService(config: AlarmeePlatformConfiguration): LocalNotificationService {
    requirePlatformConfiguration(providedPlatformConfiguration = config, targetPlatformConfiguration = AlarmeeAndroidPlatformConfiguration::class)
    createNotificationChannels(config = config)
    return DefaultLocalNotificationService(config = config)
}

actual fun scheduleAlarm(alarmee: Alarmee, config: AlarmeePlatformConfiguration, onSuccess: () -> Unit) {
    requirePlatformConfiguration(providedPlatformConfiguration = config, targetPlatformConfiguration = AlarmeeAndroidPlatformConfiguration::class)
    validateNotificationChannelId(alarmee = alarmee)

    val pendingIntent = getPendingIntent(alarmee = alarmee, config = config)

    // Schedule the alarm
    applicationContext.getAlarmManager()?.let { alarmManager ->
        val triggerAtMillis = alarmee.scheduledDateTime!!.toEpochMilliseconds(timeZone = alarmee.timeZone)

        if (config.useExactScheduling && canScheduleExactAlarms(alarmManager)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }

        // Notification scheduled successfully
        onSuccess()
    }
}

actual fun scheduleRepeatingAlarm(alarmee: Alarmee, repeatInterval: RepeatInterval, config: AlarmeePlatformConfiguration, onSuccess: () -> Unit) {
    requirePlatformConfiguration(providedPlatformConfiguration = config, targetPlatformConfiguration = AlarmeeAndroidPlatformConfiguration::class)
    validateNotificationChannelId(alarmee = alarmee)

    val pendingIntent = getPendingIntent(alarmee = alarmee, config = config)

    // Schedule the alarm according to the repeat interval
    val intervalMillis = when (repeatInterval) {
        is RepeatInterval.Hourly -> AlarmManager.INTERVAL_HOUR
        is RepeatInterval.Daily -> AlarmManager.INTERVAL_DAY
        is RepeatInterval.Weekly -> AlarmManager.INTERVAL_DAY * 7
        is RepeatInterval.Monthly -> AlarmManager.INTERVAL_DAY * 30
        is RepeatInterval.Yearly -> AlarmManager.INTERVAL_DAY * 30 * 12
        is RepeatInterval.Custom -> repeatInterval.duration.inWholeMilliseconds
    }

    applicationContext.getAlarmManager()?.let { alarmManager ->
        val triggerAtMillis = (alarmee.scheduledDateTime ?: LocalDateTime.now(timeZone = alarmee.timeZone)).toEpochMilliseconds(timeZone = alarmee.timeZone)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, pendingIntent)

        // Notification scheduled successfully
        onSuccess()
    }
}

actual fun cancelAlarm(uuid: String, config: AlarmeePlatformConfiguration) {
    requirePlatformConfiguration(providedPlatformConfiguration = config, targetPlatformConfiguration = AlarmeeAndroidPlatformConfiguration::class)

    // Create the receiver intent with the alarm parameters
    val receiverIntent = Intent(applicationContext, NotificationBroadcastReceiver::class.java).apply {
        action = NotificationBroadcastReceiver.ALARM_ACTION
        putExtra(NotificationBroadcastReceiver.KEY_UUID, uuid)
    }

    // Create the broadcast pending intent
    val pendingIntent = PendingIntent.getBroadcast(
        applicationContext,
        uuid.hashCode(),
        receiverIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    // Cancel the alarm with AlarmManager
    applicationContext.getAlarmManager()?.let { alarmManager ->
        alarmManager.cancel(pendingIntent)

        // Notification canceled successfully
        println("Notification with ID '$uuid' canceled.")
    }
}

actual fun cancelAllAlarms(config: AlarmeePlatformConfiguration) {
    requirePlatformConfiguration(providedPlatformConfiguration = config, targetPlatformConfiguration = AlarmeeAndroidPlatformConfiguration::class)

    // Cancel all pending notifications
    applicationContext.getNotificationManager()?.let { notificationManager ->
        notificationManager.cancelAll()
    }

    // Note: Android doesn't provide a direct way to cancel all AlarmManager alarms
    // Individual alarms need to be canceled using their specific PendingIntents
    // For a complete solution, apps would need to maintain a registry of active alarm UUIDs
    println("All notifications canceled.")
}

actual fun immediateAlarm(alarmee: Alarmee, config: AlarmeePlatformConfiguration, onSuccess: () -> Unit) {
    requirePlatformConfiguration(providedPlatformConfiguration = config, targetPlatformConfiguration = AlarmeeAndroidPlatformConfiguration::class)
    validateNotificationChannelId(alarmee)

    alarmee.androidNotificationConfiguration.channelId?.let { channelId ->
        val priority = mapPriority(priority = alarmee.androidNotificationConfiguration.priority)

        // If the alarmee doesn't have a specific icon or color, use the default configuration
        val notificationResId = alarmee.androidNotificationConfiguration.iconResId ?: config.notificationIconResId
        val notificationIconColor = alarmee.androidNotificationConfiguration.iconColor ?: config.notificationIconColor

        scope.launch {
            val notification = NotificationFactory.create(
                context = applicationContext,
                channelId = channelId,
                title = alarmee.notificationTitle,
                body = alarmee.notificationBody,
                priority = priority,
                iconResId = notificationResId,
                iconColor = notificationIconColor.toArgb(),
                soundFilename = config.notificationChannels.firstOrNull { it.id == channelId }?.soundFilename,
                deepLinkUri = alarmee.deepLinkUri,
                imageUrl = alarmee.imageUrl,
                notificationUuid = alarmee.uuid,
                actions = alarmee.actions,
            )

            applicationContext.getNotificationManager()?.let { notificationManager ->
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
}

/**
 * Creates notification channels based on the provided configuration. If no channels are provided, a default channel is created.
 */
private fun createNotificationChannels(config: AlarmeePlatformConfiguration) {
    requirePlatformConfiguration(providedPlatformConfiguration = config, targetPlatformConfiguration = AlarmeeAndroidPlatformConfiguration::class)

    // Create a notification channel for Android O and above
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannelRegister = NotificationChannelRegister(context = applicationContext)

        config.notificationChannels.forEach { channel ->
            notificationChannelRegister.register(id = channel.id, name = channel.name, importance = channel.importance, soundFilename = channel.soundFilename)
        }

        // Add a default notification channel if none is provided
        if (config.notificationChannels.none { it.id == DEFAULT_NOTIFICATION_CHANNEL_ID }) {
            notificationChannelRegister.register(
                id = DEFAULT_NOTIFICATION_CHANNEL_ID,
                name = "Default Notification Channel",
                importance = NotificationManager.IMPORTANCE_HIGH,
                soundFilename = null,
            )
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
        applicationContext.getNotificationManager()?.let { notificationManager ->
            val channelExists = notificationManager.notificationChannels.any { it.id == alarmee.androidNotificationConfiguration.channelId }
            require(channelExists) { "The Alarmee is set with a notification channel (ID: ${alarmee.androidNotificationConfiguration.channelId}) that doest not exist." }
        }
    }
}

private fun getPendingIntent(alarmee: Alarmee, config: AlarmeePlatformConfiguration): PendingIntent {
    requirePlatformConfiguration(providedPlatformConfiguration = config, targetPlatformConfiguration = AlarmeeAndroidPlatformConfiguration::class)

    val priority = mapPriority(priority = alarmee.androidNotificationConfiguration.priority)
    val soundFilename = config.notificationChannels.firstOrNull { it.id == alarmee.androidNotificationConfiguration.channelId }?.soundFilename

    // If the alarmee doesn't have a specific icon or color, use the default configuration
    val notificationResId = alarmee.androidNotificationConfiguration.iconResId ?: config.notificationIconResId
    val notificationIconColor = alarmee.androidNotificationConfiguration.iconColor ?: config.notificationIconColor

    // Create the receiver intent with the alarm parameters
    val receiverIntent = Intent(applicationContext, NotificationBroadcastReceiver::class.java).apply {
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
        putExtra(NotificationBroadcastReceiver.KEY_IMAGE_URL, alarmee.imageUrl)

        // Serialize actions to JSON
        if (alarmee.actions.isNotEmpty()) {
            putExtra(NotificationBroadcastReceiver.KEY_ACTIONS_JSON, serializeActions(alarmee.actions))
        }
    }

    // Create the broadcast pending intent
    return PendingIntent.getBroadcast(
        applicationContext,
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

/**
 * Checks if the app can schedule exact alarms.
 * On Android 12+ (API 31+), this requires the SCHEDULE_EXACT_ALARM permission to be granted.
 */
private fun canScheduleExactAlarms(alarmManager: AlarmManager): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

/**
 * Serializes a list of notification actions to JSON format.
 */
private fun serializeActions(actions: List<NotificationAction>): String {
    val jsonArray = JSONArray()
    actions.forEach { action ->
        val jsonObject = JSONObject().apply {
            put("id", action.id)
            put("label", action.label)
            action.iconResId?.let { put("iconResId", it) }
        }
        jsonArray.put(jsonObject)
    }
    return jsonArray.toString()
}

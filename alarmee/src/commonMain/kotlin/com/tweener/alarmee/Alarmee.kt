package com.tweener.alarmee

import androidx.compose.ui.graphics.Color
import com.tweener.alarmee.AndroidNotificationPriority.DEFAULT
import com.tweener.alarmee.AndroidNotificationPriority.HIGH
import com.tweener.alarmee.AndroidNotificationPriority.LOW
import com.tweener.alarmee.AndroidNotificationPriority.MAXIMUM
import com.tweener.alarmee.AndroidNotificationPriority.MINIMUM
import com.tweener.alarmee.serializer.ColorSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable

/**
 * Data class representing the configuration for an alarm, including scheduling time and notification details.
 *
 * @property uuid A unique identifier for the alarm.
 * @property notificationTitle The title of the notification that will be displayed when the alarm triggers.
 * @property notificationBody The body of the notification that will be displayed when the alarm triggers.
 * @property scheduledDateTime The specific date and time when the alarm is scheduled to trigger.
 * @property timeZone The time zone in which the alarm should be scheduled. By default, this is set to the system's current time zone.
 * @property repeatInterval The optional interval at which the alarm should repeat (e.g., hourly, daily, weekly). If `null`, the alarm will not repeat.
 * @property androidNotificationConfiguration Configuration specific to Android notifications, including channel ID and priority.
 *
 * @author Vivien Mahe
 * @since 06/11/2024
 */
@Serializable
data class Alarmee(
    val uuid: String,
    val notificationTitle: String,
    val notificationBody: String,
    val scheduledDateTime: LocalDateTime,
    val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    val repeatInterval: RepeatInterval? = null,
    val androidNotificationConfiguration: AndroidNotificationConfiguration,
)

/**
 * Data class representing Android-specific configuration for notifications.
 *
 * @property priority The priority level of the notification (e.g., low, default, high). Determines how prominently the notification is displayed.
 * @property channelId The notification channel to post the notification on. Required for Android 8.0 (API level 26) and above.
 */
@Serializable
data class AndroidNotificationConfiguration(
    val priority: AndroidNotificationPriority = DEFAULT,
    val channelId: String? = null,
    val notificationIconResId: Int? = null,
    @Serializable(with = ColorSerializer::class) val notificationIconColor: Color? = null,
)

/**
 * Enum representing the priority levels for Android notifications.
 *
 * @property MINIMUM Lowest priority for notifications, used for less prominent alerts.
 * @property LOW Lower priority for notifications that require minimal attention.
 * @property DEFAULT Default priority level for most notifications.
 * @property HIGH Higher priority for important notifications.
 * @property MAXIMUM Highest priority for urgent notifications that demand immediate attention.
 */
enum class AndroidNotificationPriority {
    MINIMUM,
    LOW,
    DEFAULT,
    HIGH,
    MAXIMUM
}

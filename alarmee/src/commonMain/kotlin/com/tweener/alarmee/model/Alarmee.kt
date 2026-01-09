package com.tweener.alarmee.model

import androidx.compose.ui.graphics.Color
import com.tweener.alarmee.model.AndroidNotificationPriority.DEFAULT
import com.tweener.alarmee.model.AndroidNotificationPriority.HIGH
import com.tweener.alarmee.model.AndroidNotificationPriority.LOW
import com.tweener.alarmee.model.AndroidNotificationPriority.MAXIMUM
import com.tweener.alarmee.model.AndroidNotificationPriority.MINIMUM
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

/**
 * Data class representing the configuration for an alarm, including scheduling time and notification details.
 *
 * @property uuid A unique identifier for the alarm.
 * @property notificationTitle The title of the notification that will be displayed when the alarm triggers.
 * @property notificationBody The body of the notification that will be displayed when the alarm triggers.
 * @property scheduledDateTime The specific date and time when the alarm is scheduled to trigger. Optional for pushing the notification immediately
 * @property timeZone The time zone in which the alarm should be scheduled. By default, this is set to the system's current time zone.
 * @property repeatInterval The optional interval at which the alarm should repeat (e.g., hourly, daily, weekly). If `null`, the alarm will not repeat.
 * @property deepLinkUri An optional URI that can be used to open a specific screen in the app when the notification is tapped. This is useful for deep linking into the app.
 * @property imageUrl An optional URL for an image to be displayed in the notification. This can enhance the visual appeal of the notification.
 * @property actions A list of action buttons to display on the notification. Android supports up to 3 action buttons; any extras will be ignored.
 * @property androidNotificationConfiguration Configuration specific to Android notifications.
 * @property iosNotificationConfiguration Configuration specific to iOS notifications.
 *
 * @author Vivien Mahe
 * @since 06/11/2024
 */
data class Alarmee(
    val uuid: String,
    val notificationTitle: String,
    val notificationBody: String,
    val scheduledDateTime: LocalDateTime? = null,
    val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    val repeatInterval: RepeatInterval? = null,
    val deepLinkUri: String? = null,
    val imageUrl: String? = null,
    val actions: List<NotificationAction> = emptyList(),
    val androidNotificationConfiguration: AndroidNotificationConfiguration,
    val iosNotificationConfiguration: IosNotificationConfiguration,
)

/**
 * Data class representing Android-specific configuration for notifications.
 *
 * @property priority The priority level of the notification (e.g., low, default, high). Determines how prominently the notification is displayed.
 * @property channelId The notification channel to post the notification on. Required for Android 8.0 (API level 26) and above.
 * @property iconResId The resource ID of the icon to display in the notification.
 * @property iconColor The color of the icon to display in the notification.
 */
data class AndroidNotificationConfiguration(
    val priority: AndroidNotificationPriority = DEFAULT,
    val channelId: String? = null,
    val iconResId: Int? = null,
    val iconColor: Color? = null,
)

/**
 * Data class representing iOS-specific configuration for notifications.
 *
 * @property soundFilename The name of the sound file to play when the notification is triggered. Sound files must be less than 30 seconds in length. If the sound file is longer than 30 seconds, the system plays the default sound instead.
 * @property badge The badge number to display on the app icon. If `0`, the badge will be removed. If `null`, the badge will not be changed.
 */
data class IosNotificationConfiguration(
    val soundFilename: String? = null,
    val badge: Int? = null,
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

package com.tweener.alarmee

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tweener.alarmee.configuration.AlarmeeIosPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.kmpkit.kotlinextensions.toEpochMilliseconds
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationTrigger
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

/**
 * @author Vivien Mahe
 * @since 06/11/2024
 */

@Composable
actual fun rememberAlarmeeScheduler(platformConfiguration: AlarmeePlatformConfiguration): AlarmeeScheduler {
    requirePlatformConfiguration(providedPlatformConfiguration = platformConfiguration, targetPlatformConfiguration = AlarmeeIosPlatformConfiguration::class)

    return remember { AlarmeeSchedulerIos(configuration = platformConfiguration) }
}

class AlarmeeSchedulerIos(
    private val configuration: AlarmeeIosPlatformConfiguration = AlarmeeIosPlatformConfiguration,
) : AlarmeeScheduler() {

    override fun scheduleAlarm(alarmee: Alarmee, onSuccess: () -> Unit) {
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents = alarmee.scheduledDateTime!!.toNSDateComponents(), repeats = false)

        configureNotification(uuid = alarmee.uuid, alarmee = alarmee, notificationTrigger = trigger, onSuccess = onSuccess)
    }

    override fun scheduleRepeatingAlarm(alarmee: Alarmee, repeatInterval: RepeatInterval, onSuccess: () -> Unit) {
        // Schedule the first notification at the start date
        val firstTrigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents = alarmee.scheduledDateTime!!.toNSDateComponents(), repeats = false)

        // Create a specific uuid for the one-off notification, so it is different from the one for the repeating notification.
        // Two notifications with the same identifier will overwrite each other, therefore, the repeating notification overwrites the one-off notification before the first one has a chance to trigger.
        val firstRepeatingUuid = getFirstRepeatingNotificationUuid(uuid = alarmee.uuid)

        configureNotification(
            uuid = firstRepeatingUuid,
            alarmee = alarmee,
            notificationTrigger = firstTrigger,
            onSuccess = {
                // Schedule subsequent notifications at the repeat interval
                val repeatTrigger = if (repeatInterval is RepeatInterval.Custom) {
                    UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(timeInterval = repeatInterval.duration.inWholeMilliseconds / 1000.0, repeats = true)
                } else {
                    val dateComponents = NSDateComponents()
                    dateComponents.calendar = NSCalendar.currentCalendar

                    dateComponents.second = alarmee.scheduledDateTime.second.toLong()
                    dateComponents.minute = alarmee.scheduledDateTime.minute.toLong()

                    when (repeatInterval) {
                        is RepeatInterval.Custom -> Unit // Nothing to do, already handled before
                        is RepeatInterval.Hourly -> Unit // No need to set specific date or hour; repeats every hour

                        is RepeatInterval.Daily -> {
                            dateComponents.hour = alarmee.scheduledDateTime.hour.toLong()
                        }

                        is RepeatInterval.Weekly -> {
                            dateComponents.hour = alarmee.scheduledDateTime.hour.toLong()
                            dateComponents.weekday = alarmee.scheduledDateTime.dayOfWeek.isoDayNumber.toLong()
                        }

                        is RepeatInterval.Monthly -> {
                            dateComponents.hour = alarmee.scheduledDateTime.hour.toLong()
                            dateComponents.day = alarmee.scheduledDateTime.dayOfMonth.toLong()
                        }

                        is RepeatInterval.Yearly -> {
                            dateComponents.hour = alarmee.scheduledDateTime.hour.toLong()
                            dateComponents.day = alarmee.scheduledDateTime.dayOfMonth.toLong()
                            dateComponents.month = alarmee.scheduledDateTime.monthNumber.toLong()
                        }
                    }

                    UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents = dateComponents, repeats = true)
                }

                configureNotification(uuid = alarmee.uuid, alarmee = alarmee, notificationTrigger = repeatTrigger, onSuccess = {})

                onSuccess()
            }
        )
    }

    override fun cancelAlarm(uuid: String) {
        val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(identifiers = listOf(uuid, getFirstRepeatingNotificationUuid(uuid = uuid)))
    }

    override fun pushAlarmee(alarmee: Alarmee, onSuccess: () -> Unit) {
        val trigger = null

        configureNotification(
            uuid = alarmee.uuid,
            alarmee = alarmee,
            notificationTrigger = trigger,
            onSuccess = onSuccess
        )
    }

    private fun configureNotification(uuid: String, alarmee: Alarmee, notificationTrigger: UNNotificationTrigger?, onSuccess: () -> Unit) {
        val content = UNMutableNotificationContent().apply {
            setTitle(alarmee.notificationTitle)
            setBody(alarmee.notificationBody)
        }

        val request = UNNotificationRequest.requestWithIdentifier(identifier = uuid, content = content, trigger = notificationTrigger)

        val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
        notificationCenter.requestAuthorizationWithOptions(options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge) { granted, authorizationError ->
            if (granted) {
                // Schedule the notification
                notificationCenter.addNotificationRequest(request) { requestError ->
                    if (requestError != null) {
                        println("Scheduling notification on iOS failed with error: $requestError")
                    }

                    // Notification scheduled successfully
                    onSuccess()
                }
            } else if (authorizationError != null) {
                println("Error requesting notification permission: $authorizationError")
            }
        }
    }

    private fun LocalDateTime.toNSDateComponents(timeZone: TimeZone = TimeZone.currentSystemDefault()): NSDateComponents {
        val nsDateTime = NSDate.dateWithTimeIntervalSince1970(secs = this.toEpochMilliseconds(timeZone = timeZone) / 1000.0)

        return NSCalendar.currentCalendar.components(
            unitFlags = NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond,
            fromDate = nsDateTime,
        )
    }

    private fun getFirstRepeatingNotificationUuid(uuid: String) = "${uuid}_${uuid}"
}

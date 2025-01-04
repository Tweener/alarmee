package com.tweener.alarmee

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.russhwolf.settings.Settings
import com.tweener.alarmee.configuration.AlarmeeIosPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationTrigger
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
) : AlarmeeScheduler(), KoinComponent {

    companion object {
        const val ALARMEE_UUID = "alarmee_uuid"
        const val ALARMEE_IS_REPEATING = "alarmee_is_repeating"
    }

    init {
        val schedulerModule = module {
            single { this@AlarmeeSchedulerIos }
        }
        loadKoinModules(schedulerModule)
    }

    override fun getSettings(): Settings = get()

    override fun scheduleAlarm(alarmee: Alarmee) {
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents = alarmee.scheduledDateTime.toNSDateComponents(timeZone = alarmee.timeZone), repeats = false)

        configureNotification(uuid = alarmee.uuid, alarmee = alarmee, notificationTrigger = trigger, onScheduleSuccess = {})
    }

    override fun scheduleRepeatingAlarm(alarmee: Alarmee, repeatInterval: RepeatInterval) {
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents = alarmee.scheduledDateTime.toNSDateComponents(timeZone = alarmee.timeZone), repeats = false)

        configureNotification(uuid = alarmee.uuid, alarmee = alarmee, notificationTrigger = trigger, onScheduleSuccess = {})

//        configureNotification(
//            uuid = alarmee.uuid,
//            alarmee = alarmee,
//            notificationTrigger = trigger,
//            onScheduleSuccess = {
//                // Schedule subsequent notifications at the repeat interval
//                val repeatTrigger = if (repeatInterval is RepeatInterval.Custom) {
//                    UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(timeInterval = repeatInterval.duration.inWholeMilliseconds / 1000.0, repeats = true)
//                } else {
//                    val dateComponents = NSDateComponents()
//                    dateComponents.calendar = NSCalendar.currentCalendar
//
//                    dateComponents.second = alarmee.scheduledDateTime.second.toLong()
//                    dateComponents.minute = alarmee.scheduledDateTime.minute.toLong()
//
//                    when (repeatInterval) {
//                        is RepeatInterval.Custom -> Unit // Nothing to do, already handled before
//                        is RepeatInterval.Hourly -> Unit // No need to set specific date or hour; repeats every hour
//
//                        is RepeatInterval.Daily -> {
//                            dateComponents.hour = alarmee.scheduledDateTime.hour.toLong()
//                        }
//
//                        is RepeatInterval.Weekly -> {
//                            dateComponents.hour = alarmee.scheduledDateTime.hour.toLong()
//                            dateComponents.weekday = alarmee.scheduledDateTime.dayOfWeek.isoDayNumber.toLong()
//                        }
//
//                        is RepeatInterval.Monthly -> {
//                            dateComponents.hour = alarmee.scheduledDateTime.hour.toLong()
//                            dateComponents.day = alarmee.scheduledDateTime.dayOfMonth.toLong()
//                        }
//
//                        is RepeatInterval.Yearly -> {
//                            dateComponents.hour = alarmee.scheduledDateTime.hour.toLong()
//                            dateComponents.day = alarmee.scheduledDateTime.dayOfMonth.toLong()
//                            dateComponents.month = alarmee.scheduledDateTime.monthNumber.toLong()
//                        }
//                    }
//
//                    UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents = dateComponents, repeats = true)
//                }
//
//                configureNotification(uuid = alarmee.uuid, alarmee = alarmee, notificationTrigger = repeatTrigger, onScheduleSuccess = {})
//            }
//        )
    }

    override fun cancelAlarm(uuid: String) {
        val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(identifiers = listOf(uuid))
    }

    private fun configureNotification(uuid: String, alarmee: Alarmee, notificationTrigger: UNNotificationTrigger, onScheduleSuccess: () -> Unit) {
        val isRepeating: Boolean = alarmee.repeatInterval != null

        val content = UNMutableNotificationContent().apply {
            setTitle(alarmee.notificationTitle)
            setBody(alarmee.notificationBody)
            setUserInfo(
                mapOf(
                    ALARMEE_UUID to alarmee.uuid,
                    ALARMEE_IS_REPEATING to (alarmee.repeatInterval != null),
                )
            )
        }

        val request = UNNotificationRequest.requestWithIdentifier(identifier = uuid, content = content, trigger = notificationTrigger)

        val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
        notificationCenter.requestAuthorizationWithOptions(options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge) { granted, authorizationError ->
            if (granted) {
                // Schedule the notification
                notificationCenter.addNotificationRequest(request) { requestError ->
                    if (requestError != null) {
                        println("Scheduling notification on iOS failed with error: $requestError")
                    } else {
                        // Notification scheduled successfully
                        onScheduleSuccess()
                    }
                }
            } else if (authorizationError != null) {
                println("Error requesting notification permission: $authorizationError")
            }
        }
    }

    private fun LocalDateTime.toNSDateComponents(timeZone: TimeZone = TimeZone.currentSystemDefault()): NSDateComponents =
        NSCalendar.currentCalendar.components(
            unitFlags = NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond,
            fromDate = this.toNSDate(timeZone = timeZone),
        )
}

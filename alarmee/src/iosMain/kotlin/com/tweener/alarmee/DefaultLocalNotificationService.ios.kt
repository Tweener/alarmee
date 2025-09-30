package com.tweener.alarmee

import com.tweener.alarmee.configuration.AlarmeeIosPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.alarmee.model.Alarmee
import com.tweener.alarmee.model.RepeatInterval
import com.tweener.kmpkit.kotlinextensions.toEpochMilliseconds
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.temporaryDirectory
import platform.Foundation.writeToURL
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationAttachment
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNNotificationTrigger
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

/**
 * @author Vivien Mahe
 * @since 06/11/2024
 */

private const val DEEP_LINK_URI_PARAM = "deepLinkUri"

actual fun createLocalNotificationService(config: AlarmeePlatformConfiguration): LocalNotificationService {
    requirePlatformConfiguration(providedPlatformConfiguration = config, targetPlatformConfiguration = AlarmeeIosPlatformConfiguration::class)
    return DefaultLocalNotificationService(config = config)
}

actual fun scheduleAlarm(alarmee: Alarmee, config: AlarmeePlatformConfiguration, onSuccess: () -> Unit) {
    val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents = alarmee.scheduledDateTime!!.toNSDateComponents(), repeats = false)

    configureNotification(uuid = alarmee.uuid, alarmee = alarmee, notificationTrigger = trigger, onScheduleSuccess = onSuccess)
}

actual fun scheduleRepeatingAlarm(alarmee: Alarmee, repeatInterval: RepeatInterval, config: AlarmeePlatformConfiguration, onSuccess: () -> Unit) {
    val trigger = if (repeatInterval is RepeatInterval.Custom) {
        UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(timeInterval = repeatInterval.duration.inWholeMilliseconds / 1000.0, repeats = true)
    } else {
        val dateComponents = NSDateComponents()
        dateComponents.calendar = NSCalendar.currentCalendar

        dateComponents.second = alarmee.scheduledDateTime!!.second.toLong()
        dateComponents.minute = alarmee.scheduledDateTime.minute.toLong()

        when (repeatInterval) {
            is RepeatInterval.Custom -> Unit // Nothing to do, already handled before
            is RepeatInterval.Hourly -> Unit // No need to set specific date or hour; repeats every hour

            is RepeatInterval.Daily -> {
                dateComponents.hour = alarmee.scheduledDateTime.hour.toLong()
            }

            is RepeatInterval.Weekly -> {
                dateComponents.hour = alarmee.scheduledDateTime.hour.toLong() 

                val kotlinIsoDayNumber: Int = alarmee.scheduledDateTime.dayOfWeek.isoDayNumber

                val swiftCompatibleWeekday: Int
                if (kotlinIsoDayNumber == 7) { 
                    swiftCompatibleWeekday = 1 
                } else {
                    swiftCompatibleWeekday = kotlinIsoDayNumber + 1 
                }
                
                dateComponents.weekday = swiftCompatibleWeekday.toLong()
            }

            is RepeatInterval.Monthly -> {
                dateComponents.hour = alarmee.scheduledDateTime.hour.toLong()
                dateComponents.day = alarmee.scheduledDateTime.day.toLong()
            }

            is RepeatInterval.Yearly -> {
                dateComponents.hour = alarmee.scheduledDateTime.hour.toLong()
                dateComponents.day = alarmee.scheduledDateTime.day.toLong()
                dateComponents.month = alarmee.scheduledDateTime.month.number.toLong()
            }
        }

        UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(dateComponents = dateComponents, repeats = true)
    }

    configureNotification(uuid = alarmee.uuid, alarmee = alarmee, notificationTrigger = trigger, onScheduleSuccess = onSuccess)
}

actual fun cancelAlarm(uuid: String, config: AlarmeePlatformConfiguration) {
    val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    notificationCenter.removePendingNotificationRequestsWithIdentifiers(identifiers = listOf(uuid, getFirstRepeatingNotificationUuid(uuid = uuid)))
}

actual fun cancelAllAlarms(config: AlarmeePlatformConfiguration) {
    val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    notificationCenter.removeAllPendingNotificationRequests()
    println("All scheduled notifications canceled on iOS.")
}

actual fun immediateAlarm(alarmee: Alarmee, config: AlarmeePlatformConfiguration, onSuccess: () -> Unit) {
    configureNotification(uuid = alarmee.uuid, alarmee = alarmee, onScheduleSuccess = onSuccess)
}

private fun configureNotification(uuid: String, alarmee: Alarmee, notificationTrigger: UNNotificationTrigger? = null, onScheduleSuccess: () -> Unit) {
    val content = UNMutableNotificationContent().apply {
        setTitle(alarmee.notificationTitle)
        setBody(alarmee.notificationBody)
        alarmee.iosNotificationConfiguration.soundFilename?.let { setSound(UNNotificationSound.soundNamed(name = it)) }
        alarmee.iosNotificationConfiguration.badge?.let { setBadge(NSNumber(int = it)) }
        alarmee.deepLinkUri?.let { setUserInfo(mapOf(DEEP_LINK_URI_PARAM to it)) }

        // Add the image as attachment if available
        alarmee.imageUrl?.let { imageUrl ->
            val attachment = downloadImageAsAttachment(imageUrl)
            attachment?.let { setAttachments(listOf(it)) }
        }
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

private fun LocalDateTime.toNSDateComponents(timeZone: TimeZone = TimeZone.currentSystemDefault()): NSDateComponents {
    val nsDateTime = NSDate.dateWithTimeIntervalSince1970(secs = this.toEpochMilliseconds(timeZone = timeZone) / 1000.0)

    return NSCalendar.currentCalendar.components(
        unitFlags = NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond,
        fromDate = nsDateTime,
    )
}

private fun getFirstRepeatingNotificationUuid(uuid: String) = "${uuid}_${uuid}"

@OptIn(ExperimentalForeignApi::class)
private fun downloadImageAsAttachment(imageUrl: String): UNNotificationAttachment? {
    val url = NSURL.URLWithString(imageUrl) ?: return null
    val data = NSData.dataWithContentsOfURL(url) ?: return null

    val tmpDir = NSFileManager.defaultManager.temporaryDirectory
    val tmpFile = tmpDir.URLByAppendingPathComponent("${NSUUID().UUIDString}.jpg") ?: return null

    return if (data.writeToURL(tmpFile, true)) {
        runCatching {
            UNNotificationAttachment.attachmentWithIdentifier(
                identifier = "image",
                URL = tmpFile,
                options = null,
                error = null
            )
        }.getOrNull()
    } else {
        null
    }
}

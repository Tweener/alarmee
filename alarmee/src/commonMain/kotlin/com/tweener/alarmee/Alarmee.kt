package com.tweener.alarmee

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

/**
 * Data class representing the configuration for an alarm, including scheduling time and notification details.
 *
 * @property uuid A unique identifier for the alarm.
 * @property notificationTitle The title of the notification that will be displayed when the alarm triggers.
 * @property notificationBody The body of the notification that will be displayed when the alarm triggers.
 * @property scheduledDateTime The specific date and time when the alarm is scheduled to trigger.
 * @property timeZone The time zone in which the alarm should be scheduled. By default, this is set to the system's current time zone.
 *
 * @author Vivien Mahe
 * @since 06/11/2024
 */
data class Alarmee(
    val uuid: String,
    val notificationTitle: String,
    val notificationBody: String,
    val scheduledDateTime: LocalDateTime,
    val timeZone: TimeZone = TimeZone.currentSystemDefault(),
)

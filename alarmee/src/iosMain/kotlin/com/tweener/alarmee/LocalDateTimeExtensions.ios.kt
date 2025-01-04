package com.tweener.alarmee

import com.tweener.kmpkit.kotlinextensions.toEpochMilliseconds
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970

/**
 * @author Vivien Mahe
 * @since 03/01/2025
 */

fun LocalDateTime.toNSDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): NSDate =
    NSDate.dateWithTimeIntervalSince1970(secs = this.toEpochMilliseconds(timeZone = timeZone) / 1000.0)

package com.tweener.alarmee

import kotlinx.serialization.Serializable
import kotlin.time.Duration

/**
 * Represents the repeat interval for an alarm.
 * It provides predefined intervals (e.g., hourly, daily) and supports custom intervals.
 *
 * @author Vivien Mahe
 * @since 18/11/2024
 */
@Serializable
sealed class RepeatInterval {
    @Serializable
    data object Hourly : RepeatInterval()

    @Serializable
    data object Daily : RepeatInterval()

    @Serializable
    data object Weekly : RepeatInterval()

    @Serializable
    data object Monthly : RepeatInterval()

    @Serializable
    data object Yearly : RepeatInterval()

    @Serializable
    data class Custom(val duration: Duration) : RepeatInterval()
}

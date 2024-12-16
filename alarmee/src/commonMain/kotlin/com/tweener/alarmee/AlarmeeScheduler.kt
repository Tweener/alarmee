package com.tweener.alarmee

import androidx.compose.runtime.Composable
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.common._internal.kotlinextensions.now
import com.tweener.common._internal.kotlinextensions.plus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass

/**
 * Creates an [AlarmeeScheduler] instance and remembers it.
 */
@Composable
expect fun rememberAlarmeeScheduler(platformConfiguration: AlarmeePlatformConfiguration): AlarmeeScheduler

/**
 * Class to schedule and manage alarms that trigger at specified times of day.
 * Alarms can be configured with various parameters such as time, time zone, and notification content.
 *
 * When the alarm triggers, a notification will be displayed with the specified title and body.
 *
 * @author Vivien Mahe
 * @since 05/11/2024
 */
abstract class AlarmeeScheduler {

    /**
     * Schedules an alarm to be triggered at a specific time of the day. When the alarm is triggered, a notification will be displayed.
     *
     * @param alarmee The [Alarmee] object containing the configuration for the alarm.
     */
    fun schedule(alarmee: Alarmee) {
        // Schedule an alarm with the correct calculated time
        val scheduledDateTime = adjustDateInFuture(alarmee)
        val updatedAlarmee = alarmee.copy(scheduledDateTime = scheduledDateTime)

        updatedAlarmee.repeatInterval
            ?.let { repeatInterval ->
                scheduleRepeatingAlarm(alarmee = updatedAlarmee, repeatInterval = repeatInterval) {
                    val message = when (repeatInterval) {
                        RepeatInterval.HOURLY -> "every hour at minute: ${scheduledDateTime.minute}"
                        RepeatInterval.DAILY -> "every day at ${scheduledDateTime.time}"
                        RepeatInterval.WEEKLY -> "every week on ${scheduledDateTime.dayOfWeek} at ${scheduledDateTime.time}"
                        RepeatInterval.MONTHLY -> "every month on day ${scheduledDateTime.dayOfMonth} at ${scheduledDateTime.time}"
                        RepeatInterval.YEARLY -> "every year on the ${scheduledDateTime.month}/${scheduledDateTime.dayOfMonth} at ${scheduledDateTime.time}"
                    }

                    println("Notification with title '${updatedAlarmee.notificationTitle}' scheduled $message.")
                }
            }
            ?: run {
                scheduleAlarm(alarmee = alarmee) {
                    println("Notification with title '${updatedAlarmee.notificationTitle}' scheduled at ${updatedAlarmee.scheduledDateTime}.")
                }
            }
    }

    /**
     * Cancels an existing alarm based on its unique identifier.
     * If an alarm with the specified identifier is found, it will be canceled, preventing any future notifications from being triggered for that alarm.
     *
     * @param uuid The unique identifier for the alarm to be canceled.
     */
    fun cancel(uuid: String) {
        cancelAlarm(uuid = uuid)
    }

    internal abstract fun scheduleAlarm(alarmee: Alarmee, onSuccess: () -> Unit)

    internal abstract fun scheduleRepeatingAlarm(alarmee: Alarmee, repeatInterval: RepeatInterval, onSuccess: () -> Unit)

    internal abstract fun cancelAlarm(uuid: String)

    /**
     * Adjusts the scheduled date and time to the future if it's in the past.
     * - For one-off alarms, adjusts to the next day.
     * - For repeating alarms, adjusts to the next valid occurrence based on the repeat interval.
     *
     * @param alarmee The alarm configuration containing the scheduled date and repeat interval.
     * @return The adjusted date and time in the future.
     */
    private fun adjustDateInFuture(alarmee: Alarmee): LocalDateTime {
        val now = LocalDateTime.now(timeZone = alarmee.timeZone)

        return if (alarmee.scheduledDateTime <= now) {
            val adjustedDateTime = if (alarmee.repeatInterval == null) {
                // One-off alarm: adjust to tomorrow
                now.plus(1, DateTimeUnit.DAY, timeZone = alarmee.timeZone)
            } else {
                // Repeating alarm: adjust to the next valid occurrence
                val dateTimeUnit = when (alarmee.repeatInterval) {
                    RepeatInterval.HOURLY -> DateTimeUnit.HOUR
                    RepeatInterval.DAILY -> DateTimeUnit.DAY
                    RepeatInterval.WEEKLY -> DateTimeUnit.WEEK
                    RepeatInterval.MONTHLY -> DateTimeUnit.MONTH
                    RepeatInterval.YEARLY -> DateTimeUnit.YEAR
                }

                now.plus(1, dateTimeUnit, timeZone = alarmee.timeZone)
            }

            println("The scheduled date and time (${alarmee.scheduledDateTime}) was in the past. It has been adjusted to the future: $adjustedDateTime")

            adjustedDateTime
        } else {
            alarmee.scheduledDateTime
        }
    }
}

/**
 * Ensures that the provided platform configuration matches the specified target platform type and throws an [IllegalArgumentException] if it does not.
 *
 * @param T The expected type of [AlarmeePlatformConfiguration].
 * @param providedPlatformConfiguration The platform configuration instance to be checked.
 * @param targetPlatformConfiguration The `KClass` of the expected platform configuration type.
 *
 * @throws IllegalArgumentException if [providedPlatformConfiguration] is not an instance of [targetPlatformConfiguration].
 *
 * Example usage:
 * ```
 * val configuration: AlarmeePlatformConfiguration = // obtain configuration
 * requirePlatformConfiguration(configuration, AlarmeePlatformConfiguration.Ios::class)
 * // Now `configuration` is smart-cast to `AlarmeePlatformConfiguration.Ios`
 * ```
 */
@OptIn(ExperimentalContracts::class)
internal inline fun <reified T : AlarmeePlatformConfiguration> requirePlatformConfiguration(
    providedPlatformConfiguration: AlarmeePlatformConfiguration,
    targetPlatformConfiguration: KClass<T>,
) {
    contract {
        returns() implies (providedPlatformConfiguration is T)
    }

    if (providedPlatformConfiguration !is T) {
        throw IllegalArgumentException("Expected ${targetPlatformConfiguration::class.simpleName}")
    }
}

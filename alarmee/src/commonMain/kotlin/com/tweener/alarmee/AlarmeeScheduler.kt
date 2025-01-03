package com.tweener.alarmee

import androidx.compose.runtime.Composable
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.kmpkit.kotlinextensions.now
import com.tweener.kmpkit.kotlinextensions.plus
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
        validateAlarmee(alarmee = alarmee, schedule = true)

        val scheduledDateTime = adjustDateInFuture(alarmee)
        val updatedAlarmee = alarmee.copy(scheduledDateTime = scheduledDateTime)

        updatedAlarmee.repeatInterval
            ?.let { repeatInterval ->
                scheduleRepeatingAlarm(alarmee = updatedAlarmee, repeatInterval = repeatInterval) {
                    val message = when (repeatInterval) {
                        is RepeatInterval.Hourly -> "every hour at minute: ${scheduledDateTime.minute}"
                        is RepeatInterval.Daily -> "every day at ${scheduledDateTime.time}"
                        is RepeatInterval.Weekly -> "every week on ${scheduledDateTime.dayOfWeek} at ${scheduledDateTime.time}"
                        is RepeatInterval.Monthly -> "every month on day ${scheduledDateTime.dayOfMonth} at ${scheduledDateTime.time}"
                        is RepeatInterval.Yearly -> "every year on the ${scheduledDateTime.month}/${scheduledDateTime.dayOfMonth} at ${scheduledDateTime.time}"
                        is RepeatInterval.Custom -> "every ${repeatInterval.duration} from ${scheduledDateTime.time}"
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
     * Pushes a notification immediately to the device without scheduling an alarm.
     *
     * @param alarmee The [Alarmee] object containing the configuration for the alarm.
     */
    fun push(alarmee: Alarmee) {
        validateAlarmee(alarmee)

        pushAlarmee(alarmee = alarmee) {
            println("Notification with title '${alarmee.notificationTitle}' successfully sent.")
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

    internal abstract fun pushAlarmee(alarmee: Alarmee, onSuccess: () -> Unit)

    private fun validateAlarmee(alarmee: Alarmee, schedule: Boolean = false) {

        if (schedule) {
            require(alarmee.scheduledDateTime != null) { "scheduledDateTime is required for scheduling alarms." }
        }

        if (alarmee.repeatInterval != null) {
            require(alarmee.scheduledDateTime != null) { "scheduledDateTime is required for repeating alarms." }
        }

        if (alarmee.repeatInterval is RepeatInterval.Custom) {
            require(alarmee.repeatInterval.duration.isPositive()) { "Custom repeat interval duration must be greater than zero." }
        }
    }

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

        return if (alarmee.scheduledDateTime!! <= now) {
            val adjustedDateTime = if (alarmee.repeatInterval == null) {
                // One-off alarm: adjust to tomorrow
                now.plus(1, DateTimeUnit.DAY, timeZone = alarmee.timeZone)
            } else {
                // Repeating alarm: adjust to the next valid occurrence
                when (alarmee.repeatInterval) {
                    is RepeatInterval.Hourly -> now.plus(value = 1, unit = DateTimeUnit.HOUR, timeZone = alarmee.timeZone)
                    is RepeatInterval.Daily -> now.plus(value = 1, unit = DateTimeUnit.DAY, timeZone = alarmee.timeZone)
                    is RepeatInterval.Weekly -> now.plus(value = 1, unit = DateTimeUnit.WEEK, timeZone = alarmee.timeZone)
                    is RepeatInterval.Monthly -> now.plus(value = 1, unit = DateTimeUnit.MONTH, timeZone = alarmee.timeZone)
                    is RepeatInterval.Yearly -> now.plus(value = 1, unit = DateTimeUnit.YEAR, timeZone = alarmee.timeZone)
                    is RepeatInterval.Custom -> now.plus(duration = alarmee.repeatInterval.duration, timeZone = alarmee.timeZone)
                }
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

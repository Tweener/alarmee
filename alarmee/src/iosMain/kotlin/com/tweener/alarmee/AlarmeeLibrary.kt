package com.tweener.alarmee

import com.tweener.alarmee.di.sharedIosModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSNumber
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationResponse

/**
 * @author Vivien Mahe
 * @since 03/01/2025
 */
object AlarmeeLibrary : KoinComponent {

    private const val TASK_IDENTIFIER = "com.tweener.alarmee.notificationRefresh"

    private val alarmeeScheduler: AlarmeeSchedulerIos by inject()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var scheduleNextCheckJob: Job? = null

    fun initialize() {
        startKoin {
            modules(sharedIosModule)
        }

        registerBackgroundNotificationsTask()
    }

    fun onNotificationWillPresent(notification: UNNotification) {
        println("AlarmeeLibrary - onNotificationWillPresent")

        val userInfo = notification.request.content.userInfo

        // If the alarmee is repeating, we need to schedule the next notification
        val alarmeeIsRepeating = userInfo[AlarmeeSchedulerIos.ALARMEE_IS_REPEATING] as NSNumber
        if (alarmeeIsRepeating.boolValue) {
            val alarmeeUuid = userInfo[AlarmeeSchedulerIos.ALARMEE_UUID] as String
            alarmeeScheduler.rescheduleAlarm(alarmeeUuid = alarmeeUuid)
        }
    }

    fun onNotificationDidReceive(response: UNNotificationResponse) {
        println("AlarmeeLibrary - onNotificationDidReceive")

        val userInfo = response.notification.request.content.userInfo

        // If the alarmee is repeating, we need to schedule the next notification
        val alarmeeIsRepeating = userInfo[AlarmeeSchedulerIos.ALARMEE_IS_REPEATING] as NSNumber
        if (alarmeeIsRepeating.boolValue) {
            val alarmeeUuid = userInfo[AlarmeeSchedulerIos.ALARMEE_UUID] as String
            alarmeeScheduler.rescheduleAlarm(alarmeeUuid = alarmeeUuid)
        }
    }

    private fun registerBackgroundNotificationsTask() {
        println("AlarmeeLibrary - registerBackgroundNotificationsTask")

        BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(identifier = TASK_IDENTIFIER, usingQueue = null) { task ->
            println("AlarmeeLibrary - registerBackgroundNotificationsTask - task: $task")

            scheduleNextCheckJob?.cancel()
            scheduleNextCheckJob = scope.launch {
                println("AlarmeeLibrary - registerBackgroundNotificationsTask - scheduling next check")

                alarmeeScheduler.nextScheduledAlarmee.collect { nextScheduledAlarmee ->
                    println("AlarmeeLibrary - registerBackgroundNotificationsTask - nextScheduledAlarmee: $nextScheduledAlarmee")

                    // Schedule the background task for the next scheduled alarmee
                    val request = BGAppRefreshTaskRequest(identifier = TASK_IDENTIFIER)
                    request.earliestBeginDate = nextScheduledAlarmee.scheduledDateTime.toNSDate(timeZone = nextScheduledAlarmee.timeZone)
                }
            }

            // Perform the task
            alarmeeScheduler.rescheduleAllAlarms()
            task?.setTaskCompletedWithSuccess(success = true)

            // Add an expiration handler to cancel the task if it exceeds time limits
            task?.expirationHandler = {
                println("Background task expired.")

                scheduleNextCheckJob?.cancel()
                task?.setTaskCompletedWithSuccess(success = false)
            }
        }
    }
}

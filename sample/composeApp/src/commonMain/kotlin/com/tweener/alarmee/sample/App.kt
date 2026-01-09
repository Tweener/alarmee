package com.tweener.alarmee.sample

/**
 * @author Vivien Mahe
 * @since 25/11/2024
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tweener.alarmee.MobileAlarmeeService
import com.tweener.alarmee.model.Alarmee
import com.tweener.alarmee.model.AndroidNotificationConfiguration
import com.tweener.alarmee.model.AndroidNotificationPriority
import com.tweener.alarmee.model.IosNotificationConfiguration
import com.tweener.alarmee.model.NotificationAction
import com.tweener.alarmee.model.RepeatInterval
import com.tweener.alarmee.rememberAlarmeeMobileService
import com.tweener.alarmee.sample.ui.theme.AlarmeeTheme
import com.tweener.kmpkit.kotlinextensions.now
import com.tweener.kmpkit.kotlinextensions.plus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun App() {
    val alarmService: MobileAlarmeeService = rememberAlarmeeMobileService(platformConfiguration = createAlarmeePlatformConfiguration())
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Register callback for notification action button clicks (Android-specific, no-op on iOS)
    alarmService.local.registerActionCallback()

    // Example of how to get the Firebase push token
    scope.launch {
        alarmService.push.let { pushService ->
            // Get the Firebase Installation ID, if available
            val installationId = pushService.getInstallationId()
            println("🆔 Firebase Installation ID: ${installationId.getOrNull() ?: "Error: ${installationId.exceptionOrNull()}"}")

            // Register for token updates
            pushService.onNewToken { newToken ->
                println("🔥 New Firebase token received: $newToken")
            }

            // Register for push message callbacks
            pushService.onPushMessageReceived { payload ->
                println("📩 Push message received with payload: $payload")
            }
        }
    }

    AlarmeeTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = 16.dp, alignment = Alignment.CenterVertically),
            ) {
                Button(onClick = {
                    alarmService.local.schedule(
                        alarmee = Alarmee(
                            uuid = "myOneOffAlarmId",
                            notificationTitle = "🎉 Congratulations! You've schedule a one-off Alarmee!",
                            notificationBody = "This is the notification that will be displayed 10 seconds from now.",
                            scheduledDateTime = LocalDateTime.now().plus(10.seconds),
                            androidNotificationConfiguration = AndroidNotificationConfiguration(
                                priority = AndroidNotificationPriority.DEFAULT,
                                channelId = "dailyNewsChannelId",
                            ),
                            iosNotificationConfiguration = IosNotificationConfiguration(),
                        )
                    )
                }) { Text("Set a one-off Alarmee") }

                Button(onClick = {
                    val now = LocalDateTime.now()
                    val scheduledDateTime = LocalDateTime(year = now.year, month = now.month, day = now.day, hour = 9, minute = 36, second = 0)

                    alarmService.local.schedule(
                        alarmee = Alarmee(
                            uuid = "myRepeatingAlarmId",
                            notificationTitle = "🔁 Congratulations! You've schedule a daily repeating Alarmee!",
                            notificationBody = "This notification will be displayed every day at 09:36.",
                            scheduledDateTime = scheduledDateTime,
                            repeatInterval = RepeatInterval.Daily,
                            androidNotificationConfiguration = AndroidNotificationConfiguration(
                                priority = AndroidNotificationPriority.HIGH,
                                channelId = "breakingNewsChannelId",
                            ),
                            iosNotificationConfiguration = IosNotificationConfiguration(),
                        )
                    )
                }) { Text("Set a daily repeating Alarmee") }

                Button(onClick = {
                    alarmService.local.schedule(
                        alarmee = Alarmee(
                            uuid = "myRepeatingAlarmId",
                            notificationTitle = "🔁 Congratulations! You've schedule a custom repeating Alarmee!",
                            notificationBody = "This is the notification that will be displayed every 15 minutes.",
                            repeatInterval = RepeatInterval.Custom(duration = 15.minutes),
                            androidNotificationConfiguration = AndroidNotificationConfiguration(
                                priority = AndroidNotificationPriority.MAXIMUM,
                                channelId = "breakingNewsChannelId",
                            ),
                            iosNotificationConfiguration = IosNotificationConfiguration(),
                        )
                    )
                }) { Text("Set a custom repeating Alarmee") }

                Button(onClick = {
                    alarmService.local.immediate(
                        alarmee = Alarmee(
                            uuid = "immediateNotificationId",
                            notificationTitle = "🚀 Immediate Notification",
                            notificationBody = "This is an immediate notification pushed without any schedule.",
                            deepLinkUri = "https://www.example.com",
                            imageUrl = "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
                            androidNotificationConfiguration = AndroidNotificationConfiguration(
                                priority = AndroidNotificationPriority.MINIMUM,
                                channelId = "immediateChannelId",
                            ),
                            iosNotificationConfiguration = IosNotificationConfiguration(),
                        )
                    )
                }) { Text("Send notification now") }

                Button(onClick = {
                    alarmService.local.immediate(
                        alarmee = Alarmee(
                            uuid = "soundNotificationId",
                            notificationTitle = "🔈 Notification with custom sound",
                            notificationBody = "This is a notification with a custom sound.",
                            androidNotificationConfiguration = AndroidNotificationConfiguration(
                                priority = AndroidNotificationPriority.HIGH,
                                channelId = "soundChannelId",
                            ),
                            iosNotificationConfiguration = IosNotificationConfiguration(
                                soundFilename = "notifications.wav",
                            ),
                        )
                    )
                }) { Text("Send a notification with sound") }

                Button(onClick = {
                    alarmService.local.immediate(
                        alarmee = Alarmee(
                            uuid = "actionNotificationId",
                            notificationTitle = "📩 New Message",
                            notificationBody = "You have a new message! Tap an action to respond.",
                            actions = listOf(
                                NotificationAction(id = "reply", label = "Reply"),
                                NotificationAction(id = "mark_read", label = "Mark as Read"),
                                NotificationAction(id = "dismiss", label = "Dismiss"),
                            ),
                            androidNotificationConfiguration = AndroidNotificationConfiguration(
                                priority = AndroidNotificationPriority.HIGH,
                                channelId = "immediateChannelId",
                            ),
                            iosNotificationConfiguration = IosNotificationConfiguration(),
                        )
                    )
                }) { Text("Send a notification with actions") }

                Button(onClick = {
                    scope.launch {
                        alarmService.push.let { pushService ->
                            pushService.getToken()
                                .onSuccess { token -> println("Current Firebase token: $token") }
                                .onFailure { throwable -> println("Failed to get Firebase token: $throwable") }
                        }
                    }
                }) { Text("Get current Firebase token") }

                Button(onClick = {
                    scope.launch {
                        alarmService.push.let { pushService ->
                            println("🔄 Manual token refresh requested...")
                            pushService.forceTokenRefresh()
                                .onSuccess { token -> println("🔄 Token refresh successful: ${token.take(10)}...") }
                                .onFailure { throwable -> println("🔄 Token refresh failed: $throwable") }
                        }
                    }
                }) { Text("Force token refresh") }

                Button(onClick = {
                    alarmService.local.cancel("myOneOffAlarmId")
                }) { Text("Cancel one-off Alarmee") }

                Button(onClick = {
                    alarmService.local.cancelAll()
                }) { Text("Cancel all Alarmees") }
            }
        }
    }
}

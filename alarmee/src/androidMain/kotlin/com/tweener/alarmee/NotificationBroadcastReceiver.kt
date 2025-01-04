package com.tweener.alarmee

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.tweener.alarmee.android.R
import com.tweener.kmpkit.kotlinextensions.getNotificationManager
import com.tweener.kmpkit.safeLet
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * @author Vivien Mahe
 * @since 06/11/2024
 */
class NotificationBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    private val alarmeeScheduler: AlarmeeSchedulerAndroid by inject()

    companion object {
        const val ALARM_ACTION = "com.tweener.alarmee.SET_ALARM"
        const val KEY_UUID = "notificationUuid"
        const val KEY_TITLE = "notificationTitle"
        const val KEY_BODY = "notificationBody"
        const val KEY_PRIORITY = "notificationPriority"
        const val KEY_CHANNEL_ID = "notificationChannelId"
        const val KEY_ICON_RES_ID = "notificationIconResId"
        const val KEY_ICON_COLOR = "notificationColor"
        const val KEY_IS_REPEATING = "notificationIsRepeating"

        private val DEFAULT_ICON_RES_ID = R.drawable.ic_notification
        private val DEFAULT_ICON_COLOR = Color.Transparent
        private const val DEFAULT_IS_REPEATING = false
        private const val DEFAULT_PRIORITY = NotificationCompat.PRIORITY_DEFAULT
        private const val DEFAULT_CHANNEL_ID = "notificationsChannelId"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Make sure the intent is the one we're expecting
        if (intent.action != ALARM_ACTION) return

        safeLet(
            intent.getStringExtra(KEY_UUID),
            intent.getStringExtra(KEY_TITLE),
            intent.getStringExtra(KEY_BODY),
        ) { uuid, title, body ->
            val priority = intent.getIntExtra(KEY_PRIORITY, DEFAULT_PRIORITY)
            val iconResId = intent.getIntExtra(KEY_ICON_RES_ID, DEFAULT_ICON_RES_ID)
            val iconColor = intent.getIntExtra(KEY_ICON_COLOR, DEFAULT_ICON_COLOR.toArgb())

            // For devices running on Android before Android 0, channelId passed through intents might be null so we used a default channelId that will be ignored
            val channelId = intent.getStringExtra(KEY_CHANNEL_ID) ?: DEFAULT_CHANNEL_ID

            // Create the notification
            val notification = NotificationCompat.Builder(context, channelId)
                .apply {
                    setSmallIcon(iconResId)
                    setContentTitle(title)
                    setContentText(body)
                    setPriority(priority)
                    setColor(iconColor)
                    setAutoCancel(true)
                    setContentIntent(getPendingIntent(context = context)) // Handles click on notification
                }
                .build()

            // Display the notification
            context.getNotificationManager()?.let { notificationManager ->
                if (notificationManager.areNotificationsEnabled()) {
                    notificationManager.notify(uuid.hashCode(), notification)
                } else {
                    println("Notifications permission is not granted! Can't show the notification.")
                }
            }

            // If the alarmee is repeating, we need to schedule the next notification
            val isRepeating = intent.getBooleanExtra(KEY_IS_REPEATING, DEFAULT_IS_REPEATING)
            if (isRepeating) {
                alarmeeScheduler.rescheduleAlarm(alarmeeUuid = uuid)
            }
        }
    }

    private fun getPendingIntent(context: Context): PendingIntent? {
        val intent = context.getLauncherActivityIntent()?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun Context.getLauncherActivityIntent(): Intent? = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
}

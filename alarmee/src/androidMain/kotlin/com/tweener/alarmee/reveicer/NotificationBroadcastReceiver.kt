package com.tweener.alarmee.reveicer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.tweener.alarmee.android.R
import com.tweener.alarmee.model.NotificationAction
import com.tweener.alarmee.notification.NotificationFactory
import com.tweener.kmpkit.kotlinextensions.getNotificationManager
import com.tweener.kmpkit.utils.safeLet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray

/**
 * @author Vivien Mahe
 * @since 06/11/2024
 */
class NotificationBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ALARM_ACTION = "com.tweener.alarmee.SET_ALARM"
        const val KEY_UUID = "notificationUuid"
        const val KEY_TITLE = "notificationTitle"
        const val KEY_BODY = "notificationBody"
        const val KEY_PRIORITY = "notificationPriority"
        const val KEY_CHANNEL_ID = "notificationChannelId"
        const val KEY_ICON_RES_ID = "notificationIconResId"
        const val KEY_ICON_COLOR = "notificationColor"
        const val KEY_SOUND_FILENAME = "notificationSoundFilename"
        const val KEY_DEEP_LINK_URI = "notificationDeepLinkUri"
        const val KEY_IMAGE_URL = "notificationImageUrl"
        const val KEY_GROUP_KEY = "notificationGroupKey"
        const val KEY_IS_GROUP_SUMMARY = "notificationIsGroupSummary"
        const val KEY_ACTIONS_JSON = "notificationActionsJson"
        const val KEY_REPEAT_INTERVAL_MILLIS = "notificationRepeatIntervalMillis"
        const val KEY_NEXT_TRIGGER_MILLIS = "notificationNextTriggerMillis"

        val DEFAULT_ICON_RES_ID = R.drawable.ic_notification
        val DEFAULT_ICON_COLOR = Color.Transparent
        private const val DEFAULT_PRIORITY = NotificationCompat.PRIORITY_DEFAULT
        private const val DEFAULT_CHANNEL_ID = "notificationsChannelId"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ALARM_ACTION) {
            safeLet(
                intent.getStringExtra(KEY_UUID),
                intent.getStringExtra(KEY_TITLE),
                intent.getStringExtra(KEY_BODY),
            ) { uuid, title, body ->
                // Chain the next exact alarm before showing the notification,
                // so the chain continues even if notification display fails.
                val repeatIntervalMillis = intent.getLongExtra(KEY_REPEAT_INTERVAL_MILLIS, -1L)
                if (repeatIntervalMillis > 0) {
                    scheduleNextExactAlarm(context, intent, uuid, repeatIntervalMillis)
                }

                val priority = intent.getIntExtra(KEY_PRIORITY, DEFAULT_PRIORITY)
                val iconResId = intent.getIntExtra(KEY_ICON_RES_ID, DEFAULT_ICON_RES_ID)
                val iconColor = intent.getIntExtra(KEY_ICON_COLOR, DEFAULT_ICON_COLOR.toArgb())
                val soundFilename = intent.getStringExtra(KEY_SOUND_FILENAME)
                val deepLinkUri = intent.getStringExtra(KEY_DEEP_LINK_URI)
                val imageUrl = intent.getStringExtra(KEY_IMAGE_URL)
                val groupKey = intent.getStringExtra(KEY_GROUP_KEY)
                val isGroupSummary = intent.getBooleanExtra(KEY_IS_GROUP_SUMMARY, false)
                val actionsJson = intent.getStringExtra(KEY_ACTIONS_JSON)

                // Deserialize actions
                val actions = actionsJson?.let { deserializeActions(it) } ?: emptyList()

                // For devices running on Android before Android 0, channelId passed through intents might be null so we used a default channelId that will be ignored
                val channelId = intent.getStringExtra(KEY_CHANNEL_ID) ?: DEFAULT_CHANNEL_ID

                // Create the notification
                scope.launch {
                    val notification = NotificationFactory.create(
                        context = context,
                        channelId = channelId,
                        title = title,
                        body = body,
                        priority = priority,
                        iconResId = iconResId,
                        iconColor = iconColor,
                        soundFilename = soundFilename,
                        deepLinkUri = deepLinkUri,
                        imageUrl = imageUrl,
                        groupKey = groupKey,
                        isGroupSummary = isGroupSummary,
                        notificationUuid = uuid,
                        actions = actions,
                    )

                    // Display the notification
                    context.getNotificationManager()?.let { notificationManager ->
                        if (notificationManager.areNotificationsEnabled()) {
                            notificationManager.notify(uuid.hashCode(), notification)
                        } else {
                            println("Notifications permission is not granted! Can't show the notification.")
                        }
                    }
                }
            }
        }
    }

    /**
     * Schedules the next exact alarm for repeating notifications.
     * Uses one-shot exact chaining: after each alarm fires, the next one is scheduled.
     * Falls back to inexact scheduling if the SCHEDULE_EXACT_ALARM permission was revoked.
     */
    private fun scheduleNextExactAlarm(context: Context, originalIntent: Intent, uuid: String, intervalMillis: Long) {
        val now = System.currentTimeMillis()

        // Anchor the next trigger to the original schedule grid to avoid drift. Each alarm fires
        // slightly late (Doze wakeup latency, processing, exact-alarm throttling); computing the
        // next trigger from the actual receive time would let that error accumulate over time.
        // Fall back to now-based scheduling if the anchor is missing (e.g. an older chained intent).
        val anchoredNext = originalIntent.getLongExtra(KEY_NEXT_TRIGGER_MILLIS, -1L)
        var nextTriggerMillis = if (anchoredNext > 0) anchoredNext else now + intervalMillis

        // Catch up: if one or more occurrences were missed (device asleep/off, long Doze), skip
        // forward to the next future slot on the grid instead of firing a burst of past alarms.
        if (nextTriggerMillis <= now) {
            val missedIntervals = (now - nextTriggerMillis) / intervalMillis + 1
            nextTriggerMillis += missedIntervals * intervalMillis
        }

        // Clone the intent preserving all extras (notification data + repeat interval), updating
        // the anchor to the occurrence that follows the one we're scheduling now.
        val nextIntent = Intent(originalIntent).apply {
            setClass(context, NotificationBroadcastReceiver::class.java)
            putExtra(KEY_NEXT_TRIGGER_MILLIS, nextTriggerMillis + intervalMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            uuid.hashCode(),
            nextIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerMillis, pendingIntent)
        } else {
            // Permission was revoked — fall back to inexact
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerMillis, pendingIntent)
        }
    }

    private fun deserializeActions(json: String): List<NotificationAction> {
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                NotificationAction(
                    id = obj.getString("id"),
                    label = obj.getString("label"),
                    iconResId = if (obj.has("iconResId") && !obj.isNull("iconResId")) obj.getInt("iconResId") else null,
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

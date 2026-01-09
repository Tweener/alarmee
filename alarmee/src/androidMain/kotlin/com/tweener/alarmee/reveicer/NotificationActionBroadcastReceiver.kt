package com.tweener.alarmee.reveicer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tweener.alarmee.action.NotificationActionCallbackRegistry
import com.tweener.alarmee.model.NotificationActionEvent
import com.tweener.kmpkit.kotlinextensions.getNotificationManager

/**
 * BroadcastReceiver that handles notification action button clicks.
 *
 * @author Vivien Mahe
 * @since 09/01/2026
 */
class NotificationActionBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_NOTIFICATION_ACTION_CLICKED = "com.tweener.alarmee.ACTION_CLICKED"
        const val KEY_NOTIFICATION_UUID = "notificationUuid"
        const val KEY_ACTION_ID = "actionId"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_NOTIFICATION_ACTION_CLICKED) {
            val notificationUuid = intent.getStringExtra(KEY_NOTIFICATION_UUID) ?: return
            val actionId = intent.getStringExtra(KEY_ACTION_ID) ?: return

            // Dismiss the notification after action is clicked
            context.getNotificationManager()?.cancel(notificationUuid.hashCode())

            // Notify the registered callback
            val event = NotificationActionEvent(
                notificationUuid = notificationUuid,
                actionId = actionId,
            )

            NotificationActionCallbackRegistry.notifyActionClicked(event)
        }
    }
}

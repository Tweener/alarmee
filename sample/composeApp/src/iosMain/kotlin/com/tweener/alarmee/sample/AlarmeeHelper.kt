package com.tweener.alarmee.sample

import com.tweener.alarmee.PushNotificationServiceRegistry
import com.tweener.alarmee.action.NotificationActionCallbackRegistry

/**
 * @author Vivien Mahe
 * @since 05/06/2025
 */
class AlarmeeHelper {

    fun onNotificationReceived(userInfo: Map<Any?, *>?) {
        val parsed = userInfo
            ?.mapNotNull { (key, value) ->
                val k = key?.toString()
                val v = value?.toString()
                if (k != null && v != null) k to v else null
            }
            ?.toMap()
            ?: emptyMap()

        PushNotificationServiceRegistry.get()?.handleIncomingMessage(data = parsed)
    }

    /**
     * Called from Swift when a notification action button is tapped.
     *
     * @param notificationUuid The UUID of the notification (from userInfo["notificationUuid"]).
     * @param actionId The identifier of the action that was tapped (from response.actionIdentifier).
     */
    fun onActionClicked(notificationUuid: String, actionId: String) {
        NotificationActionCallbackRegistry.notifyActionClicked(
            notificationUuid = notificationUuid,
            actionId = actionId,
        )
    }
}

package com.tweener.alarmee

import com.tweener.alarmee.action.NotificationActionCallbackRegistry
import com.tweener.alarmee.model.NotificationActionEvent

/**
 * Registers a callback to be invoked when a notification action button is clicked.
 * Android-specific extension function.
 *
 * Note: The callback is stored in a singleton registry. Only one callback can be active at a time;
 * subsequent calls will replace the previous callback. It is recommended to register the callback
 * early in the app lifecycle (e.g., in Application.onCreate() or in a Composable with LaunchedEffect).
 *
 * @param callback The callback to invoke when an action is clicked. Receives a [NotificationActionEvent]
 *                 containing the notification UUID and the action ID.
 *
 * @author Vivien Mahe
 * @since 09/01/2026
 */
fun LocalNotificationService.onActionClicked(callback: (NotificationActionEvent) -> Unit) {
    NotificationActionCallbackRegistry.registerCallback(callback)
}

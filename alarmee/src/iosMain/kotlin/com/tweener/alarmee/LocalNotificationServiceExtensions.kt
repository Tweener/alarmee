package com.tweener.alarmee

import com.tweener.alarmee.action.NotificationActionCallbackRegistry
import com.tweener.alarmee.model.NotificationActionEvent

/**
 * Registers a callback to be invoked when a notification action button is clicked.
 * iOS-specific extension function.
 *
 * Note: The callback is stored in a singleton registry. Only one callback can be active at a time;
 * subsequent calls will replace the previous callback. It is recommended to register the callback
 * early in the app lifecycle.
 *
 * Important: To receive action callbacks on iOS, you must also update your AppDelegate to call
 * [NotificationActionCallbackRegistry.notifyActionClicked] when handling notification responses.
 * See the sample app's AppDelegate.swift for an example.
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

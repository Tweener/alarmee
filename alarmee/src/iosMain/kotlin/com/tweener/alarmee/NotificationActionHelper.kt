package com.tweener.alarmee

import com.tweener.alarmee.action.NotificationActionCallbackRegistry

/**
 * Helper class to be called from Swift when a notification action is tapped.
 * This bridges the Swift UNUserNotificationCenterDelegate to the Kotlin callback registry.
 *
 * Usage in AppDelegate.swift:
 * ```swift
 * func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
 *     let userInfo = response.notification.request.content.userInfo
 *     let actionIdentifier = response.actionIdentifier
 *
 *     // Handle action button taps (not default tap)
 *     if actionIdentifier != UNNotificationDefaultActionIdentifier && actionIdentifier != UNNotificationDismissActionIdentifier {
 *         if let notificationUuid = userInfo["notificationUuid"] as? String {
 *             NotificationActionHelper().onActionClicked(notificationUuid: notificationUuid, actionId: actionIdentifier)
 *         }
 *     }
 *
 *     completionHandler()
 * }
 * ```
 *
 * @author Vivien Mahe
 * @since 09/01/2026
 */
class NotificationActionHelper {

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

package com.tweener.alarmee.model

/**
 * Event data passed when a notification action button is clicked.
 *
 * @property notificationUuid The UUID of the notification that contained the clicked action.
 * @property actionId The ID of the action button that was clicked.
 *
 * @author Vivien Mahe
 * @since 09/01/2026
 */
data class NotificationActionEvent(
    val notificationUuid: String,
    val actionId: String,
)

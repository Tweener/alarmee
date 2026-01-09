package com.tweener.alarmee.model

/**
 * Represents an action button that can be displayed on a notification.
 * Android supports up to 3 action buttons per notification.
 *
 * @property id Unique identifier for this action. Used to identify which action was clicked in the callback.
 * @property label The text displayed on the action button.
 * @property iconResId Optional icon resource ID for the action button (Android-specific, ignored on iOS).
 *
 * @author Vivien Mahe
 * @since 09/01/2026
 */
data class NotificationAction(
    val id: String,
    val label: String,
    val iconResId: Int? = null,
)

package com.tweener.alarmee.action

import com.tweener.alarmee.model.NotificationActionEvent

/**
 * Singleton registry for notification action callbacks.
 * Apps register a callback once during initialization, and it gets invoked
 * whenever any notification action button is clicked.
 *
 * @author Vivien Mahe
 * @since 09/01/2026
 */
object NotificationActionCallbackRegistry {

    private var actionCallback: ((NotificationActionEvent) -> Unit)? = null

    /**
     * Registers a callback to be invoked when any notification action is clicked.
     * Only one callback can be registered at a time; subsequent calls replace the previous callback.
     *
     * @param callback The callback to invoke when an action is clicked.
     */
    fun registerCallback(callback: (NotificationActionEvent) -> Unit) {
        actionCallback = callback
    }

    /**
     * Unregisters the current callback.
     */
    fun unregisterCallback() {
        actionCallback = null
    }

    /**
     * Invokes the registered callback with the action event.
     * Called internally by NotificationActionBroadcastReceiver.
     */
    internal fun notifyActionClicked(event: NotificationActionEvent) {
        actionCallback?.invoke(event)
    }
}

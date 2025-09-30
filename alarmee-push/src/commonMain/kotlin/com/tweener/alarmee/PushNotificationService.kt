package com.tweener.alarmee

/**
 * Interface for managing push notifications (remote notifications).
 * Used for registering/unregistering push capabilities and handling incoming push data.
 *
 * This interface is only available on mobile targets (Android & iOS).
 *
 * @author Vivien Mahe
 * @since 05/06/2025
 */

interface PushNotificationService {

    /**
     * Unregisters the device from receiving push notifications.
     * Typically used when the user signs out or disables notifications.
     */
    fun unregister()

    /**
     * Handles a push message received from the platform.
     * This function parses the payload and shows a local notification.
     * 
     * Note: This method is intended for internal platform use only.
     *
     * @param data The key-value payload of the remote push message.
     */
    fun handleIncomingMessage(data: Map<String, String>)

    /**
     * Retrieves the Firebase Installation ID for this app instance.
     * The Installation ID is a unique identifier for the app installation on the device.
     *
     * @return A [Result] containing the Installation ID on success, or an exception on failure.
     */
    suspend fun getInstallationId(): Result<String>

    /**
     * Retrieves the current Firebase Cloud Messaging (FCM) token for the device.
     * This token uniquely identifies the app instance and is required for sending push notifications.
     *
     * On iOS, this method waits for the APNs token to be set before fetching the FCM token.
     *
     * @return A [Result] containing the FCM token on success, or an exception on failure.
     */
    suspend fun getToken(): Result<String>

    /**
     * Registers a callback to be invoked when a new FCM token is generated.
     * This is typically used to update the server with the new token.
     *
     * @param callback The function to call with the new token when it is available.
     */
    suspend fun onNewToken(callback: (String) -> Unit)

    /**
     * Forces a token refresh for testing purposes.
     * This will trigger the onNewToken callback if successful.
     *
     * Note: This is primarily for debugging and testing.
     */
    suspend fun forceTokenRefresh(): Result<String>

    /**
     * Registers a callback to be invoked when a push message is received.
     * This allows the app to handle push message payloads for custom processing.
     *
     * @param callback The function to call with the push message payload when it is received.
     */
    suspend fun onPushMessageReceived(callback: (Map<String, String>) -> Unit)
}

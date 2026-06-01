package com.tweener.alarmee

import com.tweener.alarmee.PushNotificationServiceRegistry.clear
import com.tweener.alarmee.PushNotificationServiceRegistry.get


/**
 * Registry to access the current [PushNotificationService] instance at runtime.
 *
 * This is typically used from platform-specific code (e.g., Firebase or APNs) to handle incoming push messages.
 *
 * The registration is handled internally by the Alarmee library.
 * You can call [get] to retrieve the active service and [clear] to reset it if needed.
 *
 * @author Vivien Mahe
 * @since 05/06/2025
 */
object PushNotificationServiceRegistry {

    private var service: PushNotificationService? = null

    /**
     * Returns the currently registered [PushNotificationService], or `null` if none is set.
     */
    fun get(): PushNotificationService? = service

    /**
     * Clears the registered service instance.
     */
    fun clear() {
        service = null
    }

    /**
     * Notifies the registered [PushNotificationService] that a new FCM token has been generated.
     *
     * On Android this is handled internally by Alarmee, but on iOS the host application is responsible
     * for bridging platform callbacks (e.g. from `AppDelegate`). Call this from iOS platform code when a
     * new token is received so the `onNewToken` callbacks are triggered.
     *
     * @param token The new FCM token.
     */
    fun notifyTokenUpdated(token: String) {
        (service as? DefaultPushNotificationService)?.notifyTokenUpdated(token)
    }

    /**
     * Notifies the registered [PushNotificationService] that a push message has been received.
     *
     * On Android this is handled internally by Alarmee, but on iOS the host application is responsible
     * for bridging platform callbacks (e.g. from `AppDelegate`). Call this from iOS platform code when a
     * push message is received so the `onPushMessageReceived` callbacks are triggered.
     *
     * @param data The key-value payload of the remote push message.
     */
    fun notifyIncomingMessage(data: Map<String, String>) {
        service?.handleIncomingMessage(data)
    }

    /**
     * Registers a [PushNotificationService]. This is called internally by the Alarmee library.
     */
    internal fun register(service: PushNotificationService) {
        this.service = service
    }
}

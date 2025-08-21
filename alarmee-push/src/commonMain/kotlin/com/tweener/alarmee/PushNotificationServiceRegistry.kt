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
     * Registers a [PushNotificationService]. This is called internally by the Alarmee library.
     */
    internal fun register(service: PushNotificationService) {
        this.service = service
    }
}

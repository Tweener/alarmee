package com.tweener.alarmee

import cocoapods.FirebaseMessaging.FIRMessaging
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.messaging.messaging
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay

/**
 * @author Vivien Mahe
 * @since 05/06/2025
 */

internal actual fun handleNotificationData(data: Map<String, String>) {
    // On iOS, we let the system display the notification. But we can still get the data attached to the notification.
    println("Handling notification data on iOS: $data")
}

@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun getFirebaseToken(): String {
    var attempts = 0
    val maxAttempts = 10
    val delayMillis = 500L

    while (FIRMessaging.messaging().APNSToken == null && attempts < maxAttempts) {
        delay(delayMillis)
        attempts++
    }

    return try {
        Firebase.messaging.getToken()
    } catch (throwable: Throwable) {
        throw throwable
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun forceFirebaseTokenRefresh(): String =
    try {
        // On iOS, delete token and get new one
        Firebase.messaging.deleteToken()
        delay(1000) // Give some time for token deletion to process

        // Wait for APNs token if needed
        var attempts = 0
        val maxAttempts = 10
        val delayMillis = 500L

        while (FIRMessaging.messaging().APNSToken == null && attempts < maxAttempts) {
            delay(delayMillis)
            attempts++
        }

        val newToken = Firebase.messaging.getToken()
        
        // On iOS, Firebase doesn't automatically trigger the delegate callback for manual token refresh
        // So we need to manually notify the service
        PushNotificationServiceRegistry.notifyTokenUpdated(newToken)

        newToken
    } catch (throwable: Throwable) {
        throw throwable
    }


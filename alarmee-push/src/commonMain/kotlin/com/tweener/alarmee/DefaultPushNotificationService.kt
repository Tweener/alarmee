package com.tweener.alarmee

import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.kmpkit.thread.suspendCatching
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.installations.installations
import dev.gitlive.firebase.messaging.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * @author Vivien Mahe
 * @since 05/06/2025
 */
internal class DefaultPushNotificationService(
    private val config: AlarmeePlatformConfiguration
) : PushNotificationService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val tokenCallbacks = mutableListOf<(String) -> Unit>()
    private val messageCallbacks = mutableListOf<(Map<String, String>) -> Unit>()

    override fun unregister() {
        scope.launch {
            Firebase.messaging.deleteToken()
            println("Firebase push token deleted")
        }
    }

    override fun handleIncomingMessage(data: Map<String, String>) {
        handleNotificationData(data)
        
        // Notify all registered message callbacks
        scope.launch {
            messageCallbacks.forEachIndexed { index, callback ->
                try {
                    callback(data)
                } catch (e: Exception) {
                    println("Error in message callback $index: $e")
                }
            }
        }
    }

    override suspend fun getInstallationId(): Result<String> = suspendCatching {
        Firebase.installations.getId()
    }.onFailure { throwable ->
        println("Error getting Firebase Installation ID: $throwable")
    }

    override suspend fun getToken(): Result<String> = suspendCatching {
        getFirebaseToken()
    }.onFailure { throwable ->
        println("Error getting Firebase token: $throwable")
    }

    override suspend fun onNewToken(callback: (String) -> Unit) {
        tokenCallbacks.add(callback)
    }

    override suspend fun onPushMessageReceived(callback: (Map<String, String>) -> Unit) {
        messageCallbacks.add(callback)
    }
    
    override suspend fun forceTokenRefresh(): Result<String> = suspendCatching {
        forceFirebaseTokenRefresh()
    }
    
    /**
     * Internal method to notify all registered callbacks when a new token is received.
     * This is called from platform-specific code when the token changes.
     */
    internal fun notifyTokenUpdated(token: String) {
        scope.launch {
            tokenCallbacks.forEachIndexed { index, callback ->
                try {
                    callback(token)
                } catch (e: Exception) {
                    println("Error in token callback $index: $e")
                }
            }
        }
    }
}

internal expect fun handleNotificationData(data: Map<String, String>)

internal expect suspend fun getFirebaseToken(): String

internal expect suspend fun forceFirebaseTokenRefresh(): String

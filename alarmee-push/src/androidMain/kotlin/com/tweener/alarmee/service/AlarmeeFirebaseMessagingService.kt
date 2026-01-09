package com.tweener.alarmee.service

import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tweener.alarmee.DEFAULT_NOTIFICATION_CHANNEL_ID
import com.tweener.alarmee.DefaultPushNotificationService
import com.tweener.alarmee.PushNotificationServiceRegistry
import com.tweener.alarmee.notification.NotificationFactory
import com.tweener.alarmee.notification.NotificationFactory.Companion.DEEP_LINK_URI_PARAM
import com.tweener.alarmee.notification.NotificationFactory.Companion.IMAGE_URL_PARAM
import com.tweener.alarmee.reveicer.NotificationBroadcastReceiver.Companion.DEFAULT_ICON_COLOR
import com.tweener.alarmee.reveicer.NotificationBroadcastReceiver.Companion.DEFAULT_ICON_RES_ID
import com.tweener.kmpkit.kotlinextensions.getNotificationManager
import com.tweener.kmpkit.utils.safeLet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * @author Vivien Mahe
 * @since 05/06/2025
 */
internal class AlarmeeFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val TITLE_PAYLOAD_PARAM = "title"
        const val BODY_PAYLOAD_PARAM = "body"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Notify the PushNotificationService about the new token
        PushNotificationServiceRegistry.get()?.let { service ->
            if (service is DefaultPushNotificationService) {
                service.notifyTokenUpdated(token)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // First, notify the PushNotificationService with the raw data for any registered callbacks
        PushNotificationServiceRegistry.get()?.handleIncomingMessage(message.data)

        // Then handle the notification display as before
        safeLet(message.data[TITLE_PAYLOAD_PARAM], message.data[BODY_PAYLOAD_PARAM]) { title, body ->
            val deepLinkUri = message.data[DEEP_LINK_URI_PARAM]
            val imageUrl = message.data[IMAGE_URL_PARAM]

            scope.launch {
                val notification = NotificationFactory.create(
                    context = applicationContext,
                    channelId = DEFAULT_NOTIFICATION_CHANNEL_ID,
                    title = title,
                    body = body,
                    priority = NotificationCompat.PRIORITY_DEFAULT,
                    iconResId = DEFAULT_ICON_RES_ID,
                    iconColor = DEFAULT_ICON_COLOR.toArgb(),
                    deepLinkUri = deepLinkUri,
                    imageUrl = imageUrl,
                    customData = message.data,
                )

                applicationContext.getNotificationManager()?.let { notificationManager ->
                    if (notificationManager.areNotificationsEnabled()) {
                        notificationManager.notify(UUID.randomUUID().toString().hashCode(), notification)
                    } else {
                        println("Notifications permission is not granted! Can't show the notification.")
                    }
                }
            }
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()

        println("Firebase messages deleted")
    }
}

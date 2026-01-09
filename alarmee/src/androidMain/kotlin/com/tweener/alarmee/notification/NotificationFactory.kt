package com.tweener.alarmee.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.tweener.alarmee._internal.kotlinextensions.getRawUri
import com.tweener.alarmee.model.NotificationAction
import com.tweener.alarmee.reveicer.NotificationActionBroadcastReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author Vivien Mahe
 * @since 04/01/2025
 */
class NotificationFactory {

    companion object {

        const val DEEP_LINK_URI_PARAM = "deepLinkUri"
        const val IMAGE_URL_PARAM = "imageUrl"

        suspend fun create(
            context: Context,
            channelId: String,
            title: String,
            body: String,
            priority: Int,
            iconResId: Int,
            iconColor: Int,
            soundFilename: String? = null,
            deepLinkUri: String? = null,
            imageUrl: String? = null,
            customData: Map<String, String>? = null,
            notificationUuid: String? = null,
            actions: List<NotificationAction> = emptyList(),
        ): Notification {
            val bitmap = withContext(Dispatchers.IO) {
                imageUrl?.let { loadImageFromUrl(imageUrl = it) }
            }

            return NotificationCompat.Builder(context, channelId)
                .apply {
                    setContentTitle(title)
                    setContentText(body)
                    setPriority(priority)
                    setSmallIcon(iconResId)
                    setColor(iconColor)
                    setAutoCancel(true)
                    soundFilename?.let { setSound(context.getRawUri(it)) } // Ignored on Android 8.0 and higher in favor of the value set on the notification's channel
                    setContentIntent(getPendingIntent(context = context, deepLinkUri = deepLinkUri, customData = customData)) // Handles click on notification

                    bitmap?.let {
                        setLargeIcon(it)
                        setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(it)
                                .bigLargeIcon(null as? Bitmap)
                        )
                    }

                    // Add action buttons (max 3 supported by Android)
                    notificationUuid?.let { uuid ->
                        actions.take(3).forEach { action ->
                            val actionPendingIntent = getActionPendingIntent(
                                context = context,
                                notificationUuid = uuid,
                                actionId = action.id,
                            )
                            addAction(action.iconResId ?: 0, action.label, actionPendingIntent)
                        }
                    }
                }
                .build()
        }

        private fun getPendingIntent(context: Context, deepLinkUri: String? = null, customData: Map<String, String>? = null): PendingIntent? {
            val intent = context.getLauncherActivityIntent()?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                deepLinkUri?.let { putExtra(DEEP_LINK_URI_PARAM, it) } // Pass the deep link URI to the activity
                customData?.forEach { (key, value) -> putExtra(key, value) } // Pass all custom data to the activity
            }
            return PendingIntent.getActivity(context, deepLinkUri?.hashCode() ?: 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun getActionPendingIntent(context: Context, notificationUuid: String, actionId: String): PendingIntent {
            val intent = Intent(context, NotificationActionBroadcastReceiver::class.java).apply {
                action = NotificationActionBroadcastReceiver.ACTION_NOTIFICATION_ACTION_CLICKED
                putExtra(NotificationActionBroadcastReceiver.KEY_NOTIFICATION_UUID, notificationUuid)
                putExtra(NotificationActionBroadcastReceiver.KEY_ACTION_ID, actionId)
            }

            // Use unique request code based on notification UUID + action ID
            val requestCode = "$notificationUuid-$actionId".hashCode()

            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }

        private fun Context.getLauncherActivityIntent(): Intent? = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
    }
}

private fun loadImageFromUrl(imageUrl: String): Bitmap? =
    try {
        val url = URL(imageUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()

        val inputStream: InputStream = connection.inputStream
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

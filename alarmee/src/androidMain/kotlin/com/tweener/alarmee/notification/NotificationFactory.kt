package com.tweener.alarmee.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.tweener.alarmee._internal.kotlinextensions.getRawUri

/**
 * @author Vivien Mahe
 * @since 04/01/2025
 */
class NotificationFactory {

    companion object {

        fun create(context: Context, channelId: String, title: String, body: String, priority: Int, iconResId: Int, iconColor: Int, soundFilename: String? = null): Notification =
            NotificationCompat.Builder(context, channelId)
                .apply {
                    setContentTitle(title)
                    setContentText(body)
                    setPriority(priority)
                    setSmallIcon(iconResId)
                    setColor(iconColor)
                    setAutoCancel(true)
                    soundFilename?.let { setSound(context.getRawUri(it)) } // Ignored on Android 8.0 and higher in favor of the value set on the notification's channel
                    setContentIntent(getPendingIntent(context = context)) // Handles click on notification
                }
                .build()

        private fun getPendingIntent(context: Context): PendingIntent? {
            val intent = context.getLauncherActivityIntent()?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun Context.getLauncherActivityIntent(): Intent? = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
    }
}

package com.tweener.alarmee._internal

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import com.tweener.alarmee.reveicer.NotificationBroadcastReceiver

/**
 * The look a notification takes when nothing more specific was asked for: the
 * icon and tint carried by `AlarmeeAndroidPlatformConfiguration`.
 *
 * It is REMEMBERED rather than passed, because a push notification can be built
 * in a process the app never opened. Firebase starts the app for the message
 * and `AlarmeeFirebaseMessagingService` runs before - or entirely without - the
 * `initialize()` call that holds the configuration, so there is nothing in
 * memory to read it from. Whatever the last initialization configured is
 * written here, and the messaging service reads it back.
 *
 * That is safe against a cold start: a push can only arrive once the app has
 * run and registered a token, so by the time any message exists, this has been
 * written at least once.
 *
 * **The icon is stored by NAME, never by resource id.** Ids are assigned at
 * build time and shift as resources are added, so an id written by one build
 * points somewhere else in the next - which is exactly how a notification ends
 * up wearing a stranger's drawable.
 *
 * @author Vivien Mahe
 * @since 01/09/2026
 */
object NotificationAppearance {

    private const val PREFERENCES_NAME = "com.tweener.alarmee.notificationAppearance"
    private const val KEY_ICON_RES_NAME = "notificationIconResName"
    private const val KEY_ICON_COLOR = "notificationIconColor"

    @Volatile
    private var cachedIconResId: Int? = null

    @Volatile
    private var cachedIconColor: Int? = null

    /** Records what [com.tweener.alarmee.configuration.AlarmeeAndroidPlatformConfiguration] asked for. */
    fun remember(context: Context, iconResId: Int, iconColor: Int) {
        cachedIconResId = iconResId
        cachedIconColor = iconColor

        val iconResName = runCatching { context.resources.getResourceName(iconResId) }.getOrNull()

        preferences(context)
            .edit()
            .apply { if (iconResName != null) putString(KEY_ICON_RES_NAME, iconResName) else remove(KEY_ICON_RES_NAME) }
            .putInt(KEY_ICON_COLOR, iconColor)
            .apply()
    }

    /** The configured icon, falling back to the one bundled with Alarmee. */
    fun iconResId(context: Context): Int =
        cachedIconResId
            ?: storedIconResId(context)?.also { cachedIconResId = it }
            ?: NotificationBroadcastReceiver.DEFAULT_ICON_RES_ID

    /** The configured tint, falling back to the one bundled with Alarmee. */
    fun iconColor(context: Context): Int =
        cachedIconColor
            ?: preferences(context)
                .getInt(KEY_ICON_COLOR, NotificationBroadcastReceiver.DEFAULT_ICON_COLOR.toArgb())
                .also { cachedIconColor = it }

    private fun storedIconResId(context: Context): Int? {
        val iconResName = preferences(context).getString(KEY_ICON_RES_NAME, null) ?: return null

        // A name that no longer resolves (the drawable was renamed or removed) answers 0, which is not a usable icon.
        return context.resources.getIdentifier(iconResName, null, null).takeIf { it != 0 }
    }

    private fun preferences(context: Context) =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
}

package com.tweener.alarmee

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeeJvmPlatformConfiguration
import java.awt.*
import java.awt.TrayIcon.MessageType

/**
 * @since 28/11/2024
 */

@Composable
actual fun rememberAlarmeeScheduler(platformConfiguration: AlarmeePlatformConfiguration): AlarmeeScheduler {
    requirePlatformConfiguration(providedPlatformConfiguration = platformConfiguration, targetPlatformConfiguration = AlarmeeJvmPlatformConfiguration::class)

    return remember { AlarmeeSchedulerJvm(configuration = platformConfiguration) }
}

class AlarmeeSchedulerJvm(
    private val configuration: AlarmeeJvmPlatformConfiguration = AlarmeeJvmPlatformConfiguration,
) : AlarmeeScheduler() {

    override fun scheduleAlarm(alarmee: Alarmee, onSuccess: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun scheduleRepeatingAlarm(alarmee: Alarmee, repeatInterval: RepeatInterval, onSuccess: () -> Unit) {
        TODO("Not yet implemented")
    }

    override fun cancelAlarm(uuid: String) {
        TODO("Not yet implemented")
    }

    override fun sendNotificationNow(uuid: String, title: String, body: String, channelId: String, priority: Int, iconResId: Int, iconColor: Int) {
        if (!SystemTray.isSupported()) {
            println("System tray is not supported on this platform.")
            return
        }

        val tray = SystemTray.getSystemTray()
        val image = Toolkit.getDefaultToolkit().createImage("")

        val trayIcon = TrayIcon(image, "Notification")
        trayIcon.isImageAutoSize = true
        trayIcon.toolTip = "Notification"
        trayIcon.displayMessage(title, body, MessageType.NONE)

        try {
            tray.add(trayIcon)
        } catch (e: AWTException) {
            println("Error adding tray icon: ${e.message}")
        }
    }
}

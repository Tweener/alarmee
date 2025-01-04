package com.tweener.alarmee

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tweener.alarmee.configuration.AlarmeeJvmPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration

/**
 * @author Vivien Mahe
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

    override fun scheduleAlarm(alarmee: Alarmee) {
        TODO("Not yet implemented")
    }

    override fun scheduleRepeatingAlarm(alarmee: Alarmee, repeatInterval: RepeatInterval) {
        TODO("Not yet implemented")
    }

    override fun cancelAlarm(uuid: String) {
        TODO("Not yet implemented")
    }
}

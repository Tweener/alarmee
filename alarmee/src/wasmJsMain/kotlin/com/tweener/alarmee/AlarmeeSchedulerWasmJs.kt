package com.tweener.alarmee

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeeWasmJsPlatformConfiguration

/**
 * @author Vivien Mahe
 * @since 28/11/2024
 */

@Composable
actual fun rememberAlarmeeScheduler(platformConfiguration: AlarmeePlatformConfiguration): AlarmeeScheduler {
    requirePlatformConfiguration(providedPlatformConfiguration = platformConfiguration, targetPlatformConfiguration = AlarmeeWasmJsPlatformConfiguration::class)

    return remember { AlarmeeSchedulerWasmJs(configuration = platformConfiguration) }
}

class AlarmeeSchedulerWasmJs(
    private val configuration: AlarmeeWasmJsPlatformConfiguration = AlarmeeWasmJsPlatformConfiguration,
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

    override fun pushAlarmee(alarmee: Alarmee, onSuccess: () -> Unit) {
        TODO("Not yet implemented")
    }
}

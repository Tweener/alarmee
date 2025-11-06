package com.tweener.alarmee

import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import dev.gitlive.firebase.Firebase

/**
 * @author Vivien Mahe
 * @since 05/06/2025
 */
internal class MobileDefaultAlarmeeService : DefaultAlarmeeService(), MobileAlarmeeService {

    override lateinit var push: PushNotificationService
    private var isInitialized = false

    override fun initialize(platformConfiguration: AlarmeePlatformConfiguration) {
        commonInitialization(platformConfiguration = platformConfiguration, firebase = null)
    }

    override fun initialize(platformConfiguration: AlarmeePlatformConfiguration, firebase: Firebase) {
        commonInitialization(platformConfiguration = platformConfiguration, firebase = firebase)
    }

    private fun commonInitialization(platformConfiguration: AlarmeePlatformConfiguration, firebase: Firebase?) {
        super.initialize(platformConfiguration = platformConfiguration)

        // Check if the service is already initialized to prevent re-initialization
        if (isInitialized) {
            println("Alarmee is already initialized.")
            return
        }

        // Initialize push notification service BEFORE configureFirebase to avoid race condition
        push = DefaultPushNotificationService(platformConfiguration)
        PushNotificationServiceRegistry.register(push)

        // No need to initialize Firebase if it's already done
        if (firebase == null) {
            initializeFirebase()
        }

        configureFirebase()

        isInitialized = true
    }
}

fun createAlarmeeMobileService(): MobileAlarmeeService = MobileDefaultAlarmeeService()

internal expect fun initializeFirebase()

internal expect fun configureFirebase()

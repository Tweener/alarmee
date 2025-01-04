package com.tweener.alarmee.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module

/**
 * @author Vivien Mahe
 * @since 03/01/2025
 */

val sharedIosModule = module {

    includes(sharedModule)

    // Multiplatform Settings
    single<Settings> { NSUserDefaultsSettings.Factory().create(name = "alarmeeSettings") }

}

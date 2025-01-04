package com.tweener.alarmee.di

import android.content.Context
import androidx.preference.PreferenceManager
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.dsl.module

/**
 * @author Vivien Mahe
 * @since 03/01/2025
 */

fun sharedAndroidModule(context: Context) = module {

    includes(sharedModule)

    single { context }

    // Multiplatform Settings
    single<Settings> { SharedPreferencesSettings.Factory(context = get()).create(name = "alarmeeSettings") }

}

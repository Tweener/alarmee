package com.tweener.alarmee.di

import android.content.Context
import androidx.startup.Initializer
import org.koin.core.context.startKoin

/**
 * @author Vivien Mahe
 * @since 03/01/2025
 */
class KoinInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        startKoin {
            modules(sharedAndroidModule(context = context.applicationContext))
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

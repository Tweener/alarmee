package com.tweener.alarmee

/**
 * @author Vivien Mahe
 * @since 05/06/2025
 */

actual fun createAlarmeeService(): AlarmeeService =
    MobileDefaultAlarmeeService()

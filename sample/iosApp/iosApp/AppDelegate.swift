//
//  AppDelegate.swift
//  iosApp
//
//  Created by Vivien Mahé on 27/11/2024.
//

import SwiftUI
import composeApp

@MainActor
class AppDelegate : NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        
        AlarmeeLibrary.shared.initialize()

        return true
    }
    
    // Call when the notification has been delivered when the app is in foreground
    nonisolated func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
//        AlarmeeLibrary.shared.onNotificationWillPresent(notification: notification)

        completionHandler([.banner, .list, .badge, .sound])
    }
    
    // Called when the user tapped on the notification
    nonisolated func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
//        AlarmeeLibrary.shared.onNotificationDidReceive(response: response)
        
        completionHandler()
    }
}

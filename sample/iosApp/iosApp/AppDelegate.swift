//
//  AppDelegate.swift
//  iosApp
//
//  Created by Vivien Mahé on 27/11/2024.
//

import SwiftUI
import FirebaseMessaging
import composeApp

@MainActor
class AppDelegate : NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any]) async -> UIBackgroundFetchResult {
        AlarmeeHelper().onNotificationReceived(userInfo: userInfo)
        return UIBackgroundFetchResult.newData
    }
    
    nonisolated func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let userInfo = notification.request.content.userInfo
        AlarmeeHelper().onNotificationReceived(userInfo: userInfo)
        
        completionHandler([.banner, .list, .badge, .sound])
    }
    
    nonisolated func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        let actionIdentifier = response.actionIdentifier

        print("userInfo: \(userInfo)")
        print("actionIdentifier: \(actionIdentifier)")

        // Handle notification action button taps
        if actionIdentifier != UNNotificationDefaultActionIdentifier && actionIdentifier != UNNotificationDismissActionIdentifier {
            if let notificationUuid = userInfo["notificationUuid"] as? String {
                NotificationActionHelper().onActionClicked(notificationUuid: notificationUuid, actionId: actionIdentifier)
            }
        }

        if let value = userInfo["deepLinkUri"] as? String {
            // Check for the deep link URI here, which is only present if it was provided in the Alarmee(deepLinkUri: ...) creation.
        }

        completionHandler()
    }
}

package com.tweener.alarmee.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tweener.alarmee.notification.NotificationFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Handle the new intent, for example, if it contains a deep link URI
        println("New intent received with deeplink: ${intent.getStringExtra(NotificationFactory.DEEP_LINK_URI_PARAM)}")
    }
}

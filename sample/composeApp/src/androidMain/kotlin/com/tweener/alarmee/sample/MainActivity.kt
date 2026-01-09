package com.tweener.alarmee.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

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

        // Handle the new intent, extract data as needed
        println("New intent received:")
        intent.extras?.keySet()?.forEach { key ->
            println("  Extra [$key]: ${intent.extras?.getString(key)}")
        }
    }
}

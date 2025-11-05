package com.example.notifiersample

import android.app.Application
import com.github.muhammadnoman11.easyfirebasenotifier.config.FCMConfig

/**
 * @author: Muhammad Noman
 * @github: https://github.com/muhammadnoman11
 * @date: 11/4/2025
 */
class App : Application(){

    override fun onCreate() {
        super.onCreate()

        FCMConfig.initialize(
            projectId = "YOUR_PROJECT_ID",
            serviceAccountJson = "YOUR_SERVICE_ACCOUNT_JSON"

        )
    }
}
package com.github.muhammadnoman11.easyfirebasenotifier.config

import kotlinx.serialization.Serializable

/**
 * @author: Muhammad Noman
 * @github: https://github.com/muhammadnoman11
 * @date: 11/4/2025
 */

/**
 * Configuration class for FCM setup
 */
@Serializable
data class FCMConfig(
    val projectId: String,
    val serviceAccountJson: String
) {
    companion object {
        private var instance: FCMConfig? = null

        /**
         * Initialize FCM configuration
         * Call this once in Application class
         */
        fun initialize(projectId: String, serviceAccountJson: String) {
            instance = FCMConfig(projectId, serviceAccountJson)
        }

        /**
         * Get initialized configuration
         */
        fun getInstance(): FCMConfig {
            return instance ?: throw IllegalStateException(
                "FCMConfig not initialized. Call FCMConfig.initialize() first"
            )
        }
    }

    fun getPostUrl(): String =
        "https://fcm.googleapis.com/v1/projects/$projectId/messages:send"
}
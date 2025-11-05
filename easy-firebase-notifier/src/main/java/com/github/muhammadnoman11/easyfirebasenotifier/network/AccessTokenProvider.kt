package com.github.muhammadnoman11.easyfirebasenotifier.network

import com.github.muhammadnoman11.easyfirebasenotifier.config.FCMConfig
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

/**
 * @author: Muhammad Noman
 * @github: https://github.com/muhammadnoman11
 * @date: 11/4/2025
 */

/**
 * Provides OAuth2 access tokens for FCM API
 */
class AccessTokenProvider {

    private val firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging"

    /**
     * Get access token from service account credentials
     * This is a suspend function for coroutine support
     */
    suspend fun getAccessToken(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val config = FCMConfig.Companion.getInstance()
            val stream = ByteArrayInputStream(
                config.serviceAccountJson.toByteArray(StandardCharsets.UTF_8)
            )

            val credentials = GoogleCredentials.fromStream(stream)
                .createScoped(listOf(firebaseMessagingScope))

            credentials.refresh()

            val token = credentials.accessToken?.tokenValue
            if (token != null) {
                Result.success(token)
            } else {
                Result.failure(Exception("Token value is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
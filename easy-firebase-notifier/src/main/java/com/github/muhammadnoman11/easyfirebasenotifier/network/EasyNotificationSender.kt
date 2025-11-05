package com.github.muhammadnoman11.easyfirebasenotifier.network

import android.util.Log
import com.github.muhammadnoman11.easyfirebasenotifier.config.FCMConfig
import com.github.muhammadnoman11.easyfirebasenotifier.FCMTopics
import com.github.muhammadnoman11.easyfirebasenotifier.data.FCMMessage
import com.github.muhammadnoman11.easyfirebasenotifier.data.FCMResponse
import com.github.muhammadnoman11.easyfirebasenotifier.data.Message
import com.github.muhammadnoman11.easyfirebasenotifier.data.NotificationData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * @author: Muhammad Noman
 * @github: https://github.com/muhammadnoman11
 * @date: 11/4/2025
 */


/**
 * Main class for sending FCM notifications via Ktor
 */
class EasyNotificationSender {

    private val accessTokenProvider = AccessTokenProvider()
    private val tag = "FCMNotificationSender"

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.INFO
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 15000
        }
    }


    /**
     * Send notification to all users (default topic)
     */
    suspend fun sendToAllUsers(
        notificationData: NotificationData,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        sendToTopic(FCMTopics.ALL_USERS, notificationData, onSuccess, onFailure)
    }

    /**
     * Send notification to specific topic
     */
    suspend fun sendToTopic(
        topic: String,
        notificationData: NotificationData,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        withContext(Dispatchers.IO) {
            try {
                val accessTokenResult = accessTokenProvider.getAccessToken()

                if (accessTokenResult.isFailure) {
                    val exception = accessTokenResult.exceptionOrNull()
                        ?: Exception("Unknown error getting access token")
                    Log.e(tag, "Failed to get access token", exception)
                    withContext(Dispatchers.Main) { onFailure(exception as Exception) }
                    return@withContext
                }

                val accessToken = accessTokenResult.getOrNull()!!
                val config = FCMConfig.Companion.getInstance()

                val dataPayload = buildDataPayload(notificationData)

                val fcmMessage = FCMMessage(
                    message = Message(
                        topic = topic,
                        data = dataPayload
                    )
                )

                val response = httpClient.post(config.getPostUrl()) {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer $accessToken")
                    setBody(fcmMessage)
                }

                if (response.status.isSuccess()) {
                    val fcmResponse = response.body<FCMResponse>()
                    Log.d(tag, "Notification sent successfully to topic: $topic, Response: ${fcmResponse.name}")
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    val error = Exception("HTTP ${response.status.value}: ${response.status.description}")
                    Log.e(tag, "Failed to send notification", error)
                    withContext(Dispatchers.Main) { onFailure(error) }
                }

            } catch (e: Exception) {
                Log.e(tag, "Error sending notification to topic: $topic", e)
                withContext(Dispatchers.Main) { onFailure(e) }
            }
        }
    }

    /**
     * Send notification to specific device token
     */
    suspend fun sendToDevice(
        deviceToken: String,
        notificationData: NotificationData,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        withContext(Dispatchers.IO) {
            try {
                val accessTokenResult = accessTokenProvider.getAccessToken()

                if (accessTokenResult.isFailure) {
                    val exception = accessTokenResult.exceptionOrNull()
                        ?: Exception("Unknown error getting access token")
                    Log.e(tag, "Failed to get access token", exception)
                    withContext(Dispatchers.Main) { onFailure(exception as Exception) }
                    return@withContext
                }

                val accessToken = accessTokenResult.getOrNull()!!
                val config = FCMConfig.Companion.getInstance()

                val dataPayload = buildDataPayload(notificationData)

                val fcmMessage = FCMMessage(
                    message = Message(
                        token = deviceToken,
                        data = dataPayload
                    )
                )

                val response = httpClient.post(config.getPostUrl()) {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer $accessToken")
                    setBody(fcmMessage)
                }

                if (response.status.isSuccess()) {
                    val fcmResponse = response.body<FCMResponse>()
                    Log.d(tag, "Notification sent successfully to device, Response: ${fcmResponse.name}")
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    val error = Exception("HTTP ${response.status.value}: ${response.status.description}")
                    Log.e(tag, "Failed to send notification", error)
                    withContext(Dispatchers.Main) { onFailure(error) }
                }

            } catch (e: Exception) {
                Log.e(tag, "Error sending notification to device", e)
                withContext(Dispatchers.Main) { onFailure(e) }
            }
        }
    }

    private fun buildDataPayload(notificationData: NotificationData): Map<String, String> {
        val payload = mutableMapOf<String, String>()

        notificationData.title?.let { payload["title"] = it }
        notificationData.body?.let { payload["body"] = it }
        notificationData.imageUrl?.let { payload["imageUrl"] = it }
        payload["type"] = notificationData.type.name.lowercase()
        payload["timestamp"] = System.currentTimeMillis().toString()

        payload.putAll(notificationData.additionalData)

        return payload
    }

    /**
     * Clean up resources
     */
    fun close() {
        httpClient.close()
    }
}
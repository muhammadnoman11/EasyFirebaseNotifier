package com.github.muhammadnoman11.easyfirebasenotifier.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.github.muhammadnoman11.easyfirebasenotifier.FCMTopics
import com.github.muhammadnoman11.easyfirebasenotifier.data.DialogData
import com.github.muhammadnoman11.easyfirebasenotifier.pref.DialogPreferenceManager
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.URL

/**
 * @author: Muhammad Noman
 * @github: https://github.com/muhammadnoman11
 * @date: 11/4/2025
 */



/**
 * Base FCM service for receiving notifications
 */
abstract class EasyNotificationReceiver : FirebaseMessagingService() {

    private val tag = "FCMNotificationReceiver"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var dialogPrefsManager: DialogPreferenceManager

    override fun onCreate() {
        super.onCreate()
        dialogPrefsManager = DialogPreferenceManager.getInstance(applicationContext)
    }

    /**
     * Override this to provide activity class
     */
    abstract fun getActivityClass(): Class<*>

    /**
     * Override this to provide channel ID
     */
    abstract fun getChannelId(): String

    /**
     * Override this to provide channel name
     */
    abstract fun getChannelName(): String

    /**
     * Override this to provide notification icon
     */
    abstract fun getNotificationIcon(): Int

    /**
     * Override this to provide custom topics to subscribe to
     * Default is [FCMTopics.ALL_USERS]
     *
     * Example:
     * override fun getSubscriptionTopics() = listOf(
     *     FCMTopics.ALL_USERS,
     *     FCMTopics.PREMIUM_USERS,
     *     "custom_topic_123"
     * )
     */
    open fun getSubscriptionTopics(): List<String> = listOf(FCMTopics.ALL_USERS)


    /**
     * Override this to filter notifications (e.g., by app type)
     * Return true to process the notification, false to ignore it
     */
    open fun shouldProcessNotification(data: Map<String, String>): Boolean = true

    /**
     * Override this to handle dialog messages
     */
    open fun onDialogMessage(title: String?, body: String?, imageUrl: String?, data: Map<String, String>) {
        Log.d(tag, "Dialog message received: $title")
    }

    /**
     * Override this to handle token updates
     */
    open fun onTokenUpdated(token: String) {
        Log.d(tag, "New FCM token: $token")
    }


    /**
     * Called when FCM token is created or refreshed
     * Automatically subscribes to topics defined in getSubscriptionTopics()
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(tag, "New FCM token received: $token")
        onTokenUpdated(token)

        // Subscribe to all defined topics
        val topics = getSubscriptionTopics()
        topics.forEach { topic ->
            FirebaseMessaging.getInstance()
                .subscribeToTopic(topic)
                .addOnSuccessListener {
                    Log.d(tag, "Subscribed to topic: $topic")
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "Failed to subscribe to topic: $topic", e)
                }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data

        if (data.isEmpty()) {
            Log.d(tag, "No data in notification")
            return
        }

        // Check if this notification should be processed
        if (!shouldProcessNotification(data)) {
            Log.d(tag, "Notification filtered out")
            return
        }

        val title = data["title"]
        val body = data["body"]
        val imageUrl = data["imageUrl"]
        val type = data["type"] ?: "normal"

        Log.d(tag, "Notification received - Type: $type, Title: $title")

        when (type.lowercase()) {
            "normal" -> {
                serviceScope.launch {
                    displayNotification(title, body, imageUrl, data)
                }
            }
            "dialog" -> {
//                onDialogMessage(title, body, imageUrl, data)
                handleDialogMessage(title, body, imageUrl, data)
            }
            else -> {
                Log.w(tag, "Unknown notification type: $type")
            }
        }
    }

    /**
     * Handle dialog messages by saving to preferences
     */
    private fun handleDialogMessage(
        title: String?,
        body: String?,
        imageUrl: String?,
        data: Map<String, String>
    ) {
        val dialogData = DialogData(
            title = title,
            body = body,
            imageUrl = imageUrl,
            timestamp = System.currentTimeMillis(),
            action = data["action"]
        )

        dialogPrefsManager.saveDialogData(dialogData)
        Log.d(tag, "Dialog data saved: $title")
    }

    private suspend fun displayNotification(
        title: String?,
        body: String?,
        imageUrl: String?,
        data: Map<String, String>
    ) {
        val intent = Intent(this, getActivityClass()).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, getChannelId())
            .setSmallIcon(getNotificationIcon())
            .setContentTitle(title ?: "Notification")
            .setContentText(body ?: "")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Load image if available
        if (!imageUrl.isNullOrEmpty()) {
            try {
                val bitmap = loadImageFromUrl(imageUrl)
                bitmap?.let {
                    notificationBuilder
                        .setLargeIcon(it)
                        .setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(it)
                                .bigLargeIcon(null as Bitmap?)
                        )
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to load notification image", e)
            }
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getChannelId(),
                getChannelName(),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications from ${getChannelName()}"
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notificationBuilder.build()
        )
    }

    private suspend fun loadImageFromUrl(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            val inputStream = connection.getInputStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e(tag, "Error loading image from URL: $url", e)
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
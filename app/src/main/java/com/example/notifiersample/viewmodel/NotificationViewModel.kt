package com.example.notifiersample.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.muhammadnoman11.easyfirebasenotifier.data.NotificationData
import com.github.muhammadnoman11.easyfirebasenotifier.data.NotificationType
import com.github.muhammadnoman11.easyfirebasenotifier.network.EasyNotificationSender
import kotlinx.coroutines.launch

/**
 * @author: Muhammad Noman
 * @github: https://github.com/muhammadnoman11
 * @date: 11/4/2025
 */

class NotificationViewModel : ViewModel() {


    private val notificationSender = EasyNotificationSender()


    // Send to default topic (all_users)
    fun sendNotificationToAllUsers(
        title: String,
        body: String,
        imageUrl: String? = null
    ) {
        viewModelScope.launch {
            notificationSender.sendToAllUsers(
                notificationData = NotificationData(
                    title = title,
                    body = body,
                    imageUrl = imageUrl,
                    type = NotificationType.NORMAL
                ),
                onSuccess = {
                    Log.d("ViewModel", "Sent to all users")
                },
                onFailure = { e ->
                    Log.e("ViewModel", "Failed", e)
                }
            )
        }
    }


    // Send to custom topic
    fun sendToTopic(
        topic: String,
        title: String,
        body: String,
        imageUrl: String? = null
    ) {
        viewModelScope.launch {
            notificationSender.sendToTopic(
                topic = topic,
                notificationData = NotificationData(
                    title = title,
                    body = body,
                    imageUrl = imageUrl,
                    type = NotificationType.NORMAL
                )
            )
        }
    }

    fun sendDialogMessage(
        topic: String,
        title: String,
        body: String,
        imageUrl: String? = null
    ) {
        viewModelScope.launch {

            notificationSender.sendToTopic(
                topic = topic,
                notificationData = NotificationData(
                    title = title,
                    body = body,
                    imageUrl = imageUrl,
                    type = NotificationType.DIALOG,
                )
            )

        }
    }

    fun sendToSpecificUser(
        deviceToken: String,
        title: String,
        body: String,
        imageUrl: String? = null
    ) {
        viewModelScope.launch {

            notificationSender.sendToDevice(
                deviceToken = deviceToken,
                notificationData = NotificationData(
                    title = title,
                    body = body,
                    imageUrl = imageUrl,
                    type = NotificationType.NORMAL
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        notificationSender.close()
    }
}
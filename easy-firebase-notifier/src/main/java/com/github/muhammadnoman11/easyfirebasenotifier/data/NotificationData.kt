package com.github.muhammadnoman11.easyfirebasenotifier.data

/**
 * @author: Muhammad Noman
 * @github: https://github.com/muhammadnoman11
 * @date: 11/4/2025
 */


/**
 * Notification data model
 */
data class NotificationData(
    val title: String? = null,
    val body: String? = null,
    val imageUrl: String? = null,
    val type: NotificationType = NotificationType.NORMAL,
    val additionalData: Map<String, String> = emptyMap()
)


package com.example.notifiersample.service

import com.example.notifiersample.MainActivity
import com.example.notifiersample.R
import com.github.muhammadnoman11.easyfirebasenotifier.FCMTopics
import com.github.muhammadnoman11.easyfirebasenotifier.receiver.EasyNotificationReceiver

/**
 * @author: Muhammad Noman
 * @github: https://github.com/muhammadnoman11
 * @date: 11/4/2025
 */
class FCMService : EasyNotificationReceiver() {


    override fun getActivityClass() = MainActivity::class.java
    override fun getChannelId() = "user_notifications"
    override fun getChannelName() = "User Notifications"
    override fun getNotificationIcon() = R.drawable.ic_launcher_foreground


    // Custom topics - Default is ALL_USERS
    override fun getSubscriptionTopics(): List<String> {
        return listOf(
            FCMTopics.ALL_USERS, // Default topic
//            FCMTopics.PREMIUM_USERS, // If user is premium
//            "custom_offers" // Any custom topic
        )
    }

//    override fun shouldProcessNotification(data: Map<String, String>): Boolean {
//        // Filter notifications by admin ID or other criteria
//        val adminId = data["admin_id"]
//        val myAdminId = getMyAdminId()
//        return adminId == myAdminId
//    }


    override fun onTokenUpdated(token: String) {
        // Save token to your backend/database/preferences
    }


}
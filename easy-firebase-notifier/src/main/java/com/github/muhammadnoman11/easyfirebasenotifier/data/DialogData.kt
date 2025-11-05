package com.github.muhammadnoman11.easyfirebasenotifier.data

/**
 * @author: Muhammad Noman
 * @github: https://github.com/muhammadnoman11
 * @date: 11/4/2025
 */

/**
 * Data class for dialog notifications
 */
data class DialogData(
    val title: String? = null,
    val body: String? = null,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String? = null
)
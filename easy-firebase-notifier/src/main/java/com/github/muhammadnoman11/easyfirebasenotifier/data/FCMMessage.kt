package com.github.muhammadnoman11.easyfirebasenotifier.data

import kotlinx.serialization.Serializable

/**
 * @author: Muhammad Noman
 * @github: https://github.com/muhammadnoman11
 * @date: 11/5/2025
 */



/**
 * FCM Message payload models for Ktor serialization
 */
@Serializable
data class FCMMessage(
    val message: Message
)

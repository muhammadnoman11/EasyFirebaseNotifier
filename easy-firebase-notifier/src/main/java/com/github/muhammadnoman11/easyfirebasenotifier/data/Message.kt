package com.github.muhammadnoman11.easyfirebasenotifier.data

import kotlinx.serialization.Serializable

/**
 * @author: Muhammad Noman
 * @github: https://github.com/muhammadnoman11
 * @date: 11/5/2025
 */


@Serializable
data class Message(
    val topic: String? = null,
    val token: String? = null,
    val data: Map<String, String>
)
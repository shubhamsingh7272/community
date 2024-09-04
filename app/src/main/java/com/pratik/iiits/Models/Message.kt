package com.pratik.iiits.Models

data class Message(
    val senderId: String = "",
    val message: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0
)

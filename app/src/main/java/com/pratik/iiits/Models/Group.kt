package com.pratik.iiits.Models

data class Group(
    val id: String = "",
    val name: String = "",
    val admin: String = "",
    val members: List<String> = listOf(),
    val category: String = "", // Category field
    var lastMessage: String = "",
    var lastMessageTime: Long = 0L,
    val unreadMessages: MutableMap<String, Boolean> = mutableMapOf(),
    val groupImageUrl: String? = null // Add this line for the image URL
)

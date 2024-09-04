package com.pratik.iiits.Models

data class GroupRequest(
    var id: String = "",
    val userId: String = "",
    val userName: String = "",
    val groupId: String = "",
    val groupName: String = "",
    val status: String = "pending"
)

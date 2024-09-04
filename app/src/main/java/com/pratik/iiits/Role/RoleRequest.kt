package com.pratik.iiits.Role

data class RoleRequest(
    var id: String = "",
    val userId: String = "",
    val roleName: String = "",
    val status: String = ""
) {
    // No-argument constructor required for Firestore
    constructor() : this("", "", "", "")
}

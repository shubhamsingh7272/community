package com.pratik.iiits.Marketplace

import java.io.Serializable

data class Item(
    val title: String,
    val description: String,
    val price: String,
    val imageUrls: List<String>,
    val userId: String,
    var user: String,
    val itemId: String,
    var profilePictureUrl: String? = null,
    var email: String // Add email field
): Serializable {
    // Default constructor
    constructor() : this("", "", "", listOf(),"","", "",null,"")
}

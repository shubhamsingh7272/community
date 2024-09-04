package com.pratik.iiits.Models


data class Poll(
    val question: String = "",
    val options: List<String> = listOf(),
    val voteCounts: MutableList<Int> = mutableListOf(), // Track vote counts for each option
    val voters: MutableMap<String, Int> = mutableMapOf(), // Track who voted and for which option
    val timestamp: Long = 0L,
    val createdBy: UserModel? = null
){
    fun hasUserVoted(userId: String): Boolean {
        return voters.containsKey(userId)
    }
}

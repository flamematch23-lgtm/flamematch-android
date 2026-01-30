package com.flamematch.app.data.model

data class Match(
    val id: String = "",
    val user1Id: String = "",
    val user2Id: String = "",
    val user1Name: String = "",
    val user2Name: String = "",
    val user1Photo: String = "",
    val user2Photo: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0
)

package com.flamematch.app.data.model

data class Message(
    val id: String = "",
    val matchId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

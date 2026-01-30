package com.flamematch.app.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val age: Int = 18,
    val bio: String = "",
    val photos: List<String> = emptyList(),
    val gender: String = "",
    val interestedIn: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isVerified: Boolean = false,
    val isPremium: Boolean = false,
    val premiumType: String = "",
    val likesRemaining: Int = 10,
    val superLikesRemaining: Int = 1,
    val boostsRemaining: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

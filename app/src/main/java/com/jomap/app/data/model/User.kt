package com.jomap.app.data.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null
)

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val imageRes: Int
)

// نموذج مراجعة المستخدم (تاريخ نشاطه)
data class UserActivityReview(
    val placeName: String,
    val rating: Double,
    val comment: String,
    val date: String
)
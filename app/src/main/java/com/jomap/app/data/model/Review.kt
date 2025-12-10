package com.jomap.app.data.model

data class Review(
    val id: String,
    val userId: String,
    val locationId: String,
    val rating: Double,
    val comment: String,
    val createdAt: Long
)
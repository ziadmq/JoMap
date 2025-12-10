package com.jomap.app.data.model

data class Location(
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val category: String,
    val rating: Double,
    val imageUrl: String? = null,
    val distanceKm: Double? = null
)

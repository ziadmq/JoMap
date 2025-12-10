package com.jomap.app.data.model

import java.util.UUID

data class NearbyLocation(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val rating: Double,
    val lat: Double,
    val lng: Double,
    val imageRes: Int,
    val category: String,   // ضروري للفلترة
    val visitCount: Int,    // ضروري للترتيب
    val distanceKm: Double  // ضروري لحساب المسافة
)
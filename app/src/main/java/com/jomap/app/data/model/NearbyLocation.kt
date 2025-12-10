package com.jomap.app.data.model

data class NearbyLocation(
    val name: String,
    val rating: Double,
    val lat: Double,
    val lng: Double,
    val imageRes: Int  // مرجع لصورة تمثيلية (من موارد drawable)
)

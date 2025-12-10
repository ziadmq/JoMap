package com.jomap.app.data.model

import com.google.android.gms.maps.model.LatLng
import androidx.compose.ui.graphics.Color

data class Governorate(
    val id: String,
    val name: String,
    val imageRes: Int,         // صورة للمحافظة
    val description: String,   // نبذة
    val history: String,       // التاريخ
    val bestLocations: List<NearbyLocation>, // قائمة أفضل الأماكن
    val events: List<String>,  // قائمة الفعاليات
    val center: LatLng,
    val defaultZoom: Float,
    val color: Color,
    val points: List<LatLng>
)

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

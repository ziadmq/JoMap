package com.jomap.app.viewmodel

import androidx.lifecycle.ViewModel
import com.jomap.app.data.model.NearbyLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.jomap.app.R

class HomeViewModel : ViewModel() {
    // قائمة تجريبية من المواقع القريبة (بيانات ثابتة لغرض المثال)
    private val dummyLocations = listOf(
        NearbyLocation(
            name = "Sydney Opera House",
            rating = 4.8,
            lat = -33.8568, lng = 151.2153,
            imageRes = R.drawable.ic_launcher_background
        ),
        NearbyLocation(
            name = "Harbour Bridge",
            rating = 4.7,
            lat = -33.8523, lng = 151.2108,
            imageRes = R.drawable.ic_launcher_background
        ),
        NearbyLocation(
            name = "Museum of Contemporary Art",
            rating = 4.6,
            lat = -33.8590, lng = 151.2086,
            imageRes = R.drawable.ic_launcher_background
        )
    )

    // استخدام StateFlow للاحتفاظ بحالة قائمة المواقع
    private val _nearbyLocations = MutableStateFlow(dummyLocations)
    val nearbyLocations: StateFlow<List<NearbyLocation>> = _nearbyLocations.asStateFlow()

    // في حالة واقعية، يمكن إضافة دوال لتحديث المواقع القريبة من مصدر بيانات (API أو قاعدة بيانات)
}
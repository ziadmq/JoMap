package com.jomap.app.viewmodel

import androidx.lifecycle.ViewModel
import com.jomap.app.R
import com.jomap.app.data.model.NearbyLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class FavoritesViewModel : ViewModel() {

    // قائمة وهمية للمواقع المفضلة
    private val _favoriteLocations = MutableStateFlow<List<NearbyLocation>>(
        listOf(
            NearbyLocation(
                id = "1",
                name = "مطعم السلطان",
                rating = 4.5,
                lat = 31.9568,
                lng = 35.9153,
                imageRes = R.drawable.ic_launcher_background,
                category = "مطاعم",
                visitCount = 1500,
                distanceKm = 1.2
            ),
            NearbyLocation(
                id = "3",
                name = "حديقة الحسين",
                rating = 4.8,
                lat = 31.9590,
                lng = 35.9086,
                imageRes = R.drawable.ic_launcher_background,
                category = "منتزهات",
                visitCount = 5000,
                distanceKm = 3.5
            )
        )
    )
    val favoriteLocations = _favoriteLocations.asStateFlow()

    // دالة لحذف موقع من المفضلة
    fun removeFromFavorites(locationId: String) {
        _favoriteLocations.value = _favoriteLocations.value.filter { it.id != locationId }
    }
}
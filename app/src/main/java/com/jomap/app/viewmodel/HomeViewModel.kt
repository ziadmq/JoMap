package com.jomap.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.jomap.app.data.model.NearbyLocation
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// --- 1. إعدادات الاتصال بـ API ---

data class NominatimResponse(@SerializedName("geojson") val geojson: GeoJsonData)
data class GeoJsonData(@SerializedName("type") val type: String, @SerializedName("coordinates") val coordinates: List<Any>)

interface NominatimApiService {
    @GET("search")
    suspend fun getCountryBoundary(
        @Query("country") country: String,
        @Query("format") format: String = "json",
        @Query("polygon_geojson") polygonGeojson: Int = 1,
        // قللنا الرقم هنا لزيادة دقة الحدود (كان 0.005)
        @Query("polygon_threshold") threshold: Double = 0.001
    ): List<NominatimResponse>
}

object BoundaryApiClient {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"
    val service: NominatimApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApiService::class.java)
    }
}

// --- 2. الـ ViewModel ---

class HomeViewModel : ViewModel() {

    val categories = listOf("الكل", "مطاعم", "كافيهات", "منتزهات", "فنادق")

    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _jordanBoundary = MutableStateFlow<List<LatLng>>(emptyList())
    val jordanBoundary: StateFlow<List<LatLng>> = _jordanBoundary.asStateFlow()

    // بيانات وهمية
    private val allLocations = listOf(
        NearbyLocation("مطعم السلطان", 4.5, 31.9568, 35.9153, com.jomap.app.R.drawable.ic_launcher_background),
        NearbyLocation("كافيه عمان", 4.2, 31.9523, 35.9108, com.jomap.app.R.drawable.ic_launcher_background),
        NearbyLocation("حديقة الحسين", 4.8, 31.9590, 35.9086, com.jomap.app.R.drawable.ic_launcher_background),
        NearbyLocation("فندق الرويال", 4.6, 31.9600, 35.9100, com.jomap.app.R.drawable.ic_launcher_background)
    )

    private val _filteredLocations = MutableStateFlow(allLocations)
    val locations: StateFlow<List<NearbyLocation>> = _filteredLocations.asStateFlow()

    init {
        fetchJordanBoundary()
    }

    private fun fetchJordanBoundary() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // جلب الحدود من API
                val response = BoundaryApiClient.service.getCountryBoundary("Jordan")
                if (response.isNotEmpty()) {
                    val boundary = parseGeoJson(response[0].geojson)
                    if (boundary.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            _jordanBoundary.value = boundary
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching boundary: ${e.message}")
                // حدود احتياطية في حال انقطاع النت
                withContext(Dispatchers.Main) {
                    _jordanBoundary.value = listOf(
                        LatLng(32.750, 35.700), LatLng(33.350, 38.800),
                        LatLng(29.190, 34.960), LatLng(29.550, 35.050)
                    )
                }
            }
        }
    }

    private fun parseGeoJson(geojson: GeoJsonData): List<LatLng> {
        val points = mutableListOf<LatLng>()
        try {
            val coordsList = if (geojson.type == "Polygon") {
                geojson.coordinates[0] as List<List<Double>>
            } else if (geojson.type == "MultiPolygon") {
                val multi = geojson.coordinates as List<List<List<List<Double>>>>
                multi.maxByOrNull { it[0].size }?.get(0) ?: emptyList()
            } else {
                emptyList()
            }
            coordsList.forEach { point ->
                points.add(LatLng(point[1], point[0]))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return points
    }

    fun onCategorySelected(category: String) { _selectedCategory.value = category }
    fun onSearchTextChange(text: String) { _searchText.value = text }
}
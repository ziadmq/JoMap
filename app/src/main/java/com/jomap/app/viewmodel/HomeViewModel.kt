package com.jomap.app.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.jomap.app.R
import com.jomap.app.data.model.Governorate
import com.jomap.app.data.model.NearbyLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // --- StateFlows for UI Data ---
    private val _selectedGovernorate = MutableStateFlow<Governorate?>(null)
    val selectedGovernorate = _selectedGovernorate.asStateFlow()

    private val _governorates = MutableStateFlow<List<Governorate>>(emptyList())
    val governorates = _governorates.asStateFlow()

    // Holds the list of locations for the Carousel (bottom list)
    private val _locations = MutableStateFlow<List<NearbyLocation>>(emptyList())
    val locations = _locations.asStateFlow()

    // --- Search & Filters States ---
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    // --- Map Layer Toggles (New Features) ---
    private val _isTrafficEnabled = MutableStateFlow(false)
    val isTrafficEnabled = _isTrafficEnabled.asStateFlow()

    private val _showGovernorates = MutableStateFlow(true)
    val showGovernorates = _showGovernorates.asStateFlow()

    private val _mapTypeNormal = MutableStateFlow(true)
    val mapTypeNormal = _mapTypeNormal.asStateFlow()

    // Fixed Categories List
    val categories = listOf("All", "Markets", "Restaurants", "Hospitals", "Cafes", "Parks", "Hotels")

    // Colors for Governorates
    private val govColors = listOf(
        Color(0xFFE57373), Color(0xFFBA68C8), Color(0xFF64B5F6), Color(0xFF4DB6AC),
        Color(0xFFFFF176), Color(0xFFFFB74D), Color(0xFFA1887F), Color(0xFF90A4AE),
        Color(0xFFF06292), Color(0xFF7986CB), Color(0xFF4DD0E1), Color(0xFF81C784)
    )

    init {
        loadGovernoratesData()
    }

    // --- Actions ---

    fun onSearchTextChange(text: String) {
        _searchText.value = text
        // Add filtering logic here if needed
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
        // Add filtering logic here if needed
    }

    fun onGovernorateSelected(gov: Governorate) {
        _selectedGovernorate.value = gov
    }

    fun clearSelectedGovernorate() {
        _selectedGovernorate.value = null
    }

    // --- Toggle Functions ---
    fun toggleTraffic() { _isTrafficEnabled.value = !_isTrafficEnabled.value }
    fun toggleGovernorates() { _showGovernorates.value = !_showGovernorates.value }
    fun toggleMapType() { _mapTypeNormal.value = !_mapTypeNormal.value }

    // --- Data Loading Logic ---

    private fun loadGovernoratesData() {
        // Hardcoded data for demonstration
        val ammanLocs = listOf(
            NearbyLocation(UUID.randomUUID().toString(), "المدرج الروماني", 4.7, 31.951, 35.939, R.drawable.ic_launcher_background, "تاريخي", 5000, 0.0),
            NearbyLocation(UUID.randomUUID().toString(), "جبل القلعة", 4.8, 31.954, 35.935, R.drawable.ic_launcher_background, "تاريخي", 6000, 0.0),
            NearbyLocation(UUID.randomUUID().toString(), "بوليفارد العبدلي", 4.6, 31.968, 35.900, R.drawable.ic_launcher_background, "تسوق", 4000, 0.0)
        )

        val jerashLocs = listOf(
            NearbyLocation(UUID.randomUUID().toString(), "آثار جرش", 4.9, 32.272, 35.891, R.drawable.ic_launcher_background, "تاريخي", 8000, 0.0),
            NearbyLocation(UUID.randomUUID().toString(), "ساحة الأعمدة", 4.8, 32.275, 35.890, R.drawable.ic_launcher_background, "تاريخي", 7500, 0.0)
        )

        val aqabaLocs = listOf(
            NearbyLocation(UUID.randomUUID().toString(), "شاطئ الغندور", 4.5, 29.532, 35.000, R.drawable.ic_launcher_background, "سياحي", 9000, 0.0),
            NearbyLocation(UUID.randomUUID().toString(), "قلعة العقبة", 4.3, 29.520, 35.005, R.drawable.ic_launcher_background, "تاريخي", 2000, 0.0)
        )

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            // Ensure this resource exists in res/raw/jordan_governorates.json
            val resourceId = context.resources.getIdentifier("jordan_governorates", "raw", context.packageName)

            // Helper function to parse points if file exists
            val parsedPoints = if (resourceId != 0) {
                val inputStream = context.resources.openRawResource(resourceId)
                val jsonString = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                parseGeoJsonPoints(jsonString)
            } else {
                emptyList()
            }

            val fullGovernorates = listOf(
                createGov(0, "العاصمة (عمان)", R.drawable.ic_launcher_background,
                    "عمان هي عاصمة المملكة الأردنية الهاشمية.", "تاريخ عريق.",
                    ammanLocs, listOf("مهرجان صيف عمان"), parsedPoints),

                createGov(1, "إربد", R.drawable.ic_launcher_background,
                    "عروس الشمال.", "سهول خصبة.",
                    listOf(NearbyLocation(UUID.randomUUID().toString(), "أم قيس", 4.7, 32.65, 35.68, R.drawable.ic_launcher_background, "تاريخي", 3000, 0.0)),
                    listOf("مهرجان القمح"), parsedPoints),

                createGov(2, "الزرقاء", R.drawable.ic_launcher_background, "مدينة صناعية.", "تاريخ سكة الحديد.", emptyList(), emptyList(), parsedPoints),
                createGov(3, "المفرق", R.drawable.ic_launcher_background, "بوابة الأردن.", "آثار متنوعة.", emptyList(), emptyList(), parsedPoints),

                createGov(4, "عجلون", R.drawable.ic_launcher_background, "طبيعة خضراء.", "قلعة عجلون.",
                    listOf(NearbyLocation(UUID.randomUUID().toString(), "قلعة عجلون", 4.8, 32.32, 35.72, R.drawable.ic_launcher_background, "تاريخي", 5000, 0.0)),
                    listOf("مهرجان عجلون"), parsedPoints),

                createGov(5, "جرش", R.drawable.ic_launcher_background, "مدينة الألف عمود.", "آثار رومانية.",
                    jerashLocs, listOf("مهرجان جرش"), parsedPoints),

                createGov(6, "مادبا", R.drawable.ic_launcher_background, "مدينة الفسيفساء.", "خارطة مادبا.", emptyList(), emptyList(), parsedPoints),
                createGov(7, "البلقاء", R.drawable.ic_launcher_background, "السلط التراثية.", "تراث معماري.", emptyList(), emptyList(), parsedPoints),
                createGov(8, "الكرك", R.drawable.ic_launcher_background, "مدينة القلعة.", "تاريخ مؤابي.", emptyList(), emptyList(), parsedPoints),
                createGov(9, "الطفيلة", R.drawable.ic_launcher_background, "الهاشمية.", "محمية ضانا.", emptyList(), emptyList(), parsedPoints),

                createGov(10, "معان", R.drawable.ic_launcher_background, "البتراء ووادي رم.", "حضارة الأنباط.",
                    listOf(
                        NearbyLocation(UUID.randomUUID().toString(), "البتراء", 5.0, 30.32, 35.44, R.drawable.ic_launcher_background, "عجائب الدنيا", 10000, 0.0),
                        NearbyLocation(UUID.randomUUID().toString(), "وادي رم", 4.9, 29.57, 35.42, R.drawable.ic_launcher_background, "طبيعة", 8000, 0.0)
                    ),
                    listOf("مهرجان البتراء"), parsedPoints),

                createGov(11, "العقبة", R.drawable.ic_launcher_background, "ثغر الأردن.", "منفذ بحري.",
                    aqabaLocs, listOf("مهرجان العقبة"), parsedPoints)
            )

            withContext(Dispatchers.Main) {
                _governorates.value = fullGovernorates
                // Populate initial locations for the carousel (combining some from govs)
                _locations.value = ammanLocs + jerashLocs + aqabaLocs
            }
        }
    }

    private fun createGov(index: Int, name: String, img: Int, desc: String, hist: String, locs: List<NearbyLocation>, events: List<String>, allPoints: List<List<LatLng>>): Governorate {
        val points = if (index < allPoints.size) allPoints[index] else emptyList()
        val center = if (points.isNotEmpty()) calculateCentroid(points) else LatLng(31.0, 36.0)

        return Governorate(
            id = index.toString(),
            name = name,
            imageRes = img,
            description = desc,
            history = hist,
            bestLocations = locs,
            events = events,
            center = center,
            defaultZoom = 10f,
            color = govColors[index % govColors.size],
            points = points
        )
    }

    private fun parseGeoJsonPoints(json: String): List<List<LatLng>> {
        val allPolygons = mutableListOf<List<LatLng>>()
        try {
            val root = JSONObject(json)
            val features = root.getJSONArray("features")
            for (i in 0 until features.length()) {
                val geometry = features.getJSONObject(i).getJSONObject("geometry")
                val type = geometry.getString("type")
                val points = mutableListOf<LatLng>()

                if (type == "Polygon") {
                    val coords = geometry.getJSONArray("coordinates").getJSONArray(0)
                    for (j in 0 until coords.length()) {
                        points.add(LatLng(coords.getJSONArray(j).getDouble(1), coords.getJSONArray(j).getDouble(0)))
                    }
                } else if (type == "MultiPolygon") {
                    val coords = geometry.getJSONArray("coordinates").getJSONArray(0).getJSONArray(0)
                    for (j in 0 until coords.length()) {
                        points.add(LatLng(coords.getJSONArray(j).getDouble(1), coords.getJSONArray(j).getDouble(0)))
                    }
                }
                allPolygons.add(points)
            }
        } catch (e: Exception) { e.printStackTrace() }
        return allPolygons
    }

    private fun calculateCentroid(points: List<LatLng>): LatLng {
        var lat = 0.0; var lng = 0.0
        points.forEach { lat += it.latitude; lng += it.longitude }
        return LatLng(lat / points.size, lng / points.size)
    }
}
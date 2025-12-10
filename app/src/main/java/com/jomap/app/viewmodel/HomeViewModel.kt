package com.jomap.app.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.jomap.app.R
import com.jomap.app.data.model.CommunityPost
import com.jomap.app.data.model.Governorate
import com.jomap.app.data.model.NearbyLocation
import com.jomap.app.data.model.PostType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import kotlin.math.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // --- Existing States ---
    private val _selectedGovernorate = MutableStateFlow<Governorate?>(null)
    val selectedGovernorate = _selectedGovernorate.asStateFlow()

    private val _governorates = MutableStateFlow<List<Governorate>>(emptyList())
    val governorates = _governorates.asStateFlow()

    private val _locations = MutableStateFlow<List<NearbyLocation>>(emptyList())
    val locations = _locations.asStateFlow()

    private var allLocations = listOf<NearbyLocation>()

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _isTrafficEnabled = MutableStateFlow(false)
    val isTrafficEnabled = _isTrafficEnabled.asStateFlow()

    private val _showGovernorates = MutableStateFlow(true)
    val showGovernorates = _showGovernorates.asStateFlow()

    private val _mapTypeNormal = MutableStateFlow(true)
    val mapTypeNormal = _mapTypeNormal.asStateFlow()

    private val _tripSelection = MutableStateFlow<Set<String>>(emptySet())
    val tripSelection = _tripSelection.asStateFlow()

    // 🟢 Community Posts State
    private val _communityPosts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val communityPosts = _communityPosts.asStateFlow()

    // 🟢 Map Focus Event (to move camera when "Show Location" is clicked)
    private val _mapFocusTarget = MutableStateFlow<LatLng?>(null)
    val mapFocusTarget = _mapFocusTarget.asStateFlow()

    val categories = listOf("All", "History", "Tourism", "Markets", "Restaurants", "Hospitals", "Cafes", "Parks", "Hotels")

    private val govColors = listOf(
        Color(0xFFE57373), Color(0xFFBA68C8), Color(0xFF64B5F6), Color(0xFF4DB6AC),
        Color(0xFFFFF176), Color(0xFFFFB74D), Color(0xFFA1887F), Color(0xFF90A4AE),
        Color(0xFFF06292), Color(0xFF7986CB), Color(0xFF4DD0E1), Color(0xFF81C784)
    )

    init {
        loadGovernoratesData()
        generateDummyCommunityPosts() // Load community data
    }

    // --- Community Actions 🟢 ---

    fun focusOnLocation(latLng: LatLng) {
        _mapFocusTarget.value = latLng
    }

    fun clearMapFocus() {
        _mapFocusTarget.value = null
    }

    private fun generateDummyCommunityPosts() {
        // Sample data for Amman (ID: 0), Irbid (ID: 1), Aqaba (ID: 11)
        val posts = listOf(
            CommunityPost(
                governorateId = "0", // Amman
                placeName = "Downtown Restaurant",
                description = "Special Offer: 50% off on Mansaf family platter this Friday!",
                type = PostType.OFFER,
                location = LatLng(31.951, 35.939),
                imageRes = R.drawable.ic_launcher_background,
                date = "2 hours ago"
            ),
            CommunityPost(
                governorateId = "0", // Amman
                placeName = "Abdali Mall",
                description = "Summer Festival starts tomorrow. Live music and food stalls.",
                type = PostType.EVENT,
                location = LatLng(31.968, 35.900),
                imageRes = R.drawable.ic_launcher_background,
                date = "1 day ago"
            ),
            CommunityPost(
                governorateId = "11", // Aqaba
                placeName = "Red Sea Diving Center",
                description = "Free trial dive for beginners this weekend.",
                type = PostType.OFFER,
                location = LatLng(29.532, 35.000),
                imageRes = R.drawable.ic_launcher_background,
                date = "3 hours ago"
            ),
            CommunityPost(
                governorateId = "5", // Jerash
                placeName = "Jerash Festival",
                description = "The annual festival creates a bridge between cultures.",
                type = PostType.EVENT,
                location = LatLng(32.272, 35.891),
                imageRes = R.drawable.ic_launcher_background,
                date = "Just now"
            )
        )
        _communityPosts.value = posts
    }

    // --- Search & Filter ---
    fun onSearchTextChange(text: String) { _searchText.value = text; updateFilteredLocations() }
    fun onCategorySelected(category: String) { _selectedCategory.value = category; updateFilteredLocations() }

    private fun updateFilteredLocations() {
        val query = _searchText.value.lowercase().trim()
        val cat = _selectedCategory.value
        _locations.value = allLocations.filter { loc ->
            val matchesSearch = loc.name.lowercase().contains(query)
            val matchesCategory = if (cat == "All") true else loc.category.equals(cat, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }

    // --- Trip Planner Actions ---
    fun toggleTripLocation(locationId: String) {
        val current = _tripSelection.value.toMutableSet()
        if (current.contains(locationId)) current.remove(locationId) else current.add(locationId)
        _tripSelection.value = current
    }
    fun clearTrip() { _tripSelection.value = emptySet() }

    fun getOptimizedTripPlan(startLat: Double, startLng: Double): List<NearbyLocation> {
        val selected = allLocations.filter { _tripSelection.value.contains(it.id) }
        return selected.sortedBy { calculateDistance(startLat, startLng, it.lat, it.lng) }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    fun onGovernorateSelected(gov: Governorate) { _selectedGovernorate.value = gov }
    fun clearSelectedGovernorate() { _selectedGovernorate.value = null }
    fun toggleTraffic() { _isTrafficEnabled.value = !_isTrafficEnabled.value }
    fun toggleGovernorates() { _showGovernorates.value = !_showGovernorates.value }
    fun toggleMapType() { _mapTypeNormal.value = !_mapTypeNormal.value }

    private fun loadGovernoratesData() {
        val ammanLocs = listOf(
            NearbyLocation(UUID.randomUUID().toString(), "Roman Amphitheatre", 4.7, 31.951, 35.939, R.drawable.ic_launcher_background, "History", 5000, 0.0),
            NearbyLocation(UUID.randomUUID().toString(), "Amman Citadel", 4.8, 31.954, 35.935, R.drawable.ic_launcher_background, "History", 6000, 0.0),
            NearbyLocation(UUID.randomUUID().toString(), "Abdali Boulevard", 4.6, 31.968, 35.900, R.drawable.ic_launcher_background, "Markets", 4000, 0.0)
        )
        val jerashLocs = listOf(
            NearbyLocation(UUID.randomUUID().toString(), "Jerash Ruins", 4.9, 32.272, 35.891, R.drawable.ic_launcher_background, "History", 8000, 0.0),
            NearbyLocation(UUID.randomUUID().toString(), "Oval Plaza", 4.8, 32.275, 35.890, R.drawable.ic_launcher_background, "History", 7500, 0.0)
        )
        val aqabaLocs = listOf(
            NearbyLocation(UUID.randomUUID().toString(), "Al-Ghandour Beach", 4.5, 29.532, 35.000, R.drawable.ic_launcher_background, "Tourism", 9000, 0.0),
            NearbyLocation(UUID.randomUUID().toString(), "Aqaba Fort", 4.3, 29.520, 35.005, R.drawable.ic_launcher_background, "History", 2000, 0.0)
        )
        val petraLocs = listOf(
            NearbyLocation(UUID.randomUUID().toString(), "Petra", 5.0, 30.32, 35.44, R.drawable.ic_launcher_background, "History", 10000, 0.0),
            NearbyLocation(UUID.randomUUID().toString(), "Wadi Rum", 4.9, 29.57, 35.42, R.drawable.ic_launcher_background, "Tourism", 8000, 0.0)
        )

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val resourceId = context.resources.getIdentifier("jordan_governorates", "raw", context.packageName)

            val parsedPoints = if (resourceId != 0) {
                try {
                    val inputStream = context.resources.openRawResource(resourceId)
                    val jsonString = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                    parseGeoJsonPoints(jsonString)
                } catch (e: Exception) { emptyList() }
            } else { emptyList() }

            val fullGovernorates = listOf(
                createGov(0, "Amman", R.drawable.ic_launcher_background, "Capital", "History", ammanLocs, listOf("Amman Summer"), parsedPoints),
                createGov(1, "Irbid", R.drawable.ic_launcher_background, "North Bride", "Plains", emptyList(), listOf("Wheat"), parsedPoints),
                createGov(2, "Zarqa", R.drawable.ic_launcher_background, "Industrial", "Trains", emptyList(), emptyList(), parsedPoints),
                createGov(3, "Mafraq", R.drawable.ic_launcher_background, "Crossroads", "Ruins", emptyList(), emptyList(), parsedPoints),
                createGov(4, "Ajloun", R.drawable.ic_launcher_background, "Green", "Castle", emptyList(), listOf("Ajloun Fest"), parsedPoints),
                createGov(5, "Jerash", R.drawable.ic_launcher_background, "1000 Columns", "Roman", jerashLocs, listOf("Jerash Fest"), parsedPoints),
                createGov(6, "Madaba", R.drawable.ic_launcher_background, "Mosaics", "Map", emptyList(), emptyList(), parsedPoints),
                createGov(7, "Balqa", R.drawable.ic_launcher_background, "Salt", "Heritage", emptyList(), emptyList(), parsedPoints),
                createGov(8, "Karak", R.drawable.ic_launcher_background, "Castle City", "Crusader", emptyList(), emptyList(), parsedPoints),
                createGov(9, "Tafilah", R.drawable.ic_launcher_background, "Dana", "Nature", emptyList(), emptyList(), parsedPoints),
                createGov(10, "Ma'an", R.drawable.ic_launcher_background, "Petra", "Nabatean", petraLocs, listOf("Petra Fest"), parsedPoints),
                createGov(11, "Aqaba", R.drawable.ic_launcher_background, "Red Sea", "Coast", aqabaLocs, listOf("Aqaba Fest"), parsedPoints)
            )

            withContext(Dispatchers.Main) {
                _governorates.value = fullGovernorates
                allLocations = ammanLocs + jerashLocs + aqabaLocs + petraLocs
                updateFilteredLocations()
            }
        }
    }

    private fun createGov(index: Int, name: String, img: Int, desc: String, hist: String, locs: List<NearbyLocation>, events: List<String>, allPoints: List<List<LatLng>>): Governorate {
        val points = if (index < allPoints.size) allPoints[index] else emptyList()
        val center = if (points.isNotEmpty()) calculateCentroid(points) else LatLng(31.0, 36.0)
        return Governorate(index.toString(), name, img, desc, hist, locs, events, center, 10f, govColors[index % govColors.size], points)
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
        } catch (e: Exception) { }
        return allPolygons
    }

    private fun calculateCentroid(points: List<LatLng>): LatLng {
        var lat = 0.0; var lng = 0.0
        points.forEach { lat += it.latitude; lng += it.longitude }
        return LatLng(lat / points.size, lng / points.size)
    }
}
package com.jomap.app.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
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
data class ActivityAd(
    val id: String,
    val title: String,
    val description: String,
    val imageRes: Int,
    val price: String
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // ------------------------------------------------------------------------------------
    // MAP STYLE STATE (DARK + MINT BRAND COLOR)
    // ------------------------------------------------------------------------------------
    private val _mapStyleOptions = MutableStateFlow<MapStyleOptions?>(null)
    val mapStyleOptions = _mapStyleOptions.asStateFlow()

    private val _ads = MutableStateFlow(
        listOf(
            ActivityAd("1", "Wadi Rum Jeep Tour", "Discover the red desert secrets", R.drawable.ic_launcher_background, "25 JOD"),
            ActivityAd("2", "Petra Night Show", "Experience the Siq by candlelight", R.drawable.ic_launcher_background, "17 JOD"),
            ActivityAd("3", "Aqaba Snorkeling", "Swim with Red Sea turtles", R.drawable.ic_launcher_background, "15 JOD"),
            ActivityAd("4", "Amman Food Tour", "Taste the best Falafel & Mansaf", R.drawable.ic_launcher_background, "20 JOD")
        )
    )
    val ads = _ads.asStateFlow()

    private fun loadMapStyle() {
        val json = """
        [
          { "featureType": "all", "elementType": "labels.text.fill", "stylers": [ { "color": "#A7F3EB" } ] },
          { "featureType": "all", "elementType": "labels.text.stroke", "stylers": [ { "color": "#0A0F0D" } ] },
          { "featureType": "landscape", "elementType": "geometry", "stylers": [ { "color": "#0D1110" } ] },
          { "featureType": "water", "elementType": "geometry", "stylers": [ { "color": "#021F24" } ] },
          { "featureType": "road", "elementType": "geometry", "stylers": [ { "color": "#0F1D1B" } ] },
          { "featureType": "road", "elementType": "geometry.stroke", "stylers": [ { "color": "#00BFA6" } ] },
          { "featureType": "poi", "elementType": "geometry", "stylers": [ { "color": "#0E1615" } ] },
          { "featureType": "poi", "elementType": "labels.icon", "stylers": [ { "color": "#00BFA6" } ] },
          { "featureType": "road.highway", "elementType": "geometry.fill", "stylers": [ { "color": "#01413A" } ] },
          { "featureType": "road.highway", "elementType": "labels.text.fill", "stylers": [ { "color": "#00BFA6" } ] }
        ]
        """.trimIndent()

        _mapStyleOptions.value = MapStyleOptions(json)
    }

    // ------------------------------------------------------------------------------------
    // Existing State Variables
    // ------------------------------------------------------------------------------------
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

    private val _communityPosts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val communityPosts = _communityPosts.asStateFlow()

    private val _mapFocusTarget = MutableStateFlow<LatLng?>(null)
    val mapFocusTarget = _mapFocusTarget.asStateFlow()

    // ⭐ ADDED "Governorates" TO THE LIST
    val categories = listOf("All", "Amman", "Zarqa", "Irbid", "Mafraq", "Ajloun", "Jerash", "Madaba", "Balqa", "Karak","Tafilah","Ma'an","Aqaba")

    private val govColors = listOf(
        Color(0xFFE57373), Color(0xFFBA68C8), Color(0xFF64B5F6), Color(0xFF4DB6AC),
        Color(0xFFFFF176), Color(0xFFFFB74D), Color(0xFFA1887F), Color(0xFF90A4AE),
        Color(0xFFF06292), Color(0xFF7986CB), Color(0xFF4DD0E1), Color(0xFF81C784)
    )

    init {
        loadGovernoratesData()
        generateDummyCommunityPosts()
        loadMapStyle()
    }

    // ------------------------------------------------------------------------------------
    // Map Actions
    // ------------------------------------------------------------------------------------
    fun focusOnLocation(latLng: LatLng) { _mapFocusTarget.value = latLng }
    fun clearMapFocus() { _mapFocusTarget.value = null }

    // ------------------------------------------------------------------------------------
    // Community
    // ------------------------------------------------------------------------------------
    private fun generateDummyCommunityPosts() {
        val posts = listOf(
            CommunityPost(
                governorateId = "0",
                placeName = "Downtown Restaurant",
                description = "50% off Mansaf!",
                type = PostType.OFFER,
                location = LatLng(31.951, 35.939),
                imageRes = R.drawable.ic_launcher_background,
                date = "2 hours ago"
            ),
            CommunityPost(
                governorateId = "0",
                placeName = "Abdali Mall",
                description = "Summer Festival Tomorrow!",
                type = PostType.EVENT,
                location = LatLng(31.968, 35.900),
                imageRes = R.drawable.ic_launcher_background,
                date = "1 day ago"
            ),
            CommunityPost(
                governorateId = "11",
                placeName = "Red Sea Diving",
                description = "Free trial dive!",
                type = PostType.OFFER,
                location = LatLng(29.532, 35.000),
                imageRes = R.drawable.ic_launcher_background,
                date = "3 hours ago"
            ),
            CommunityPost(
                governorateId = "5",
                placeName = "Jerash Festival",
                description = "Annual Culture Event",
                type = PostType.EVENT,
                location = LatLng(32.272, 35.891),
                imageRes = R.drawable.ic_launcher_background,
                date = "Just now"
            )
        )
        _communityPosts.value = posts
    }

    // ------------------------------------------------------------------------------------
    // Search & Filter
    // ------------------------------------------------------------------------------------
    fun onSearchTextChange(text: String) { _searchText.value = text; updateFilteredLocations() }
    fun onCategorySelected(category: String) { _selectedCategory.value = category; updateFilteredLocations() }

    private fun  updateFilteredLocations() {
        val query = _searchText.value.lowercase()
        val cat = _selectedCategory.value

        // ⭐ HANDLE "Governorates" CATEGORY
        if (cat == "Governorates") {
            // Convert Governorates to NearbyLocation format so they can be displayed in the list
            _locations.value = _governorates.value.filter { gov ->
                gov.name.lowercase().contains(query)
            }.map { gov ->
                NearbyLocation(
                    id = gov.id,
                    name = gov.name,
                    rating = 5.0, // Default rating for governorates
                    lat = gov.center.latitude,
                    lng = gov.center.longitude,
                    imageRes = gov.imageRes,
                    category = "Governorate",
                    visitCount = 0,
                    distanceKm = 0.0
                )
            }
        } else {
            // Normal location filtering
            _locations.value = allLocations.filter { loc ->
                val matchesSearch = loc.name.lowercase().contains(query)
                val matchesCategory = (cat == "All" || loc.category.equals(cat, true))
                matchesSearch && matchesCategory
            }
        }
    }

    // ------------------------------------------------------------------------------------
    // Trip Planner
    // ------------------------------------------------------------------------------------
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
        val R = 6371
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    // ------------------------------------------------------------------------------------
    // Map Toggles
    // ------------------------------------------------------------------------------------
    fun onGovernorateSelected(gov: Governorate) { _selectedGovernorate.value = gov }
    fun clearSelectedGovernorate() { _selectedGovernorate.value = null }
    fun toggleTraffic() { _isTrafficEnabled.value = !_isTrafficEnabled.value }
    fun toggleGovernorates() { _showGovernorates.value = !_showGovernorates.value }
    fun toggleMapType() { _mapTypeNormal.value = !_mapTypeNormal.value }

    // ------------------------------------------------------------------------------------
    // Load Governorates & Locations
    // ------------------------------------------------------------------------------------
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
                } catch (e: Exception) {
                    emptyList()
                }
            } else emptyList()

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
                // ⭐ Re-run filtering to update list if category was selected early
                updateFilteredLocations()
            }
        }
    }

    private fun createGov(
        index: Int,
        name: String,
        img: Int,
        desc: String,
        hist: String,
        locs: List<NearbyLocation>,
        events: List<String>,
        allPoints: List<List<LatLng>>
    ): Governorate {
        val points = if (index < allPoints.size) allPoints[index] else emptyList()
        val center = if (points.isNotEmpty()) calculateCentroid(points) else LatLng(31.0, 36.0)
        return Governorate(
            index.toString(),
            name,
            img,
            desc,
            hist,
            locs,
            events,
            center,
            10f,
            govColors[index % govColors.size],
            points
        )
    }

    private fun parseGeoJsonPoints(json: String): List<List<LatLng>> {
        val polygons = mutableListOf<List<LatLng>>()
        try {
            val root = JSONObject(json)
            val features = root.getJSONArray("features")
            for (i in 0 until features.length()) {
                val geometry = features.getJSONObject(i).getJSONObject("geometry")
                val type = geometry.getString("type")
                val pts = mutableListOf<LatLng>()

                if (type == "Polygon") {
                    val coords = geometry.getJSONArray("coordinates").getJSONArray(0)
                    for (j in 0 until coords.length()) {
                        pts.add(
                            LatLng(
                                coords.getJSONArray(j).getDouble(1),
                                coords.getJSONArray(j).getDouble(0)
                            )
                        )
                    }
                } else if (type == "MultiPolygon") {
                    val coords = geometry.getJSONArray("coordinates")
                        .getJSONArray(0)
                        .getJSONArray(0)
                    for (j in 0 until coords.length()) {
                        pts.add(
                            LatLng(
                                coords.getJSONArray(j).getDouble(1),
                                coords.getJSONArray(j).getDouble(0)
                            )
                        )
                    }
                }
                polygons.add(pts)
            }
        } catch (_: Exception) {}

        return polygons
    }

    private fun calculateCentroid(points: List<LatLng>): LatLng {
        var lat = 0.0
        var lng = 0.0
        points.forEach {
            lat += it.latitude
            lng += it.longitude
        }
        return LatLng(lat / points.size, lng / points.size)
    }
}
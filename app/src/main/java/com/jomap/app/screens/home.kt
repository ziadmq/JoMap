package com.jomap.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.android.gms.maps.model.*
import com.google.gson.annotations.SerializedName
import com.google.maps.android.compose.*
import com.jomap.app.data.model.NearbyLocation
import com.jomap.app.ui.navigation.Screen
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

// =========================================================================================
// 1. الجزء الخاص بالبيانات والاتصال (ViewModel + Repository)
// =========================================================================================

// نماذج API
data class NominatimResponse(@SerializedName("geojson") val geojson: GeoJsonData)
data class GeoJsonData(@SerializedName("type") val type: String, @SerializedName("coordinates") val coordinates: List<Any>)

// واجهة Retrofit
interface NominatimApiService {
    @GET("search")
    suspend fun getCountryBoundary(
        @Query("country") country: String,
        @Query("format") format: String = "json",
        @Query("polygon_geojson") polygonGeojson: Int = 1,
        @Query("polygon_threshold") threshold: Double = 0.002 // دقة متوسطة لضمان الرسم السريع
    ): List<NominatimResponse>
}

// العميل
object BoundaryApiClient {
    val service: NominatimApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApiService::class.java)
    }
}

class HomeViewModel : ViewModel() {
    val categories = listOf("الكل", "مطاعم", "كافيهات", "منتزهات", "فنادق")
    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory = _selectedCategory.asStateFlow()
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    // حدود الأردن
    private val _jordanBoundary = MutableStateFlow<List<LatLng>>(emptyList())
    val jordanBoundary = _jordanBoundary.asStateFlow()

    // بيانات وهمية
    private val allLocations = listOf(
        NearbyLocation("مطعم السلطان", 4.5, 31.9568, 35.9153, com.jomap.app.R.drawable.ic_launcher_background),
        NearbyLocation("كافيه عمان", 4.2, 31.9523, 35.9108, com.jomap.app.R.drawable.ic_launcher_background)
    )
    private val _filteredLocations = MutableStateFlow(allLocations)
    val locations = _filteredLocations.asStateFlow()

    init {
        fetchJordanBoundary()
    }

    private fun fetchJordanBoundary() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = BoundaryApiClient.service.getCountryBoundary("Jordan")
                if (response.isNotEmpty()) {
                    val boundary = parseGeoJson(response[0].geojson)
                    withContext(Dispatchers.Main) { _jordanBoundary.value = boundary }
                }
            } catch (e: Exception) {
                Log.e("HomeVM", "Error: ${e.message}")
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
            } else emptyList()

            coordsList.forEach { points.add(LatLng(it[1], it[0])) }
        } catch (e: Exception) { }
        return points
    }

    fun onCategorySelected(c: String) { _selectedCategory.value = c }
    fun onSearchTextChange(t: String) { _searchText.value = t }
}

// =========================================================================================
// 2. الجزء الخاص بالشاشة (Screen UI)
// =========================================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMapScreen(
    navController: NavController,
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val locations by viewModel.locations.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val jordanBoundary by viewModel.jordanBoundary.collectAsState()
    val categories = viewModel.categories
    val context = LocalContext.current

    // إعدادات الأذونات
    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasLocationPermission = it[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    // --- إعداد المضلعات ---
    // 1. مضلع العالم (مع عقارب الساعة Clockwise)
    // استخدام 85 درجة بدلاً من 90 لتجنب مشاكل الرسم في خرائط جوجل
    val worldPolygon = listOf(
        LatLng(85.0, -180.0), // Top Left
        LatLng(85.0, 180.0),  // Top Right
        LatLng(-85.0, 180.0), // Bottom Right
        LatLng(-85.0, -180.0) // Bottom Left
    )

    val amman = LatLng(31.9539, 35.9106)
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(amman, 7.5f) }

    Scaffold { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, mapToolbarEnabled = false),
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    minZoomPreference = 7f
                )
            ) {
                if (jordanBoundary.isNotEmpty()) {
                    // القناع (Mask): يرسم العالم كله باللون الأبيض ويستثني الأردن (الثقب)
                    Polygon(
                        points = worldPolygon,
                        holes = listOf(jordanBoundary), // بدون reversed لأن API يعطيها CCW والعالم CW
                        fillColor = Color.White.copy(alpha = 1f), // لون مصمت تماماً
                        strokeWidth = 0f,
                        zIndex = 10f
                    )

                    // حدود الأردن (Outline)
                    Polygon(
                        points = jordanBoundary,
                        fillColor = Color.Transparent,
                        strokeColor = MaterialTheme.colorScheme.primary,
                        strokeWidth = 5f,
                        zIndex = 11f
                    )
                }

                locations.forEach {
                    Marker(
                        state = rememberMarkerState(position = LatLng(it.lat, it.lng)),
                        title = it.name,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                        zIndex = 12f
                    )
                }
            }

            // --- UI Overlays ---
            Column(modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)) {
                SearchBarSection(searchText, { viewModel.onSearchTextChange(it) }, {}, {})
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = cat == selectedCategory,
                            onClick = { viewModel.onCategorySelected(cat) },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.White)
                        )
                    }
                }
            }

            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)) {
                LocationsCarousel(locations, { navController.navigate("location_details/1") })
            }
        }
    }
}

// --- Components (مختصرة لعدم التكرار، استخدم النسخ السابقة إذا أردت التفاصيل الكاملة) ---
@Composable
fun SearchBarSection(text: String, onTextChange: (String) -> Unit, onProfile: () -> Unit, onFav: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().height(56.dp).shadow(6.dp, RoundedCornerShape(28.dp)), shape = RoundedCornerShape(28.dp), color = Color.White) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
            Icon(Icons.Default.Search, null, tint = Color.Gray)
            TextField(value = text, onValueChange = onTextChange, placeholder = { Text("بحث...") }, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), modifier = Modifier.weight(1f))
            Icon(Icons.Default.FavoriteBorder, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun LocationsCarousel(locations: List<NearbyLocation>, onClick: (NearbyLocation) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(locations) { loc ->
            Card(modifier = Modifier.size(160.dp, 120.dp).clickable { onClick(loc) }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(Modifier.padding(8.dp)) {
                    Text(loc.name, fontWeight = FontWeight.Bold)
                    Text("⭐ ${loc.rating}", color = Color.Gray)
                }
            }
        }
    }
}
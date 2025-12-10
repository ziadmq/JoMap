package com.jomap.app.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.jomap.app.viewmodel.HomeViewModel
import com.jomap.app.screens.components.SearchBarSection
import com.jomap.app.screens.components.LocationsCarousel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMapScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    // تجميع الحالات (State Collection)
    val locations by viewModel.locations.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val governorates by viewModel.governorates.collectAsState()
    val selectedGovernorate by viewModel.selectedGovernorate.collectAsState()
    val currentMapStyleRes by viewModel.currentMapStyle.collectAsState()

    val categories = viewModel.categories
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // إذن الموقع
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasLocationPermission = it[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    // إحداثيات مركز الأردن
    val jordanCenter = LatLng(31.2, 36.5)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(jordanCenter, 7.5f)
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                ),
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                        context,
                        currentMapStyleRes
                    )
                )
            ) {
                // رسم المحافظات (Polygons)
                governorates.forEach { gov ->
                    val isSelected = (gov == selectedGovernorate)
                    val baseColor = gov.color

                    val fillColor = if (isSelected)
                        baseColor.copy(alpha = 0.5f)
                    else
                        baseColor.copy(alpha = 0.25f)

                    val strokeColor = if (isSelected) baseColor else baseColor.copy(alpha = 0.8f)
                    val strokeWidth = if (isSelected) 6f else 3f
                    val zIndex = if (isSelected) 2f else 1f

                    Polygon(
                        points = gov.points,
                        fillColor = fillColor,
                        strokeColor = strokeColor,
                        strokeWidth = strokeWidth,
                        zIndex = zIndex,
                        clickable = true,
                        onClick = {
                            viewModel.onGovernorateSelected(gov)
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(
                                        gov.center,
                                        gov.defaultZoom
                                    ),
                                    durationMs = 1200
                                )
                            }
                            // الانتقال لصفحة تفاصيل المحافظة
                            navController.navigate("governorate_details")
                        }
                    )
                }

                // رسم الدبابيس (Markers)
                locations.forEach {
                    Marker(
                        state = rememberMarkerState(position = LatLng(it.lat, it.lng)),
                        title = it.name,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
            }

            // --- واجهة البحث والتصنيفات (في الأعلى) ---
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                SearchBarSection(
                    searchText = searchText,
                    onSearchTextChange = { viewModel.onSearchTextChange(it) },
                    onProfileClick = { navController.navigate("profile") },
                    onFavoritesClick = { navController.navigate("favorites") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = cat == selectedCategory,
                            onClick = { viewModel.onCategorySelected(cat) },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // --- أزرار تبديل المود (على اليسار) ---
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { viewModel.toggleMapMode("Standard") },
                    containerColor = Color.White,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.Refresh, "Standard")
                }

                FloatingActionButton(
                    onClick = { viewModel.toggleMapMode("Petra") },
                    containerColor = Color(0xFFD2B48C),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.WbSunny, "Petra")
                }

                FloatingActionButton(
                    onClick = { viewModel.toggleMapMode("Nightlife") },
                    containerColor = Color(0xFF212121),
                    contentColor = Color(0xFF00E5FF)
                ) {
                    Icon(Icons.Default.Nightlife, "Night")
                }
            }

            // --- شريط الأماكن (في الأسفل) ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                LocationsCarousel(
                    locations = locations,
                    onItemClick = { navController.navigate("location_details/1") }
                )
            }

            // --- زر العودة (Reset View) ---
            // يظهر فقط عند اختيار محافظة
            if (selectedGovernorate != null) {
                FloatingActionButton(
                    onClick = {
                        // استخدام دالة المسح الصحيحة
                        viewModel.clearSelectedGovernorate()
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(jordanCenter, 7.5f)
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp, bottom = 200.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.ZoomOutMap, "عودة")
                }
            }
        }
    }
}
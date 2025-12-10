package com.jomap.app.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.jomap.app.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMapScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val locations by viewModel.locations.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val governorates by viewModel.governorates.collectAsState()
    val selectedGovernorate by viewModel.selectedGovernorate.collectAsState()

    // Map Filter States
    val isTrafficEnabled by viewModel.isTrafficEnabled.collectAsState()
    val showGovernorates by viewModel.showGovernorates.collectAsState()
    val isMapTypeNormal by viewModel.mapTypeNormal.collectAsState()

    val categories = viewModel.categories
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. Permission Logic
    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasLocationPermission = it[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    // 2. Map Configuration (Restricted to Jordan)
    val jordanCenter = LatLng(31.2, 36.5)
    // Jordan Bounds (approximate) to restrict camera
    val jordanBounds = LatLngBounds(
        LatLng(29.185, 34.959), // South West
        LatLng(33.375, 39.300)  // North East
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(jordanCenter, 7.5f)
    }

    Scaffold(
        containerColor = Color(0xFFF1F5F9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            // --- SECTION 1: MAP (Top Half) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f) // Takes roughly half the screen
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, mapToolbarEnabled = false),
                    properties = MapProperties(
                        isMyLocationEnabled = hasLocationPermission,
                        mapType = if (isMapTypeNormal) MapType.NORMAL else MapType.HYBRID,
                        isTrafficEnabled = isTrafficEnabled,
                        // Constrain Map to Jordan Bounds
                        latLngBoundsForCameraTarget = jordanBounds,
                        minZoomPreference = 7.0f, // Prevent zooming out too far
                        maxZoomPreference = 18.0f
                    )
                ) {
                    // Draw Governorates if enabled
                    if (showGovernorates) {
                        governorates.forEach { gov ->
                            val isSelected = (gov == selectedGovernorate)
                            Polygon(
                                points = gov.points,
                                fillColor = if (isSelected) gov.color.copy(alpha = 0.5f) else gov.color.copy(alpha = 0.2f),
                                strokeColor = gov.color,
                                strokeWidth = if (isSelected) 4f else 2f,
                                clickable = true,
                                // UPDATED: Navigate to details on click
                                onClick = {
                                    viewModel.onGovernorateSelected(gov)
                                    navController.navigate(Screen.GovernoratDetails.createRoute(gov.id))
                                }
                            )
                        }
                    }

                    // Markers
                    locations.forEach {
                        Marker(
                            state = rememberMarkerState(position = LatLng(it.lat, it.lng)),
                            title = it.name
                        )
                    }
                }

                // --- MAP FILTER CONTROLS (Top Right) ---
                var showMapMenu by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    // Filter Button
                    SmallFloatingActionButton(
                        onClick = { showMapMenu = !showMapMenu },
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = "Layers")
                    }

                    // Dropdown for Layers
                    DropdownMenu(
                        expanded = showMapMenu,
                        onDismissRequest = { showMapMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Transports (Traffic)") },
                            onClick = { viewModel.toggleTraffic(); showMapMenu = false },
                            leadingIcon = {
                                Icon(Icons.Default.Traffic, null, tint = if(isTrafficEnabled) Color.Green else Color.Gray)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Countries (Govs)") },
                            onClick = { viewModel.toggleGovernorates(); showMapMenu = false },
                            leadingIcon = {
                                Icon(Icons.Default.Public, null, tint = if(showGovernorates) Color.Green else Color.Gray)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Streets / Satellite") },
                            onClick = { viewModel.toggleMapType(); showMapMenu = false },
                            leadingIcon = {
                                Icon(Icons.Default.Map, null, tint = if(isMapTypeNormal) Color.Green else Color.Gray)
                            }
                        )
                    }
                }
            }

            // --- SECTION 2: SEARCH & FILTERS (Bottom Half) ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes remaining space
                    .padding(top = 24.dp), // Spacing from map
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {

                // 1. Search Bar (with Favorites Icon)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp) // Add spacing below search bar
                            .shadow(16.dp, RoundedCornerShape(24.dp))
                            .background(Color.White, RoundedCornerShape(24.dp))
                    ) {
                        SearchBarSection(
                            searchText = searchText,
                            onSearchTextChange = { viewModel.onSearchTextChange(it) },
                            onProfileClick = { navController.navigate("profile") },
                            onFavoritesClick = { navController.navigate("favorites") }
                        )
                    }
                }

                // 2. Filter List (Category Chips)
                item {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(categories) { cat ->
                            val isSelected = cat == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onCategorySelected(cat) },
                                label = { Text(cat) },
                                enabled = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White,
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    labelColor = Color.Black,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // 3. Recommended / Results
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Recommended Places",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // List of places can go here (Vertical or Horizontal)
                    LocationsCarousel(
                        locations = locations,
                        onItemClick = { navController.navigate("location_details/1") }
                    )
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}
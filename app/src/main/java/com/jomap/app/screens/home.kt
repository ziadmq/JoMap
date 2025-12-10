package com.jomap.app.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
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

    // Map States
    val isTrafficEnabled by viewModel.isTrafficEnabled.collectAsState()
    val showGovernorates by viewModel.showGovernorates.collectAsState()
    val isMapTypeNormal by viewModel.mapTypeNormal.collectAsState()

    // Focus target from Community Screen
    val mapFocusTarget by viewModel.mapFocusTarget.collectAsState()

    val categories = viewModel.categories
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasLocationPermission = it[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    val jordanCenter = LatLng(31.2, 36.5)
    val jordanBounds = LatLngBounds(LatLng(29.185, 34.959), LatLng(33.375, 39.300))

    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(jordanCenter, 7.5f) }

    // 🟢 Watch for Focus Requests (from Community Screen)
    LaunchedEffect(mapFocusTarget) {
        mapFocusTarget?.let { target ->
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, 15f), 1000)
            viewModel.clearMapFocus() // Reset after moving
        }
    }

    Scaffold(containerColor = Color(0xFFF1F5F9)) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // --- MAP SECTION ---
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f).clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, mapToolbarEnabled = false),
                    properties = MapProperties(
                        isMyLocationEnabled = hasLocationPermission,
                        mapType = if (isMapTypeNormal) MapType.NORMAL else MapType.HYBRID,
                        isTrafficEnabled = isTrafficEnabled,
                        latLngBoundsForCameraTarget = jordanBounds,
                        minZoomPreference = 7.0f,
                        maxZoomPreference = 18.0f
                    )
                ) {
                    if (showGovernorates) {
                        governorates.forEach { gov ->
                            val isSelected = (gov == selectedGovernorate)
                            if (gov.points.isNotEmpty()) {
                                Polygon(
                                    points = gov.points,
                                    fillColor = if (isSelected) gov.color.copy(alpha = 0.5f) else gov.color.copy(alpha = 0.2f),
                                    strokeColor = gov.color,
                                    strokeWidth = if (isSelected) 4f else 2f,
                                    clickable = true,
                                    onClick = {
                                        viewModel.onGovernorateSelected(gov)
                                        scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(gov.center, gov.defaultZoom)) }
                                    }
                                )
                            }
                            Marker(
                                state = rememberMarkerState(position = gov.center),
                                title = gov.name,
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                                onClick = {
                                    viewModel.onGovernorateSelected(gov)
                                    navController.navigate(Screen.GovernoratDetails.route)
                                    true
                                }
                            )
                        }
                    }
                    locations.forEach {
                        Marker(state = rememberMarkerState(position = LatLng(it.lat, it.lng)), title = it.name)
                    }
                }

                // --- TOP RIGHT CONTROLS ---
                Column(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    // 1. Layer/Filter Button
                    var showMapMenu by remember { mutableStateOf(false) }
                    Box {
                        SmallFloatingActionButton(
                            onClick = { showMapMenu = !showMapMenu },
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) { Icon(Icons.Default.Layers, "Layers") }

                        DropdownMenu(expanded = showMapMenu, onDismissRequest = { showMapMenu = false }, modifier = Modifier.background(Color.White)) {
                            DropdownMenuItem(text = { Text("Traffic") }, onClick = { viewModel.toggleTraffic(); showMapMenu = false }, leadingIcon = { Icon(Icons.Default.Traffic, null) })
                            DropdownMenuItem(text = { Text("Governorates") }, onClick = { viewModel.toggleGovernorates(); showMapMenu = false }, leadingIcon = { Icon(Icons.Default.Public, null) })
                            DropdownMenuItem(text = { Text("Satellite") }, onClick = { viewModel.toggleMapType(); showMapMenu = false }, leadingIcon = { Icon(Icons.Default.Map, null) })
                        }
                    }

                    // 2. Trip Planner Button
                    SmallFloatingActionButton(
                        onClick = { navController.navigate(Screen.TripPlanner.route) },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.EditCalendar, "Plan")
                    }

                    // 🟢 3. Community Button (New)
                    SmallFloatingActionButton(
                        onClick = {
                            val govId = selectedGovernorate?.id ?: "all"
                            if (govId == "all") {
                                Toast.makeText(context, "Showing Community for all Jordan", Toast.LENGTH_SHORT).show()
                            }
                            navController.navigate(Screen.Community.createRoute(govId))
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Groups, "Community")
                    }
                }
            }

            // --- BOTTOM SECTION ---
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 24.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).shadow(16.dp, RoundedCornerShape(24.dp)).background(Color.White, RoundedCornerShape(24.dp))) {
                        SearchBarSection(searchText, { viewModel.onSearchTextChange(it) }, { navController.navigate("profile") }, { navController.navigate("favorites") })
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = cat == selectedCategory,
                                onClick = { viewModel.onCategorySelected(cat) },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(containerColor = Color.White, selectedContainerColor = MaterialTheme.colorScheme.primary, labelColor = Color.Black, selectedLabelColor = Color.White)
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Recommended Places", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LocationsCarousel(locations = locations, onItemClick = { navController.navigate("location_details/1") })
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}
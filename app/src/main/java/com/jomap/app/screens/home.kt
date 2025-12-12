package com.jomap.app.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState // ✅ Added for scrolling
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll // ✅ Added for scrolling
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.jomap.app.R // ✅ Added to access map_style
import com.jomap.app.viewmodel.HomeViewModel
import com.jomap.app.screens.components.LocationsCarousel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMapScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current

    // DATA
    val locations by viewModel.locations.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val categories = viewModel.categories
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    // PERMISSIONS
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        hasPermission = map[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasPermission)
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    // MAP INITIAL POSITION
    val center = LatLng(31.2, 36.5)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 12f)
    }

    // BOTTOM SHEET
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )
    val scaffoldState = rememberBottomSheetScaffoldState(sheetState)
    val isExpanded = sheetState.currentValue == SheetValue.Expanded

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 160.dp,

        // REMOVE DEFAULT HANDLE
        sheetDragHandle = {},

        sheetShape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),

        sheetContainerColor = MaterialTheme.colorScheme.surface,

        sheetContent = {

            // ⭐ UPDATED COLUMN: Full height & Scrollable
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight() // ✅ Ensures it fills the screen when expanded
                    .verticalScroll(rememberScrollState()) // ✅ Allows content to scroll
                    .padding(16.dp)
            ) {

                // ⭐ OUR ONLY HANDLE
                Box(
                    Modifier
                        .width(40.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(10.dp))

                // ⭐ SEARCH BAR
                SearchBarModern(
                    value = searchText,
                    onValueChange = { viewModel.onSearchTextChange(it) },
                    onProfileClick = { navController.navigate("profile") },
                    onFavoritesClick = { navController.navigate("favorites") }
                )

                Spacer(Modifier.height(10.dp))

                // ⭐ CATEGORY CHIPS
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = cat == selectedCategory,
                            onClick = { viewModel.onCategorySelected(cat) },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // COLLAPSED = STOP HERE (Optional: remove this if you want to see content peeking)
                if (!isExpanded) return@Column

                // ⭐ EXPANDED CONTENT
                Spacer(Modifier.height(20.dp))
                BannerSection()

                Spacer(Modifier.height(20.dp))
                Text(
                    "Recommended Places",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(10.dp))

                LocationsCarousel(
                    locations = locations,
                    onItemClick = { navController.navigate("location_details/1") }
                )

                Spacer(Modifier.height(200.dp))
            }
        }
    ) { padding ->

        // ⭐ MAP
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasPermission,
                    // ✅ Apply the Dark Map Style here
                    mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                )
            ) {
                locations.forEach {
                    Marker(
                        state = rememberMarkerState(position = LatLng(it.lat, it.lng)),
                        title = it.name
                    )
                }
            }

            // ⭐ FLOATING BUTTONS
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MapFloatingButton(Icons.Default.Layers) {}
                MapFloatingButton(Icons.Default.Add) {
                    cameraPositionState.move(CameraUpdateFactory.zoomIn())
                }
                MapFloatingButton(Icons.Default.Remove) {
                    cameraPositionState.move(CameraUpdateFactory.zoomOut())
                }
                MapFloatingButton(Icons.Default.MyLocation) {}
            }
        }
    }
}

@Composable
fun SearchBarModern(
    value: String,
    onValueChange: (String) -> Unit,
    onProfileClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            // 🔍 Search icon
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                modifier = Modifier.size(22.dp)
            )

            Spacer(Modifier.width(10.dp))

            // ✏️ Placeholder / text
            Text(
                text = if (value.isBlank()) "Search…" else value,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            // ❤️ Favorite icon
            IconButton(
                onClick = onFavoritesClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
            }

            // 👤 Profile icon
            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun BannerSection() {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(20.dp)
    ) {
        Text(
            "Contribute:\nLet's build Jordan Maps together",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
    }
}

@Composable
fun MapFloatingButton(icon: ImageVector, onClick: () -> Unit) {
    SmallFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(icon, null)
    }
}
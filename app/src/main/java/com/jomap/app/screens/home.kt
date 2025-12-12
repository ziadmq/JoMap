package com.jomap.app.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.jomap.app.R
import com.jomap.app.data.model.Governorate
import com.jomap.app.data.model.NearbyLocation
import com.jomap.app.viewmodel.ActivityAd
import com.jomap.app.viewmodel.HomeViewModel
import com.jomap.app.screens.components.LocationsCarousel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeMapScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current

    // --- DATA ---
    val locations by viewModel.locations.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val categories = viewModel.categories
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedGovernorate by viewModel.selectedGovernorate.collectAsState()
    val governorates by viewModel.governorates.collectAsState()
    val ads by viewModel.ads.collectAsState()

    // --- PERMISSIONS ---
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
        if (!hasPermission) launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    // --- MAP SETUP ---
    val center = LatLng(31.2, 36.5)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 9f)
    }

    // --- BOTTOM SHEET ---
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true
    )
    val scaffoldState = rememberBottomSheetScaffoldState(sheetState)

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 160.dp,
        sheetDragHandle = {},
        sheetShape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            // MAIN SCROLLABLE CONTAINER
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    // ✅ THIS is the ONLY verticalScroll allowed.
                    // Any child inside this Column MUST NOT have verticalScroll.
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 1. Custom Drag Handle
                Box(
                    Modifier
                        .width(40.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(10.dp))

                // 2. Search Bar
                SearchBarModern(
                    value = searchText,
                    onValueChange = { viewModel.onSearchTextChange(it) },
                    onProfileClick = { navController.navigate("profile") },
                    onFavoritesClick = { navController.navigate("favorites") }
                )

                Spacer(Modifier.height(10.dp))

                // 3. Categories / Governorates List (ALWAYS VISIBLE)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(categories) { cat ->
                        val isGovSelected = selectedGovernorate?.name == cat
                        val isSelected = cat == selectedCategory || isGovSelected

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val gov = governorates.find { it.name == cat }
                                if (gov != null) {
                                    if (selectedGovernorate?.id == gov.id) {
                                        viewModel.clearSelectedGovernorate()
                                        viewModel.onCategorySelected("All")
                                    } else {
                                        viewModel.onGovernorateSelected(gov)
                                        cameraPositionState.position = CameraPosition.fromLatLngZoom(gov.center, 10f)
                                    }
                                } else {
                                    viewModel.clearSelectedGovernorate()
                                    viewModel.onCategorySelected(cat)
                                }
                            },
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

                Spacer(Modifier.height(20.dp))

                AnimatedContent(targetState = selectedGovernorate, label = "ContentSwitch") { governorate ->
                    if (governorate != null) {
                        GovernorateDetailContent(
                            governorate = governorate,
                            ads = ads,
                            onBackClick = { viewModel.clearSelectedGovernorate() },
                            onLocationClick = { loc -> navController.navigate("location_details/${loc.id}") }
                        )
                    } else {
                        Column {
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
                                onItemClick = { navController.navigate("location_details/${it.id}") }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    ) { padding ->
        // --- GOOGLE MAP ---
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasPermission,
                    mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
            ) {
                locations.forEach {
                    Marker(state = rememberMarkerState(position = LatLng(it.lat, it.lng)), title = it.name)
                }
            }
            // --- MAP BUTTONS ---
            Column(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MapFloatingButton(Icons.Default.Layers) { }
                MapFloatingButton(Icons.Default.Add) { cameraPositionState.move(CameraUpdateFactory.zoomIn()) }
                MapFloatingButton(Icons.Default.Remove) { cameraPositionState.move(CameraUpdateFactory.zoomOut()) }
                MapFloatingButton(Icons.Default.MyLocation) { }
            }
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun GovernorateDetailContent(
    governorate: Governorate,
    ads: List<ActivityAd>,
    onBackClick: () -> Unit,
    onLocationClick: (NearbyLocation) -> Unit
) {
    // ❌ REMOVED verticalScroll() from here to fix the crash
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // --- 1. Header Image & Title ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(24.dp))
        ) {
            Image(
                painter = painterResource(id = governorate.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 200f
                        )
                    )
            )
            // Back Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            // Title
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = governorate.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(text = "Jordan", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 2. Description (About) ---
        Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = governorate.description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- 3. History Section ---
        HistorySection(
            historyText = governorate.history,
            locations = governorate.bestLocations,
            onLocationClick = onLocationClick
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- 4. Recommended Places (Horizontal Scroll) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recommended Places", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = { /* See All Logic */ }) { Text("See All") }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(governorate.bestLocations) { location ->
                RecommendedPlaceCard(location = location, onClick = { onLocationClick(location) })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ 5. ADS SECTION
        if (ads.isNotEmpty()) {
            AdsSection(ads)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- 6. Events ---
        if (governorate.events.isNotEmpty()) {
            Text("Upcoming Events", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            governorate.events.forEach { event ->
                EventItem(event)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// --- HELPER COMPONENTS ---

@Composable
fun RecommendedPlaceCard(location: NearbyLocation, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(190.dp),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = location.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.height(110.dp).fillMaxWidth()
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(location.name, fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 14.sp)
                Text(location.category, color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${location.rating}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun EventItem(eventName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFE3F2FD), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Event, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(eventName, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun HistorySection(
    historyText: String,
    locations: List<NearbyLocation>,
    onLocationClick: (NearbyLocation) -> Unit
) {
    val historicalSites = locations.filter {
        it.category == "History" || it.category == "Museum" || it.category == "Heritage"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Historical Significance",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = historyText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        if (historicalSites.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Key Historical Sites",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(historicalSites) { site ->
                    HistoricalSiteCard(site = site, onClick = { onLocationClick(site) })
                }
            }
        }
    }
}

@Composable
fun HistoricalSiteCard(site: NearbyLocation, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Image(
                painter = painterResource(id = site.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.height(90.dp).fillMaxWidth()
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = site.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Place,
                        null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Text("Landmark", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun AdsSection(ads: List<ActivityAd>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Trending Activities", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Sponsored", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
            items(ads) { ad -> AdCard(ad) }
        }
    }
}

@Composable
fun AdCard(ad: ActivityAd) {
    Card(
        modifier = Modifier.width(260.dp).height(160.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = ad.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 100f
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = ad.price,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = ad.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ad.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1
                )
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
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Search,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                if (value.isBlank()) "Search..." else value,
                color = Color.Gray,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onFavoritesClick, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.FavoriteBorder, null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onProfileClick, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun BannerSection() {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
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
            "Explore Jordan's Gems!",
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
        shape = RoundedCornerShape(12.dp)
    ) { Icon(icon, null) }
}
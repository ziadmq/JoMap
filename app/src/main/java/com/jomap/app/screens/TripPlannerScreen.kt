package com.jomap.app.screens

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jomap.app.data.model.NearbyLocation
import com.jomap.app.viewmodel.HomeViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripPlannerScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val allLocations by viewModel.locations.collectAsState()
    val selectedIds by viewModel.tripSelection.collectAsState()

    // State to toggle between "Selecting" and "Viewing Plan"
    var isViewingPlan by remember { mutableStateOf(false) }

    // Assuming User is in Amman (31.95, 35.91) for sorting logic
    val userLat = 31.9539
    val userLng = 35.9106

    val plannedTrip = remember(selectedIds, isViewingPlan) {
        if (isViewingPlan) viewModel.getOptimizedTripPlan(userLat, userLng) else emptyList()
    }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isViewingPlan) "Your Trip Itinerary" else "Select Places") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isViewingPlan) isViewingPlan = false else navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!isViewingPlan) {
                        TextButton(onClick = { viewModel.clearTrip() }) {
                            Text("Clear", color = Color.Red)
                        }
                    } else {
                        IconButton(onClick = { downloadTripPDF(context, plannedTrip) }) {
                            Icon(Icons.Default.Download, "Download PDF")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isViewingPlan && selectedIds.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { isViewingPlan = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Text("Create Plan (${selectedIds.size})")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, null)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (!isViewingPlan) {
                // --- STEP 1: SELECTION LIST ---
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    item {
                        Text("Select places you want to visit:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(allLocations) { location ->
                        val isSelected = selectedIds.contains(location.id)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.toggleTripLocation(location.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = isSelected, onCheckedChange = { viewModel.toggleTripLocation(location.id) })
                                Column {
                                    Text(text = location.name, fontWeight = FontWeight.Bold)
                                    Text(text = location.category, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            } else {
                // --- STEP 2: TIMELINE PLAN ---
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sorted from nearest to you (Amman Center)")
                            }
                        }
                    }

                    items(plannedTrip) { location ->
                        TimelineItem(location = location, isLast = location == plannedTrip.last())
                    }

                    item {
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineItem(location: NearbyLocation, isLast: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        // Line Column
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text((location.name.firstOrNull() ?: '?').toString(), color = Color.White, fontSize = 12.sp)
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color.Gray.copy(alpha = 0.5f))
                )
            }
        }

        // Content Column
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, bottom = 24.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = location.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Category, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = location.category, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        }
    }
}

// --- PDF DOWNLOAD FUNCTION ---
fun downloadTripPDF(context: Context, trip: List<NearbyLocation>) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = android.graphics.Paint()

    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("My Jordan Trip Plan", 50f, 60f, paint)

    paint.textSize = 16f
    paint.isFakeBoldText = false
    var y = 100f

    trip.forEachIndexed { index, loc ->
        val text = "${index + 1}. ${loc.name} (${loc.category})"
        canvas.drawText(text, 50f, y, paint)
        y += 40f
    }

    pdfDocument.finishPage(page)

    // Save to Downloads folder
    val fileName = "Trip_Plan_${System.currentTimeMillis()}.pdf"
    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

    try {
        pdfDocument.writeTo(FileOutputStream(file))
        Toast.makeText(context, "Saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}
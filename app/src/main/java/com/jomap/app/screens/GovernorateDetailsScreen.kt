package com.jomap.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jomap.app.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GovernorateDetailsScreen(
    navController: NavController,
    viewModel: HomeViewModel // نستخدم نفس الـ ViewModel لأن البيانات موجودة فيه
) {
    val governorate by viewModel.selectedGovernorate.collectAsState()

    if (governorate == null) {
        // إذا لم يكن هناك محافظة مختارة، نعود للخلف
        navController.popBackStack()
        return
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { /* فتح "المساعد الذكي" مستقبلاً */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.AutoAwesome, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("اسأل عن ${governorate!!.name}")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // 1. صورة الغلاف والعنوان
            item {
                Box(modifier = Modifier.height(250.dp).fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = governorate!!.imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )
                    IconButton(
                        onClick = {
                            viewModel.clearSelectedGovernorate()
                            navController.popBackStack()
                        },
                        modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                    ) {
                        Text(
                            text = governorate!!.name,
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. نبذة وتاريخ
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader("نبذة عامة", Icons.Default.Info)
                    Text(
                        text = governorate!!.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SectionHeader("التاريخ", Icons.Default.History)
                    Text(
                        text = governorate!!.history,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 24.sp
                    )
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // 3. أفضل الأماكن (Best Locations)
            if (governorate!!.bestLocations.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        PaddingBox { SectionHeader("أفضل الأماكن للزيارة", Icons.Default.Place) }
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(governorate!!.bestLocations) { loc ->
                                LocationMiniCard(loc) {
                                    // الانتقال لتفاصيل المكان
                                    navController.navigate("location_details/${loc.id}")
                                }
                            }
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            // 4. الفعاليات (Events)
            if (governorate!!.events.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader("أهم الفعاليات والمهرجانات", Icons.Default.Event)
                        governorate!!.events.forEach { event ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Celebration, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = event, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            // مساحة في الأسفل للزر العائم
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PaddingBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) { content() }
}

@Composable
fun LocationMiniCard(location: com.jomap.app.data.model.NearbyLocation, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(160.dp).height(180.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = location.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.height(100.dp).fillMaxWidth()
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = location.name, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(12.dp), tint = Color(0xFFFFB300))
                    Text(text = "${location.rating}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
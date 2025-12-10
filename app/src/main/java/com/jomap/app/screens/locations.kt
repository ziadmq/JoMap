package com.jomap.app.screens

import android.widget.Toast
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.jomap.app.data.model.NearbyLocation
import com.jomap.app.viewmodel.LocationListViewModel
import com.jomap.app.viewmodel.SortOption
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.graphics.Brush
import com.jomap.app.viewmodel.OwnerPost
import com.jomap.app.viewmodel.Review
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.jomap.app.viewmodel.LocationDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationListScreen(
    navController: NavController,
    viewModel: LocationListViewModel = viewModel()
) {
    val locations by viewModel.uiLocations.collectAsState()
    val activeLocation by viewModel.activeReviewLocation.collectAsState() // مراقبة حالة المراجعة
    val context = LocalContext.current

    // ✅ هنا الكود المسؤول عن فتح النافذة المنبثقة بدلاً من شاشة فارغة
    if (activeLocation != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeReviewDialog() },
            containerColor = Color.White
        ) {
            AddReviewBottomSheetContent(
                locationName = activeLocation!!.name,
                viewModel = viewModel,
                onSuccess = {
                    Toast.makeText(context, "تم إرسال التقييم بنجاح!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("استكشف الأماكن") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF8F9FA))) {
            // ... (أكواد الفلترة والقائمة كما هي)

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(locations) { location ->
                    LocationItemCard(
                        location = location,
                        onDetailsClick = {
                            // هذا سينقلك لشاشة التفاصيل (تأكد أنك فعلتها في AppNavGraph)
                            navController.navigate("location_details/${location.id}")
                        },
                        onMapClick = {
                            navController.previousBackStackEntry?.savedStateHandle?.set("focus_location", location.id)
                            navController.popBackStack()
                        },
                        onReviewClick = {
                            // ✅ هذا يفتح الـ BottomSheet ولا يقوم بعمل Navigate
                            viewModel.openReviewDialog(location)
                        }
                    )
                }
            }
        }
    }
}

// ... (تأكد أن الدوال المساعدة LocationItemCard و AddReviewBottomSheetContent موجودة في الملف كما أرسلتها سابقاً)
@Composable
fun AddReviewBottomSheetContent(
    locationName: String,
    viewModel: LocationListViewModel,
    onSuccess: () -> Unit
) {
    val rating by viewModel.rating.collectAsState()
    val reviewText by viewModel.reviewText.collectAsState()

    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("قيّم $locationName", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            for (i in 1..5) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (i <= rating) Color(0xFFFFB300) else Color.Gray,
                    modifier = Modifier.size(48.dp).clickable { viewModel.setRating(i) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = reviewText,
            onValueChange = { viewModel.onReviewTextChanged(it) },
            label = { Text("ملاحظاتك") },
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.submitReview(); onSuccess() },
            modifier = Modifier.fillMaxWidth(),
            enabled = rating > 0
        ) {
            Text("إرسال")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun LocationItemCard(
    location: NearbyLocation,
    onDetailsClick: () -> Unit,
    onMapClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Image(
                painter = painterResource(id = location.imageRes), contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp)).background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(location.name, fontWeight = FontWeight.Bold)
                    // زر التقييم يفتح الـ BottomSheet
                    IconButton(onClick = onReviewClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.RateReview, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Text("${location.rating} ★", color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onDetailsClick, modifier = Modifier.weight(1f).height(35.dp), contentPadding = PaddingValues(0.dp)) { Text("التفاصيل", fontSize = 12.sp) }
                    OutlinedButton(onClick = onMapClick, modifier = Modifier.weight(1f).height(35.dp), contentPadding = PaddingValues(0.dp)) { Text("الخريطة", fontSize = 12.sp) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailsScreen(
    navController: NavController,
    locationId: String,
    viewModel: LocationDetailsViewModel = viewModel()
) {
    LaunchedEffect(locationId) { viewModel.loadLocation(locationId) }
    val location by viewModel.selectedLocation.collectAsState()

    if (location == null) return

    Scaffold(
        bottomBar = {
            // زر الحجز أو الاتصال العائم في الأسفل
            Button(
                onClick = { /* Action */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("احجز الآن / اتصل", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. صورة الغلاف (Header Image)
            item {
                Box(modifier = Modifier.height(300.dp).fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = location!!.imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // تدرج لوني للنصوص
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                    startY = 300f
                                )
                            )
                    )
                    // أزرار العودة والمفضلة
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                        IconButton(
                            onClick = { /* Favorite */ },
                            modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Fav", tint = Color.Red)
                        }
                    }
                    // اسم المكان وتقييمه فوق الصورة
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = location!!.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${location!!.distanceKm} كم عن موقعك",
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // 2. قسم التقييم والمعلومات السريعة
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoBadge(icon = Icons.Default.Star, value = "${location!!.rating}", label = "التقييم", color = Color(0xFFFFB300))
                    InfoBadge(icon = Icons.Default.Category, value = location!!.category, label = "الفئة", color = MaterialTheme.colorScheme.primary)
                    InfoBadge(icon = Icons.Default.AccessTime, value = "مفتوح", label = "الحالة", color = Color.Green)
                }
                Divider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
            }

            // 3. قسم "منشور المالك" (Owner Post)
            item {
                SectionTitle(title = "تحديث من المالك")
                OwnerPostCard(post = viewModel.ownerPost)
            }

            // 4. نبذة عن المكان
            item {
                SectionTitle(title = "عن المكان")
                Text(
                    text = "هذا المكان يقدم تجربة فريدة من نوعها تجمع بين الأصالة والحداثة. نتميز بتقديم أفضل الخدمات لضمان راحتكم.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // 5. قسم المراجعات والتقييمات (Reviews)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(title = "التقييمات والمراجعات")
                    TextButton(onClick = { navController.navigate("add_review/${locationId}") }) {
                        Text("أضف تقييم")
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.reviews) { review ->
                        ReviewCard(review = review)
                    }
                }
                Spacer(modifier = Modifier.height(100.dp)) // مساحة لزر الحجز السفلي
            }
        }
    }
}

// --- Components (مكونات التصميم) ---

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun InfoBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun OwnerPostCard(post: OwnerPost) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // صورة المالك (رمزية)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = post.ownerName, fontWeight = FontWeight.Bold)
                    Text(text = post.time, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = post.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(140.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = review.name.first().toString(), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = review.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                // النجوم
                Row {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                    Text(text = "${review.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(text = review.date, style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
        }
    }
}
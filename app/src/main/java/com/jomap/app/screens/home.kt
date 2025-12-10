package com.jomap.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*    // أو Material3
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*  // مكونات خرائط Compose
import com.jomap.app.viewmodel.HomeViewModel

@Composable
fun HomeMapScreen(
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // الحصول على قائمة المواقع من الـViewModel (كمتغير حالة قابل لإعادة التركيب)
    val locations by viewModel.nearbyLocations.collectAsState()

    // تحديد موقع الكاميرا الابتدائي (نستخدم أول موقع في القائمة كمركز)
    val initialPosition = locations.firstOrNull()?.let { LatLng(it.lat, it.lng) }
        ?: LatLng(0.0, 0.0)  // موقع افتراضي (في حال القائمة فارغة)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 14f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "الخريطة") },
                actions = {
                    // زر بحث في الشريط العلوي (يمكن استبداله بحقل نص للبحث إذا رغبت)
                    IconButton(onClick = { /* TODO: وظيفة البحث */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { innerPadding ->
        // محتوى الشاشة (الخريطة + قائمة الأماكن) داخل الـScaffold
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            // الخريطة تغطي المساحة العلوية المتبقية من الشاشة
            Box(modifier = Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    // يمكن تخصيص خصائص الخريطة (مثلاً نوع الخريطة أو إظهار حركة المرور) عبر MapProperties
                    // properties = MapProperties(mapType = MapType.NORMAL)
                    // إعدادات واجهة الخريطة (إظهار زر الموقع وغيره) عبر MapUiSettings إذا لزم
                    // uiSettings = MapUiSettings(myLocationButtonEnabled = true)
                ) {
                    // إضافة علامات (Markers) لكل موقع قريب على الخريطة
                    for (location in locations) {
                        Marker(
                            state = rememberMarkerState(position = LatLng(location.lat, location.lng)),
                            title = location.name
                        )
                    }
                }
            }

            // مساحة فارغة صغيرة بين الخريطة والقائمة
            Spacer(modifier = Modifier.height(8.dp))

            // قائمة أفقية للأماكن القريبة
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(locations) { location ->
                    Card(
                        modifier = Modifier
                            .width(140.dp)
                            .padding(end = 8.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            // صورة المكان (من الموارد التخزينية للتطبيق)
                            Image(
                                painter = painterResource(id = location.imageRes),
                                contentDescription = location.name,
                                modifier = Modifier
                                    .height(80.dp)
                                    .fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                            // اسم المكان
                            Text(
                                text = location.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.body1
                            )
                            // تقييم المكان
                            Text(
                                text = "التقييم: %.1f".format(location.rating),
                                style = MaterialTheme.typography.body2
                            )
                        }
                    }
                }
            }
        }
    }
}







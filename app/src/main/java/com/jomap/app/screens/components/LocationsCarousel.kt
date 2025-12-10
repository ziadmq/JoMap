package com.jomap.app.screens.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jomap.app.data.model.NearbyLocation

@Composable
fun LocationsCarousel(
    locations: List<NearbyLocation>,
    onItemClick: (NearbyLocation) -> Unit,
    onSeeAllClick: () -> Unit = {} // دالة اختيارية لزر "عرض الكل"
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp), // مسافة في بداية ونهاية القائمة
        horizontalArrangement = Arrangement.spacedBy(16.dp), // مسافة بين كل عنصر وآخر
        modifier = Modifier.fillMaxWidth()
    ) {
        // 1. عرض العناصر الموجودة في القائمة
        items(locations) { location ->
            LocationCard(
                location = location,
                onClick = { onItemClick(location) }
            )
        }

        // 2. بطاقة "عرض الكل" في نهاية القائمة
        item {
            SeeAllCard(onClick = onSeeAllClick)
        }
    }
}

@Composable
fun LocationCard(
    location: NearbyLocation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp) // عرض ثابت للبطاقة
            .height(210.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // صورة الموقع
            Image(
                painter = painterResource(id = location.imageRes),
                contentDescription = location.name,
                modifier = Modifier
                    .height(120.dp) // ارتفاع الصورة
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )

            // تفاصيل الموقع
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                // الاسم
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // التقييم والمسافة
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // نجمة التقييم
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300), // لون ذهبي
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // رقم التقييم
                    Text(
                        text = "${location.rating}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.weight(1f)) // دفع المسافة لليسار

                    // المسافة (قيمة ثابتة للتجربة)
                    Text(
                        text = "1.2 كم",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SeeAllCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(width = 80.dp, height = 210.dp) // بطاقة نحيفة
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "عرض\nالكل",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
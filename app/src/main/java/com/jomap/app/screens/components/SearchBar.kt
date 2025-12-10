package com.jomap.app.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SearchBarSection(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onProfileClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    // Surface لعمل الخلفية البيضاء والظل والحواف الدائرية
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 6.dp, // قوة الظل
                shape = RoundedCornerShape(28.dp), // درجة التدوير الكاملة
                clip = false
            ),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface, // لون الخلفية (أبيض عادة)
        tonalElevation = 6.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp) // مسافة داخلية من الجوانب
        ) {
            // 1. أيقونة البحث
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "بحث",
                tint = Color.Gray
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 2. حقل الكتابة (شفاف وبدون خط سفلي)
            TextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                placeholder = {
                    Text(
                        text = "ابحث عن مطعم، مكان...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                },
                modifier = Modifier.weight(1f), // يأخذ كل المساحة المتاحة في الوسط
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent, // إخفاء الخط السفلي عند الكتابة
                    unfocusedIndicatorColor = Color.Transparent, // إخفاء الخط السفلي
                )
            )

            // 3. زر المفضلة
            IconButton(onClick = onFavoritesClick) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "المفضلة",
                    tint = MaterialTheme.colorScheme.primary // لون التطبيق الأساسي
                )
            }

            // 4. زر الملف الشخصي
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "الملف الشخصي",
                    tint = Color.Gray
                )
            }
        }
    }
}
package com.jomap.app.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SearchBarClean(
    value: String,
    onValueChange: (String) -> Unit,
    onProfileClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(Color(0xFFF0F2F7))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)

        Spacer(Modifier.width(8.dp))

        Text(
            text = value.ifEmpty { "Search place…" },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onFavoritesClick) {
            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.Gray)
        }

        IconButton(onClick = onProfileClick) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
        }
    }
}

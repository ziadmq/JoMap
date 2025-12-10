package com.jomap.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jomap.app.data.model.CommunityPost
import com.jomap.app.data.model.PostType
import com.jomap.app.ui.navigation.Screen
import com.jomap.app.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    governorateId: String
) {
    val posts by viewModel.communityPosts.collectAsState()
    val governorates by viewModel.governorates.collectAsState()

    // Filter posts for this governorate (or show all if "all" is passed)
    val filteredPosts = remember(posts, governorateId) {
        if (governorateId == "all") posts else posts.filter { it.governorateId == governorateId }
    }

    val govName = governorates.find { it.id == governorateId }?.name ?: "All Jordan"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Community Feed")
                        Text(govName, style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF5F5F5))) {
            if (filteredPosts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No updates in this community yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(filteredPosts) { post ->
                        CommunityPostCard(post) {
                            // On "Show Location" Click
                            viewModel.focusOnLocation(post.location)
                            // Navigate back to Home Map (using popUpTo to clear stack if needed, or just navigate)
                            navController.navigate(Screen.HomeMap.route) {
                                popUpTo(Screen.HomeMap.route) { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityPostCard(post: CommunityPost, onShowLocationClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Header: Icon + Name + Date
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color.LightGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (post.type == PostType.OFFER) Icons.Default.LocalOffer else Icons.Default.Event,
                        null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(post.placeName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(post.date, color = Color.Gray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Badge(
                    containerColor = if (post.type == PostType.OFFER) Color(0xFFFF9800) else Color(0xFF2196F3)
                ) {
                    Text(post.type.name, modifier = Modifier.padding(4.dp), color = Color.White)
                }
            }

            // Image (Fake Placeholder)
            Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(Color.Gray)) {
                Image(
                    painter = painterResource(id = post.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Content
            Column(modifier = Modifier.padding(16.dp)) {
                Text(post.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                // Button to Open Map
                Button(
                    onClick = onShowLocationClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Show Location on Map", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
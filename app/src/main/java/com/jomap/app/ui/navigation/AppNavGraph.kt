package com.jomap.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jomap.app.data.model.LocationListScreen
import com.jomap.app.screens.AddReviewScreen
import com.jomap.app.ui.screens.*

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.HomeMap.route
    ) {
        composable(Screen.HomeMap.route) {
            HomeMapScreen(navController)
        }
        composable(Screen.LocationList.route) {
            LocationListScreen(navController)
        }
        composable(Screen.LocationDetails.route) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: ""
//            LocationDetailsScreen(navController, locationId)
        }
        composable(Screen.AddReview.route) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: ""
//            AddReviewScreen(navController, locationId)
        }
        composable(Screen.Favorites.route) {
//            FavoritesScreen(navController)
        }
        composable(Screen.Profile.route) {
//            ProfileScreen(navController)
        }
        composable(Screen.Recommendations.route) {
//            RecommendationsScreen(navController)
        }
    }
}


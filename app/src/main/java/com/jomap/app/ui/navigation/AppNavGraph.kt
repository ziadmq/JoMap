package com.jomap.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jomap.app.screens.*
import com.jomap.app.viewmodel.HomeViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.HomeMap.route
    ) {
        // 1. Home Map
        composable(Screen.HomeMap.route) {
            HomeMapScreen(navController, homeViewModel)
        }

        // 2. Location List
        composable(Screen.LocationList.route) {
            LocationListScreen(navController)
        }

        // 3. Location Details
        composable(Screen.LocationDetails.route) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: "0"
            LocationDetailsScreen(navController, locationId)
        }

        // 4. Add Review
        composable(Screen.AddReview.route) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: ""
            AddReviewScreen(navController, locationId)
        }

        // 5. Favorites
        composable(Screen.Favorites.route) {
            FavoritesScreen(navController)
        }

        // 6. Profile
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }

        // 7. Trip Planner
        composable(Screen.TripPlanner.route) {
            TripPlannerScreen(navController, homeViewModel)
        }

        // 8. Community
        composable(Screen.Community.route) { backStackEntry ->
            val govId = backStackEntry.arguments?.getString("governorateId") ?: "0"
            CommunityScreen(navController, homeViewModel, govId)
        }
    }
}
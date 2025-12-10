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
        composable(Screen.HomeMap.route) {
            HomeMapScreen(navController, homeViewModel)
        }

        composable(Screen.LocationList.route) {
            LocationListScreen(navController)
        }

        composable(Screen.LocationDetails.route) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: "0"
            LocationDetailsScreen(navController, locationId)
        }

        composable(Screen.AddReview.route) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: ""
            AddReviewScreen(navController, locationId)
        }

        composable(Screen.Favorites.route) {
            LocationListScreen(navController)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }

        composable(Screen.GovernoratDetails.route) {
            GovernorateDetailsScreen(navController, homeViewModel)
        }

        composable(Screen.TripPlanner.route) {
            TripPlannerScreen(navController, homeViewModel)
        }

        // 🟢 New Community Screen
        composable(Screen.Community.route) { backStackEntry ->
            val govId = backStackEntry.arguments?.getString("governorateId") ?: "0"
            CommunityScreen(navController, homeViewModel, govId)
        }
    }
}
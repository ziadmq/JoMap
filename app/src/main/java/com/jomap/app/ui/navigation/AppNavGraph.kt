package com.jomap.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
// تأكد من استدعاء الشاشات من الباكيج الصحيح
import com.jomap.app.screens.HomeMapScreen
import com.jomap.app.screens.LocationListScreen
import com.jomap.app.screens.LocationDetailsScreen
import com.jomap.app.screens.AddReviewScreen // إذا كنت ستستخدمها كشاشة منفصلة أيضاً
import com.jomap.app.screens.FavoritesScreen
import com.jomap.app.screens.GovernorateDetailsScreen
import com.jomap.app.screens.ProfileScreen
import com.jomap.app.viewmodel.HomeViewModel

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
            val locationId = backStackEntry.arguments?.getString("locationId") ?: "0"
            LocationDetailsScreen(navController, locationId)
        }

        composable(Screen.AddReview.route) { backStackEntry ->
            val locationId = backStackEntry.arguments?.getString("locationId") ?: ""
            AddReviewScreen(navController, locationId)
        }

        // يمكنك تفعيل الباقي عند إنشائهم
        composable(Screen.Favorites.route) {
            LocationListScreen(navController)

//            FavoritesScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Screen.Recommendations.route) {

        }
        composable(Screen.GovernoratDetails.route) { backStackEntry ->
            val govId = backStackEntry.arguments?.getString("govId")

            // Create or get ViewModel
            val homeViewModel: HomeViewModel = viewModel()

            // Select the governorate so the screen has data
            if (govId != null) {
                homeViewModel.selectGovernorateById(govId)
            }

            GovernorateDetailsScreen(navController, homeViewModel)
        }
    }
}
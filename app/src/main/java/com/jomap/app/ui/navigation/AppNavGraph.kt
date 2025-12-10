package com.jomap.app.ui.navigation

import androidx.compose.runtime.Composable
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
            FavoritesScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Screen.Recommendations.route) {

        }
        composable("governorate_details") {
            // نحتاج للـ HomeViewModel المشترك أو إنشاء واحد جديد
            // الأفضل هنا استخدام koin أو hilt للمشاركة، لكن للتبسيط سنستدعيه:
            val homeViewModel: com.jomap.app.viewmodel.HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            GovernorateDetailsScreen(navController, homeViewModel)
        }
    }
}
// File: app/src/main/java/com/jomap/app/ui/navigation/Screen.kt
package com.jomap.app.ui.navigation

sealed class Screen(val route: String) {
    data object HomeMap : Screen("home_map")
    data object LocationList : Screen("location_list")
    data object LocationDetails : Screen("location_details/{locationId}") {
        fun createRoute(locationId: String) = "location_details/$locationId"
    }
    data object AddReview : Screen("add_review/{locationId}") {
        fun createRoute(locationId: String) = "add_review/$locationId"
    }
    data object Favorites : Screen("favorites")

    // UPDATED: Added argument for ID
    data object GovernoratDetails : Screen("governorate_details/{govId}") {
        fun createRoute(govId: String) = "governorate_details/$govId"
    }

    data object Profile : Screen("profile")
    data object Recommendations : Screen("recommendations")
}
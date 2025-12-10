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
    data object Profile : Screen("profile")
    data object Recommendations : Screen("recommendations")
    // لاحقاً: OwnerDashboard, AdminPanel لو حبيت تضيفهم للموبايل
}

package com.example.fleetsync

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object SignUp : Screen("signup_screen")
    object AdminDashboard : Screen("admin_dashboard")
    object FleetOwnerDashboard : Screen("fleet_owner_dashboard")
    object DriverDashboard : Screen("driver_dashboard")
    object ClientDashboard : Screen("client_dashboard")
    object Map : Screen("map_screen")
    object Orders : Screen("orders_screen")
    object Settings : Screen("settings_screen")
    object Notifications : Screen("notifications_screen")
    object TrackOrder : Screen("track_order/{tripId}") {
        fun createRoute(tripId: String) = "track_order/$tripId"
    }
    object ClientMapTracking : Screen("client_map_tracking/{tripId}") {
        fun createRoute(tripId: String) = "client_map_tracking/$tripId"
    }
}

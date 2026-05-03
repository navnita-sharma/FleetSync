package com.example.fleetsync

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth 
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize


@Composable
fun AppNavigation(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val authState by authViewModel.authState
    val context = LocalContext.current

    var isDarkMode by remember { mutableStateOf(false) }
    var isCheckingSession by remember { mutableStateOf(true) }

    // Session Persistence Check
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Fetch user's role from Firestore
            FirebaseFirestore.getInstance().collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role") ?: "Fleet Owner"
                        val destination = when (role) {
                            "Admin" -> Screen.AdminDashboard.route
                            "Fleet Owner" -> Screen.FleetOwnerDashboard.route
                            "Driver" -> Screen.DriverDashboard.route
                            "Client" -> Screen.ClientDashboard.route
                            else -> Screen.FleetOwnerDashboard.route
                        }
                        navController.navigate(destination) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                    isCheckingSession = false
                }
                .addOnFailureListener {
                    isCheckingSession = false
                }
        } else {
            isCheckingSession = false
        }
    }

    // Handle AuthState changes (for manual login/signup)
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                val role = (authState as AuthState.Success).role
                authViewModel.resetState()

                val destination = when (role) {
                    "Admin" -> Screen.AdminDashboard.route
                    "Fleet Owner" -> Screen.FleetOwnerDashboard.route
                    "Driver" -> Screen.DriverDashboard.route
                    "Client" -> Screen.ClientDashboard.route
                    else -> Screen.FleetOwnerDashboard.route
                }

                navController.navigate(destination) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            is AuthState.Error -> {
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    if (isCheckingSession) {
        Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFE68A1E))
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route
        ) {
            composable(route = Screen.Login.route) {
                FleetSyncLoginScreen(
                    authState = authState,
                    onLoginClick = { email, password ->
                        authViewModel.loginUser(email, password)
                    },
                    onSignUpClick = {
                        navController.navigate(Screen.SignUp.route)
                    },
                    onTrackShipmentClick = {
                        navController.navigate("guest_tracking_login")
                    }
                )
            }

            composable(route = "guest_tracking_login") {
                CustomerTrackLoginScreen(
                    onTrackClick = { shipmentId, passcode ->
                        FirebaseFirestore.getInstance().collection("trips")
                            .whereEqualTo("tripId", shipmentId)
                            .whereEqualTo("trackingPasskey", passcode)
                            .get()
                            .addOnSuccessListener { snap ->
                                if (!snap.isEmpty) {
                                    navController.navigate(Screen.TrackOrder.createRoute(shipmentId))
                                } else {
                                    Toast.makeText(context, "Invalid ID or Passcode", Toast.LENGTH_SHORT).show()
                                }
                            }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(route = Screen.SignUp.route) {
                CreateAccountScreen(
                    isDarkMode = isDarkMode,
                    authState = authState,
                    authViewModel = authViewModel,
                    onRegisterClick = { email, password, name, phone, role, companyName, companyId ->
                        authViewModel.registerUser(email, password, name, phone, role, companyName, companyId)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = Screen.AdminDashboard.route) {
                AdminDashboard()
            }

            composable(route = Screen.FleetOwnerDashboard.route) {
                FleetOwnerDashboard(
                    isDarkMode = isDarkMode,
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onMapClick = { navController.navigate(Screen.Map.route) },
                    onOrdersClick = { navController.navigate(Screen.Orders.route) },
                    onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
                )
            }

            composable(route = Screen.DriverDashboard.route) {
                DriverDashboard(
                    isDarkMode = isDarkMode,
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onNotificationsClick = { navController.navigate(Screen.Notifications.route) }
                )
            }

            composable(route = Screen.ClientDashboard.route) {
                CustomerTrackingDashboard(
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                    }
                )
            }

            composable(route = Screen.Map.route) {
                FleetTrackingScreen(
                    isDarkMode = isDarkMode,
                    onDashboardClick = { navController.navigate(Screen.FleetOwnerDashboard.route) },
                    onOrdersClick = { navController.navigate(Screen.Orders.route) },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) }
                )
            }

            composable(route = Screen.Orders.route) {
                OrdersScreen(
                    isDarkMode = isDarkMode,
                    onBackClick = { navController.popBackStack() },
                    onMapClick = { navController.navigate(Screen.Map.route) },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onTrackClick = { tripId -> navController.navigate(Screen.TrackOrder.createRoute(tripId)) }
                )
            }

            composable(route = Screen.Settings.route) {
                SettingsScreen(
                    isDarkMode = isDarkMode,
                    onThemeChanged = { isDarkMode = it },
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                    }
                )
            }

            composable(route = Screen.Notifications.route) {
                NotificationsScreen(
                    isDarkMode = isDarkMode,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(route = Screen.TrackOrder.route) {
                val fleetViewModel: FleetDashboardViewModel = viewModel()
                val tripId = it.arguments?.getString("tripId") ?: ""
                val trip = fleetViewModel.liveTrips.find { t -> t.tripId == tripId } ?: TripModel()

                TrackOrderScreen(
                    trip = trip,
                    isDarkMode = isDarkMode,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

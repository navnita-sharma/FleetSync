package com.example.fleetsync

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*

// Define Theme Colors
val MapNavyBlue = Color(0xFF0A1160)
val MapActiveOrange = Color(0xFFFF9500)
val MapTextDark = Color(0xFF1E293B)
val MapTextMuted = Color(0xFF64748B)
val MapLightGrayBg = Color(0xFFF8F9FA)
val MapSurfaceCard = Color(0xFFF4F5F7)
val MapInTransitBlue = Color(0xFF26328C)
val MapTimelineBrown = Color(0xFF8B5A2B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetTrackingScreen(
    isDarkMode: Boolean = true,
    onDashboardClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: FleetDashboardViewModel = viewModel()
) {
    val vehicles = viewModel.vehicleList
    val selectedVehicleState = remember { mutableStateOf<VehicleModel?>(null) }
    val selectedVehicle = selectedVehicleState.value
    
    val sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    
    val mumbai = LatLng(19.0760, 72.8777)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mumbai, 10f)
    }

    // Dynamic Colors based on Theme
    val bgColor = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val surfaceColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
    val textColor = if (isDarkMode) Color.White else Color(0xFF1E293B)
    val mutedTextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val brandColor = if (isDarkMode) Color(0xFFF97316) else Color(0xFF0D1260)

    // Update camera when a vehicle is selected
    LaunchedEffect(selectedVehicle) {
        selectedVehicle?.let {
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                    LatLng(it.latitude, it.longitude), 14f
                )
            )
        }
    }

    Scaffold(
        bottomBar = {
            FleetBottomNavigation(
                isDarkMode = isDarkMode,
                onDashboardClick = onDashboardClick,
                onOrdersClick = onOrdersClick,
                onSettingsClick = onSettingsClick
            )
        },
        containerColor = bgColor
    ) { innerPadding ->
        BottomSheetScaffold(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
            scaffoldState = scaffoldState,
            sheetPeekHeight = 140.dp,
            sheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            sheetContainerColor = surfaceColor,
            sheetContentColor = textColor,
            sheetSwipeEnabled = true,
            sheetContent = {
                selectedVehicle?.let { vehicle ->
                    BottomSheetContent(vehicle = vehicle, isDarkMode = isDarkMode)
                } ?: Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Select a vehicle on map to view details", color = mutedTextColor)
                }
            },
            containerColor = bgColor
        ) { 
            Box(modifier = Modifier.fillMaxSize()) {
                // Google Map Background - Covering Full Screen
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { selectedVehicleState.value = null }
                ) {
                    vehicles.forEach { vehicle ->
                        Marker(
                            state = MarkerState(position = LatLng(vehicle.latitude, vehicle.longitude)),
                            title = vehicle.vehicleNumber,
                            snippet = "${vehicle.vehicleType} - ${vehicle.model}",
                            onClick = {
                                selectedVehicleState.value = vehicle
                                true
                            }
                        )
                    }
                }

                // Top UI Overlays - Using individual boxes to avoid blocking interaction
                TopSearchBar(
                    isDarkMode = isDarkMode, 
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )
                
                StatusPillsRow(
                    isDarkMode = isDarkMode, 
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 88.dp, start = 16.dp, end = 16.dp)
                )
            }
        }
    }
}

@Composable
fun TopSearchBar(isDarkMode: Boolean, modifier: Modifier = Modifier) {
    val surfaceColor = if (isDarkMode) Color(0xFF334155) else Color.White
    val textColor = if (isDarkMode) Color.White else Color(0xFF1E293B)
    val mutedTextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = surfaceColor.copy(alpha = 0.95f),
        shadowElevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(Icons.Outlined.Search, contentDescription = "Search", tint = mutedTextColor)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search vehicle, driver...",
                color = mutedTextColor,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            HorizontalDivider(
                color = mutedTextColor.copy(alpha = 0.3f),
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.Outlined.Tune, contentDescription = "Filter", tint = if (isDarkMode) Color(0xFFF97316) else Color(0xFF0D1260))
        }
    }
}

@Composable
fun StatusPillsRow(isDarkMode: Boolean, modifier: Modifier = Modifier) {
    var selectedStatus by remember { mutableStateOf("MOVING") }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusPill(text = "MOVING", isActive = selectedStatus == "MOVING", isDarkMode = isDarkMode, onClick = { selectedStatus = "MOVING" })
        StatusPill(text = "AT TOLL", isActive = selectedStatus == "AT TOLL", isDarkMode = isDarkMode, onClick = { selectedStatus = "AT TOLL" })
        StatusPill(text = "HALTED", isActive = selectedStatus == "HALTED", isDarkMode = isDarkMode, onClick = { selectedStatus = "HALTED" })
    }
}

@Composable
fun StatusPill(text: String, isActive: Boolean, isDarkMode: Boolean, onClick: () -> Unit) {
    val activeColor = if (isDarkMode) Color(0xFFF97316) else Color(0xFF0D1260)
    val inactiveColor = if (isDarkMode) Color(0xFF334155) else Color.White
    val textColor = if (isActive) Color.White else (if (isDarkMode) Color.White else Color(0xFF1E293B))

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (isActive) activeColor else inactiveColor,
        shadowElevation = 2.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (isActive) Color.White else Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FloatingMapCard(vehicle: VehicleModel, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFEDEFFF), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = MapNavyBlue, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(vehicle.vehicleNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MapTextDark)
                Text("In Transit", color = MapTextMuted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun BottomSheetContent(vehicle: VehicleModel, isDarkMode: Boolean, modifier: Modifier = Modifier) {
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val mutedTextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val brandColor = if (isDarkMode) Color(0xFFF97316) else Color(0xFF0D1260)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(mutedTextColor.copy(alpha = 0.5f))
                .align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Basic Info Header (License, Model)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = vehicle.vehicleNumber,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = brandColor
                )
                Text(
                    text = "${vehicle.vehicleType} • ${vehicle.model}",
                    color = mutedTextColor,
                    fontSize = 14.sp
                )
            }
            
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDarkMode) Color(0xFF2E7D32).copy(alpha = 0.2f) else Color(0xFFE8F5E9)
            ) {
                Text(
                    text = "ACTIVE",
                    color = Color(0xFF2E7D32),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Detail Grid
        Row(modifier = Modifier.fillMaxWidth()) {
            DetailItem(
                label = "Driver",
                value = "Not Assigned",
                icon = Icons.Default.Person,
                modifier = Modifier.weight(1f),
                isDarkMode = isDarkMode
            )
            DetailItem(
                label = "Fuel",
                value = vehicle.fuelType,
                icon = Icons.Default.LocalGasStation,
                modifier = Modifier.weight(1f),
                isDarkMode = isDarkMode
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            DetailItem(
                label = "Capacity",
                value = "${vehicle.capacity} Tons",
                icon = Icons.Default.Inventory,
                modifier = Modifier.weight(1f),
                isDarkMode = isDarkMode
            )
            DetailItem(
                label = "Last Update",
                value = "Just Now",
                icon = Icons.Default.Update,
                modifier = Modifier.weight(1f),
                isDarkMode = isDarkMode
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tracking Section
        Text(
            text = "LIVE TRACKING",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = mutedTextColor,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = brandColor)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Current Coordinates", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Lat: ${vehicle.latitude}, Lng: ${vehicle.longitude}", fontSize = 12.sp, color = mutedTextColor)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DetailItem(label: String, value: String, icon: ImageVector, modifier: Modifier, isDarkMode: Boolean) {
    val mutedTextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (isDarkMode) Color.White else Color.DarkGray)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = mutedTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, icon: ImageVector, iconTint: Color, title: String, value: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MapSurfaceCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, color = MapTextMuted, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = MapTextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OrderTrackingCard(vehicle: VehicleModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = MapTextMuted, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VEHICLE ID: ${vehicle.id.take(8)}", fontWeight = FontWeight.Bold, color = MapTextDark, fontSize = 14.sp)
                }
                Surface(
                    color = MapInTransitBlue,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "LIVE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Timeline Map
            TimelineItem(
                title = "Current Coordinates",
                subtitle = "Lat: ${vehicle.latitude}, Lng: ${vehicle.longitude}",
                iconBgColor = MapNavyBlue,
                icon = Icons.Default.Navigation,
                isLast = false,
                isCurrent = true
            )
            TimelineItem(
                title = "Fuel Type",
                subtitle = vehicle.fuelType,
                iconBgColor = Color(0xFFF5B041),
                icon = Icons.Default.LocalGasStation,
                isLast = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Contact Button
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MapNavyBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contact Driver", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun TimelineItem(
    title: String,
    subtitle: String,
    subtitleColor: Color = MapTextMuted,
    iconBgColor: Color,
    icon: ImageVector,
    iconTint: Color = Color.White,
    isLast: Boolean,
    isCurrent: Boolean = false
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        // Icon and Line Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .padding(vertical = 4.dp)
                        .background(if (isCurrent) Color(0xFFF5B041).copy(alpha = 0.5f) else Color(0xFFE2E8F0))
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text Content Column
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 24.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = MapTextDark, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = subtitleColor, fontSize = 12.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun FleetBottomNavigation(
    isDarkMode: Boolean,
    onDashboardClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val mutedTextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val brandColor = if (isDarkMode) Color(0xFFF97316) else Color(0xFF0D1260)
    val bgColor = if (isDarkMode) Color(0xFF1E293B) else Color.White

    NavigationBar(
        containerColor = bgColor,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("DASHBOARD", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = onDashboardClick,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = mutedTextColor,
                unselectedTextColor = mutedTextColor,
                indicatorColor = brandColor.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Orders") },
            label = { Text("ORDERS", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = onOrdersClick,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = mutedTextColor,
                unselectedTextColor = mutedTextColor,
                indicatorColor = brandColor.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Explore, contentDescription = "Map") },
            label = { Text("MAP", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            selected = true,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = brandColor,
                selectedTextColor = brandColor,
                unselectedIconColor = mutedTextColor,
                unselectedTextColor = mutedTextColor,
                indicatorColor = brandColor.copy(alpha = 0.2f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("SETTINGS", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = onSettingsClick,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = mutedTextColor,
                unselectedTextColor = mutedTextColor,
                indicatorColor = brandColor.copy(alpha = 0.1f)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFleetTrackingScreen() {
    MaterialTheme {
        FleetTrackingScreen()
    }
}

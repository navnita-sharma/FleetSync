package com.example.fleetsync

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapUiSettings

import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetOwnerDashboard(
    onSettingsClick: () -> Unit,
    onMapClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    isDarkMode: Boolean = true,
    viewModel: FleetDashboardViewModel = viewModel()
) {
    // State for Name
    var userName by remember { mutableStateOf("Loading...") }
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showActiveVehiclesSheet by remember { mutableStateOf(false) }
    var showAddDriverSheet by remember { mutableStateOf(false) }

    // Fetch user name
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userName = document.getString("name") ?: "Fleet Owner"
                    }
                }
        }
    }

    val activeVehicles = viewModel.activeVehiclesCount
    val pendingOrders  = viewModel.pendingOrdersCount
    val criticalAlerts = viewModel.criticalAlertsCount

    val bgColor = if (isDarkMode) Color(0xFF0F0F0F) else Color(0xFFF8F9FB)
    val cardBg = if (isDarkMode) Color(0xFF1A1A1A) else Color.White

    Scaffold(
        topBar = { DashboardTopBar(isDarkMode, onNotificationsClick) },
        bottomBar = { DashboardBottomNav(onSettingsClick, onMapClick, onOrdersClick, isDarkMode) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = { showAddDriverSheet = true },
                    containerColor = Color(0xFF6366F1),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Driver")
                }
                Spacer(modifier = Modifier.height(16.dp))
                FloatingActionButton(
                    onClick = { showBottomSheet = true },
                    containerColor = Color(0xFFE68A1E),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Vehicle")
                }
            }
        },
        containerColor = bgColor
    ) { padding ->
        if (showActiveVehiclesSheet) {
            ModalBottomSheet(
                onDismissRequest = { showActiveVehiclesSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
            ) {
                ActiveVehiclesListSheet(
                    vehicles = viewModel.vehicleList,
                    isDarkMode = isDarkMode
                )
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
            ) {
                AddVehicleBottomSheet(
                    onDismiss = { showBottomSheet = false },
                    onSave = { vehicle ->
                        viewModel.addVehicle(
                            vehicle = vehicle,
                            onSuccess = {
                                showBottomSheet = false
                                Toast.makeText(context, "Vehicle Added Successfully!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                )
            }
        }

        if (showAddDriverSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddDriverSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
            ) {
                AddDriverBottomSheet(
                    onDismiss = { showAddDriverSheet = false },
                    isDarkMode = isDarkMode,
                    onSave = { name, email, phone, pass ->
                        viewModel.addDriver(
                            name = name,
                            email = email,
                            phone = phone,
                            pass = pass,
                            onSuccess = {
                                showAddDriverSheet = false
                                Toast.makeText(context, "Driver Registered Successfully!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            // Stats Section
            item {
                ActiveVehiclesCard(isDarkMode, activeVehicles, onClick = { showActiveVehiclesSheet = true })
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Pending Orders",
                        value = String.format("%02d", pendingOrders),
                        icon = Icons.Default.Assignment,
                        iconBgColor = if (isDarkMode) Color(0xFF2D3748) else Color(0xFFFFEEDD),
                        iconColor = Color(0xFFE68A1E),
                        containerColor = cardBg,
                        isDarkMode = isDarkMode
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Critical Alerts",
                        value = String.format("%02d", criticalAlerts),
                        icon = Icons.Default.Warning,
                        iconBgColor = if (isDarkMode) Color(0xFF5D2516) else Color(0xFFFFE5E5),
                        iconColor = Color(0xFFD32F2F),
                        isDark = true,
                        containerColor = if (isDarkMode) Color(0xFF5D2516) else Color(0xFFD32F2F).copy(
                            alpha = 0.1f
                        ),
                        isDarkMode = isDarkMode
                    )
                }
            }

            // Fleet Tracking Section
            item {
                SectionHeader(
                    title = "Fleet Tracking",
                    actionText = "View Full Map",
                    onActionClick = onMapClick,
                    isDarkMode = isDarkMode
                )
                Spacer(modifier = Modifier.height(12.dp))
                FleetMapPlaceholder(isDarkMode)
            }
        }
    }
}

@Composable
fun DashboardTopBar(isDarkMode: Boolean, onNotificationsClick: () -> Unit = {}) {
    val textColor = if (isDarkMode) Color.White else Color(0xFF1A1F71)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDarkMode) Color(0xFF2D3748) else Color(0xFFE0E7FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Waves,
                    contentDescription = null,
                    tint = if (isDarkMode) Color.White else Color(0xFF1A1F71),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "FleetSync",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "FLEET OWNER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.Gray else Color.Gray,
                    letterSpacing = 1.sp
                )
            }
        }
        IconButton(onClick = onNotificationsClick) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ActiveVehiclesCard(isDarkMode: Boolean, count: Int, onClick: () -> Unit = {}) {
    val cardBg = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkMode) Color.White else Color(0xFF1A1F71)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkMode) Color(0xFF2D3748) else Color(0xFFE0E7FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = if (isDarkMode) Color.White else Color(0xFF1A1F71)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDarkMode) Color(0xFF2E7D32).copy(alpha = 0.2f) else Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "+12%",
                        color = Color(0xFF2E7D32),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = count.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = "Active Vehicles",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ActiveVehiclesListSheet(vehicles: List<VehicleModel>, isDarkMode: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .padding(16.dp)
    ) {
        Text(
            "Active Vehicles",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color.White else Color(0xFF1A1F71)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (vehicles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active vehicles found", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(vehicles) { vehicle ->
                    VehicleDetailCard(vehicle, isDarkMode)
                }
            }
        }
    }
}

@Composable
fun VehicleDetailCard(vehicle: VehicleModel, isDarkMode: Boolean) {
    val cardBg = if (isDarkMode) Color(0xFF2D3748) else Color(0xFFF3F4F6)
    val textColor = if (isDarkMode) Color.White else Color.Black

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vehicle.vehicleNumber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFE68A1E).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = vehicle.vehicleType,
                        color = Color(0xFFE68A1E),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = textColor.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            DetailRow("Model", vehicle.model, isDarkMode)
            DetailRow("Chassis No", vehicle.chassisNumber, isDarkMode)
            DetailRow("Fuel Type", vehicle.fuelType, isDarkMode)
            DetailRow("Capacity", "${vehicle.capacity} Tons", isDarkMode)
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpiryBadge("Insurance", vehicle.insuranceExpiry, isDarkMode, Modifier.weight(1f))
                ExpiryBadge("PUC", vehicle.pucExpiry, isDarkMode, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isDarkMode: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, color = if (isDarkMode) Color.White else Color.Black, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ExpiryBadge(label: String, date: String, isDarkMode: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isDarkMode) Color(0xFF1A1A1A) else Color.White)
            .padding(8.dp)
    ) {
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Text(date, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else Color.Black)
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    isDark: Boolean = false,
    containerColor: Color = Color.White,
    isDarkMode: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDarkMode && !isDark) Color(0xFF2D3748) else iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isDark || isDarkMode) Color.White else iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark || isDarkMode) Color.White else Color.Black
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = if (isDark || isDarkMode) Color.White.copy(alpha = 0.7f) else Color.Gray
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String? = null, onActionClick: () -> Unit = {}, showFilter: Boolean = false, isDarkMode: Boolean) {
    val textColor = if (isDarkMode) Color.White else Color(0xFF1A1F71)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        if (actionText != null) {
            Text(
                text = actionText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFE68A1E) else Color(0xFF4527A0),
                modifier = Modifier.clickable { onActionClick() }
            )
        }
        if (showFilter) {
            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.Gray)
        }
    }
}

@Composable
fun FleetMapPlaceholder(isDarkMode: Boolean) {
    val mumbai = LatLng(19.0760, 72.8777)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mumbai, 11f)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDarkMode) Color(0xFF1A1A1A) else Color.LightGray)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                scrollGesturesEnabled = false,
                zoomGesturesEnabled = false,
                tiltGesturesEnabled = false,
                rotationGesturesEnabled = false
            )
        ) {
            Marker(
                state = MarkerState(position = mumbai),
                title = "Fleet Center"
            )
        }
        
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = if (isDarkMode) Color(0xFF1A1A1A) else Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE68A1E))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "38 LIVE PINGS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun OrderItem(order: OrderData, isDarkMode: Boolean) {
    val cardBg = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDarkMode) Color(0xFF2D3748) else Color(0xFFF0F2F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory, contentDescription = null, tint = if (isDarkMode) Color.White else Color(0xFF1A1F71))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.id,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textColor
                )
                Text(
                    text = "${order.vehicle} • ${order.driver}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (order.status == "IN-TRANSIT") {
                        if (isDarkMode) Color(0xFFE68A1E).copy(alpha = 0.2f) else Color(0xFFFFF3E0)
                    } else {
                        if (isDarkMode) Color(0xFF2E7D32).copy(alpha = 0.2f) else Color(0xFFE8F5E9)
                    }
                ) {
                    Text(
                        text = order.status,
                        color = if (order.status == "IN-TRANSIT") Color(0xFFE68A1E) else Color(0xFF2E7D32),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ETA: ${order.eta}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun CreateDispatchButton(isDarkMode: Boolean) {
    OutlinedButton(
        onClick = { },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.Gray),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Dispatch", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DashboardBottomNav(
    onSettingsClick: () -> Unit,
    onMapClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    isDarkMode: Boolean
) {
    val bgColor = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
    val brandColor = Color(0xFFE68A1E)
    val unselectedColor = if (isDarkMode) Color.Gray else Color.Gray

    NavigationBar(
        containerColor = bgColor,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
            label = { Text("DASHBOARD") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = brandColor,
                selectedTextColor = brandColor,
                indicatorColor = brandColor.copy(alpha = 0.1f),
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onOrdersClick,
            icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
            label = { Text("ORDERS") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onMapClick,
            icon = { Icon(Icons.Default.Explore, contentDescription = null) },
            label = { Text("MAP") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onSettingsClick,
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("SETTINGS") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            )
        )
    }
}

data class OrderData(
    val id: String,
    val vehicle: String,
    val driver: String,
    val status: String,
    val eta: String
)

val activeOrders = listOf(
    OrderData("#ORD-8829", "HR-26-CZ-1092", "Rajesh K.", "IN-TRANSIT", "14:30"),
    OrderData("#ORD-8831", "DL-01-BK-4456", "Amrit S.", "LOADING", "Ready in 15m"),
    OrderData("#ORD-8835", "MH-12-PQ-9001", "Vijay M.", "IN-TRANSIT", "19:15")
)

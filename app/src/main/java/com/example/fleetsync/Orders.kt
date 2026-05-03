package com.example.fleetsync

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast

data class Order(
    val id: String = "",
    val vehicle: String = "",
    val driver: String = "",
    val from: String = "",
    val to: String = "",
    val status: String = "",
    val eta: String = "",
    val date: String = "",
    val ownerUid: String = "",
    val estimatedTime: String = "",
    val tollCount: Int = 0
)

val sampleOrders = listOf(
    Order("#ORD-8829", "HR-26-CZ-1092", "Rajesh Kumar", "Mumbai WH", "Pune Hub", "IN-TRANSIT", "18:45", "Oct 24"),
    Order("#ORD-8831", "DL-01-BK-4456", "Amrit Singh", "Delhi DC-3", "Jaipur", "LOADING", "Ready in 15m", "Oct 24"),
    Order("#ORD-8835", "MH-12-PQ-9001", "Vijay Mehta", "Nagpur DC", "Hyderabad", "IN-TRANSIT", "19:15", "Oct 24"),
    Order("#ORD-8840", "GJ-05-AB-2234", "Suresh Patel", "Surat WH", "Ahmedabad", "DELIVERED", "Done", "Oct 23"),
    Order("#ORD-8844", "KA-09-XZ-5567", "Mohan Rao", "Bangalore", "Chennai DC", "PENDING", "Tomorrow", "Oct 25"),
    Order("#ORD-8851", "TN-07-CD-8890", "Arjun Das", "Chennai Hub", "Coimbatore", "IN-TRANSIT", "22:00", "Oct 24"),
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    isDarkMode: Boolean = false,
    onBackClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onTrackClick: (String) -> Unit = {},
    fleetViewModel: FleetDashboardViewModel = viewModel()
) {
    // Use live trips from FleetDashboardViewModel for real-time status updates
    val liveTrips   = fleetViewModel.liveTrips
    val isLoading   = fleetViewModel.isLoadingTrips

    val bgColor      = if (isDarkMode) Color(0xFF0F0F0F) else Color(0xFFF8F9FB)
    val cardBg       = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
    val textColor    = if (isDarkMode) Color.White else Color(0xFF1A1F71)
    val accentOrange = Color(0xFFE68A1E)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tab_all),
        stringResource(R.string.tab_in_transit),
        stringResource(R.string.tab_pending),
        stringResource(R.string.tab_delivered)
    )

    var showCreateOrderSheet by remember { mutableStateOf(false) }
    // Edit sheet state
    var tripToEdit by remember { mutableStateOf<TripModel?>(null) }
    // Delete confirm dialog
    var tripToDelete by remember { mutableStateOf<TripModel?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.orders_title),
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = bgColor)
            )
        },
        bottomBar = { 
            OrdersBottomNav(
                onDashboardClick = onBackClick,
                onMapClick = onMapClick,
                onSettingsClick = onSettingsClick,
                isDarkMode = isDarkMode
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateOrderSheet = true },
                containerColor = accentOrange,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Order")
            }
        },
        containerColor = bgColor
    ) { padding ->

        // ── Create Order Sheet ────────────────────────────────────────────────
        if (showCreateOrderSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCreateOrderSheet = false },
                sheetState       = sheetState,
                containerColor   = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
            ) {
                CreateOrderBottomSheet(
                    onDismiss      = { showCreateOrderSheet = false },
                    fleetViewModel = fleetViewModel,
                    isDarkMode     = isDarkMode
                )
            }
        }

        // ── Edit Trip Sheet ───────────────────────────────────────────────────
        tripToEdit?.let { trip ->
            ModalBottomSheet(
                onDismissRequest = { tripToEdit = null },
                sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor   = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
            ) {
                EditTripBottomSheet(
                    trip           = trip,
                    fleetViewModel = fleetViewModel,
                    isDarkMode     = isDarkMode,
                    onDismiss      = { tripToEdit = null }
                )
            }
        }

        // ── Delete Confirm Dialog ─────────────────────────────────────────────
        tripToDelete?.let { trip ->
            AlertDialog(
                onDismissRequest = { tripToDelete = null },
                icon  = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFD32F2F)) },
                title = { Text("Delete Order?", fontWeight = FontWeight.ExtraBold) },
                text  = { Text("This will permanently remove the trip ${trip.origin} → ${trip.destination}. This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            fleetViewModel.deleteTrip(trip.tripId,
                                onSuccess = { tripToDelete = null
                                    Toast.makeText(context, "Order deleted", Toast.LENGTH_SHORT).show() },
                                onError   = { err -> Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { tripToDelete = null }) { Text("Cancel") }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Summary Cards (live counts) ───────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OrderSummaryCard(Modifier.weight(1f), "Total",
                    String.format("%02d", liveTrips.size), Color(0xFF6366F1), cardBg)
                OrderSummaryCard(Modifier.weight(1f), "In-Transit",
                    String.format("%02d", liveTrips.count { it.status == "In Transit" }), Color(0xFFE68A1E), cardBg)
                OrderSummaryCard(Modifier.weight(1f), "Pending",
                    String.format("%02d", liveTrips.count { it.status == "Pending" }), Color(0xFFD32F2F), cardBg)
            }

            // ── Filter Tabs ───────────────────────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor   = bgColor,
                contentColor     = accentOrange,
                edgePadding      = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color    = accentOrange
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected               = selectedTab == index,
                        onClick                = { selectedTab = index },
                        selectedContentColor   = accentOrange,
                        unselectedContentColor = Color.Gray,
                        text = {
                            Text(title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize   = 13.sp)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = accentOrange)
                    }
                }
                liveTrips.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory2, contentDescription = null,
                                tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No orders yet", color = Color.Gray, fontSize = 15.sp)
                        }
                    }
                }
                else -> {
                    val filtered = when (selectedTab) {
                        1    -> liveTrips.filter { it.status == "In Transit" }
                        2    -> liveTrips.filter { it.status == "Pending" }
                        3    -> liveTrips.filter { it.status == "Delivered" }
                        else -> liveTrips
                    }
                    LazyColumn(
                        modifier        = Modifier.fillMaxSize(),
                        contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered, key = { it.tripId }) { trip ->
                            LiveTripCard(
                                trip       = trip,
                                cardBg     = cardBg,
                                isDarkMode = isDarkMode,
                                onEdit     = { tripToEdit   = trip },
                                onDelete   = { tripToDelete = trip },
                                onTrack    = { onTrackClick(trip.tripId) }
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

// ─── LiveTripCard — real-time card for Fleet Owner's Orders screen ────────────
@Composable
fun LiveTripCard(
    trip: TripModel,
    cardBg: Color,
    isDarkMode: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTrack: () -> Unit
) {
    val statusColor = when (trip.status) {
        "In Transit" -> Color(0xFFE68A1E)
        "Delivered"  -> Color(0xFF2E7D32)
        else         -> Color(0xFF6366F1)   // Pending = indigo
    }
    val textColor = if (isDarkMode) Color.White else Color(0xFF1E293B)
    val isPending = trip.status == "Pending"

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onTrack() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Top row: truck icon + driver name + status badge + actions
            Row(
                modifier             = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment    = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalShipping, null, tint = statusColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            trip.assignedDriverName.ifBlank { "No Driver" },
                            fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = textColor
                        )
                        Text(trip.vehicleNumber.ifBlank { "No Vehicle" }, fontSize = 11.sp, color = Color.Gray)
                    }
                }
                // Status badge
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.12f)) {
                    Text(
                        trip.status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                // Edit + Delete (only for Pending)
                if (isPending) {
                    Spacer(Modifier.width(6.dp))
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
            Spacer(Modifier.height(12.dp))

            // Route row
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(Color(0xFF2E7D32), CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(trip.origin, fontSize = 13.sp, color = textColor, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowForward, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Text(trip.destination, fontSize = 13.sp, color = textColor, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(8.dp).background(Color(0xFFD32F2F), CircleShape))
            }
            
            if (trip.status != "Pending") {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onTrack,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Route, null, tint = statusColor, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Track Status", fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Edit Trip Bottom Sheet ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTripBottomSheet(
    trip: TripModel,
    fleetViewModel: FleetDashboardViewModel,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    var origin      by remember { mutableStateOf(trip.origin) }
    var destination by remember { mutableStateOf(trip.destination) }

    // Driver dropdown
    var driverExpanded    by remember { mutableStateOf(false) }
    var selectedDriverName by remember { mutableStateOf(trip.assignedDriverName) }
    var selectedDriverUid  by remember { mutableStateOf(trip.assignedDriverUid) }

    // Vehicle dropdown
    var vehicleExpanded   by remember { mutableStateOf(false) }
    var selectedVehicle   by remember { mutableStateOf(trip.vehicleNumber) }

    val drivers  = fleetViewModel.availableDriverPairs
    val vehicles = fleetViewModel.availableVehicles
    val context  = LocalContext.current
    val isSaving = fleetViewModel.isSavingOrder

    val textColor    = if (isDarkMode) Color.White else Color(0xFF1A1F71)
    val fieldBg      = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFF1F5F9)
    val accentOrange = Color(0xFFE68A1E)

    LaunchedEffect(Unit) {
        fleetViewModel.fetchDrivers()
        fleetViewModel.fetchVehicles()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Edit Order", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
        Text("Update trip details below", fontSize = 13.sp, color = Color.Gray)
        Spacer(Modifier.height(20.dp))

        // Driver dropdown
        Text("DRIVER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded         = driverExpanded,
            onExpandedChange = { driverExpanded = it }
        ) {
            OutlinedTextField(
                value          = selectedDriverName.ifBlank { "Select Driver" },
                onValueChange  = {},
                readOnly       = true,
                trailingIcon   = { ExposedDropdownMenuDefaults.TrailingIcon(driverExpanded) },
                modifier       = Modifier.fillMaxWidth().menuAnchor(),
                shape          = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = driverExpanded, onDismissRequest = { driverExpanded = false }) {
                drivers.forEach { (name, uid) ->
                    DropdownMenuItem(
                        text    = { Text(name) },
                        onClick = { selectedDriverName = name; selectedDriverUid = uid; driverExpanded = false }
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Vehicle dropdown
        Text("VEHICLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded         = vehicleExpanded,
            onExpandedChange = { vehicleExpanded = it }
        ) {
            OutlinedTextField(
                value         = selectedVehicle.ifBlank { "Select Vehicle" },
                onValueChange = {},
                readOnly      = true,
                trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(vehicleExpanded) },
                modifier      = Modifier.fillMaxWidth().menuAnchor(),
                shape         = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = vehicleExpanded, onDismissRequest = { vehicleExpanded = false }) {
                vehicles.forEach { v ->
                    DropdownMenuItem(text = { Text(v) }, onClick = { selectedVehicle = v; vehicleExpanded = false })
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Origin
        Text("SOURCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value         = origin,
            onValueChange = { origin = it },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            singleLine    = true,
            placeholder   = { Text("City / Warehouse") }
        )

        Spacer(Modifier.height(14.dp))

        // Destination
        Text("DESTINATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value         = destination,
            onValueChange = { destination = it },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(12.dp),
            singleLine    = true,
            placeholder   = { Text("City / Hub") }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                fleetViewModel.updateTrip(
                    tripId        = trip.tripId,
                    driverName    = selectedDriverName,
                    driverUid     = selectedDriverUid,
                    vehicleNumber = selectedVehicle,
                    origin        = origin,
                    destination   = destination,
                    onSuccess     = {
                        Toast.makeText(context, "Order updated!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            },
            enabled  = !isSaving,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = accentOrange)
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun OrderSummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    count: String,
    color: Color,
    bgColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun OrderCard(order: Order, cardBg: Color, isDarkMode: Boolean) {
    val statusColor = when (order.status) {
        "IN-TRANSIT" -> Color(0xFFE68A1E)
        "DELIVERED"  -> Color(0xFF2E7D32)
        "LOADING"    -> Color(0xFF1565C0)
        "PENDING"    -> Color(0xFFD32F2F)
        else         -> Color.Gray
    }
    val statusBg = statusColor.copy(alpha = 0.12f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row: ID + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDarkMode) Color(0xFF2D3748) else Color(0xFFEEF2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = Color(0xFF3B4ED8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            order.id,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = if (isDarkMode) Color.White else Color(0xFF1A1F71)
                        )
                        Text(order.date, fontSize = 11.sp, color = Color.Gray)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = statusBg) {
                    Text(
                        order.status,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(14.dp))

            // Route
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(Color(0xFF2E7D32), CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(order.from, fontSize = 13.sp, color = if (isDarkMode) Color.White else Color(0xFF1E293B), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.weight(1f))
                Text(order.to, fontSize = 13.sp, color = if (isDarkMode) Color.White else Color(0xFF1E293B), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFD32F2F), CircleShape))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Driver + Vehicle + Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(order.driver, fontSize = 12.sp, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(order.vehicle, fontSize = 12.sp, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFE68A1E), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(order.estimatedTime.ifBlank { order.eta }, fontSize = 12.sp, color = Color(0xFFE68A1E), fontWeight = FontWeight.Bold)
                }
                if (order.tollCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddRoad, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${order.tollCount} Tolls", fontSize = 12.sp, color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderBottomSheet(
    onDismiss: () -> Unit,
    fleetViewModel: FleetDashboardViewModel = viewModel(),
    isDarkMode: Boolean
) {
    // ── Local UI state ────────────────────────────────────────────────────────
    var source           by remember { mutableStateOf("") }
    var destination      by remember { mutableStateOf("") }
    var selectedVehicle  by remember { mutableStateOf("") }
    var selectedDriver   by remember { mutableStateOf("") }
    var selectedDriverUid by remember { mutableStateOf("") }  // kept for trip creation
    var vehicleExpanded  by remember { mutableStateOf(false) }
    var driverExpanded   by remember { mutableStateOf(false) }

    // ── Fetch data when the sheet opens ───────────────────────────────────────
    LaunchedEffect(Unit) {
        fleetViewModel.fetchDrivers()
        fleetViewModel.fetchVehicles()
    }

    val driverPairs       = fleetViewModel.availableDriverPairs
    val availableVehicles = fleetViewModel.availableVehicles
    val isSaving          = fleetViewModel.isSavingOrder
    val context           = LocalContext.current

    val textColor   = if (isDarkMode) Color.White else Color(0xFF1A1F71)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor   = Color(0xFFE68A1E),
        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
        focusedLabelColor    = Color(0xFFE68A1E),
        cursorColor          = Color(0xFFE68A1E)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE68A1E).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = Color(0xFFE68A1E),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Create New Order",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
                Text(
                    "Fill in the dispatch details below",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Select Driver ─────────────────────────────────────────────────────
        Text(
            "DRIVER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = driverExpanded,
            onExpandedChange = { driverExpanded = !driverExpanded }
        ) {
            OutlinedTextField(
                value = selectedDriver.ifBlank { if (driverPairs.isEmpty()) "Fetching drivers…" else "Select Driver" },
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = driverExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )
            ExposedDropdownMenu(
                expanded = driverExpanded,
                onDismissRequest = { driverExpanded = false }
            ) {
                if (driverPairs.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No drivers found — add one first", color = Color.Gray) },
                        onClick = { driverExpanded = false }
                    )
                } else {
                    driverPairs.forEach { (name, uid) ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFFE68A1E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(name, fontWeight = FontWeight.Medium)
                                }
                            },
                            onClick = {
                                selectedDriver    = name
                                selectedDriverUid = uid
                                driverExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Select Vehicle ────────────────────────────────────────────────────
        Text(
            "VEHICLE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = vehicleExpanded,
            onExpandedChange = { vehicleExpanded = !vehicleExpanded }
        ) {
            OutlinedTextField(
                value = selectedVehicle.ifBlank { if (availableVehicles.isEmpty()) "Fetching vehicles…" else "Select Vehicle" },
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = fieldColors
            )
            ExposedDropdownMenu(
                expanded = vehicleExpanded,
                onDismissRequest = { vehicleExpanded = false }
            ) {
                if (availableVehicles.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No vehicles found — add one first", color = Color.Gray) },
                        onClick = { vehicleExpanded = false }
                    )
                } else {
                    availableVehicles.forEach { num ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = Color(0xFFE68A1E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(num, fontWeight = FontWeight.Medium)
                                }
                            },
                            onClick = {
                                selectedVehicle = num
                                vehicleExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Source ────────────────────────────────────────────────────────────
        Text("SOURCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = source,
            onValueChange = { source = it },
            placeholder = { Text("e.g. Mumbai Warehouse", color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors
        )

        Spacer(Modifier.height(16.dp))

        // ── Destination ───────────────────────────────────────────────────────
        Text("DESTINATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = destination,
            onValueChange = { destination = it },
            placeholder = { Text("e.g. Pune Delivery Hub", color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors
        )

        Spacer(Modifier.height(28.dp))

        // ── Save Button ───────────────────────────────────────────────────────
        Button(
            onClick = {
                fleetViewModel.createTrip(
                    driverName    = selectedDriver,
                    driverUid     = selectedDriverUid,
                    vehicleNumber = selectedVehicle,
                    origin        = source,
                    destination   = destination,
                    onSuccess = {
                        onDismiss()
                        Toast.makeText(context, "Trip Created Successfully! 🚚", Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE68A1E)),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color    = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Order", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun OrdersBottomNav(
    onDashboardClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    isDarkMode: Boolean = false
) {
    val bgColor = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
    val unselectedColor = Color.Gray
    val brandColor = Color(0xFFE68A1E)

    NavigationBar(containerColor = bgColor, tonalElevation = 8.dp) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("DASHBOARD", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = onDashboardClick,
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = unselectedColor, unselectedTextColor = unselectedColor)
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.LocalShipping, contentDescription = "Orders") },
            label = { Text("ORDERS", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            selected = true,
            onClick = { },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = brandColor,
                selectedTextColor = brandColor,
                indicatorColor = brandColor.copy(alpha = 0.15f)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Explore, contentDescription = "Map") },
            label = { Text("MAP", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = onMapClick,
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = unselectedColor, unselectedTextColor = unselectedColor)
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("SETTINGS", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            selected = false,
            onClick = onSettingsClick,
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = unselectedColor, unselectedTextColor = unselectedColor)
        )
    }
}

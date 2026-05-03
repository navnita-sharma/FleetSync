package com.example.fleetsync

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fleetsync.ui.theme.FleetSyncTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Color tokens ─────────────────────────────────────────────────────────────
private val DriverNavy   = Color(0xFF1A1F71)
private val DriverOrange = Color(0xFFE68A1E)
private val DriverGreen  = Color(0xFF16A34A)
private val DriverRed    = Color(0xFFC53030)
private val DriverGray   = Color(0xFF64748B)

@Composable
fun DriverDashboard(
    isDarkMode: Boolean = false,
    onSettingsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    viewModel: DriverDashboardViewModel = viewModel()
) {
    val bgColor    = if (isDarkMode) Color(0xFF0F0F0F) else Color(0xFFF8F9FB)
    val cardBg     = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
    val darkBlue   = if (isDarkMode) Color(0xFF818CF8) else DriverNavy
    val alertRed   = DriverRed
    val alertBg    = if (isDarkMode) Color(0xFF451A1A) else Color(0xFFFFF5F5)
    val textGray   = if (isDarkMode) Color.LightGray else DriverGray

    val driverName    = viewModel.driverName
    val companyName   = viewModel.companyName
    val driverStatus  = viewModel.driverStatus
    val assignedTrips = viewModel.assignedTrips
    val isLoading     = viewModel.isLoadingTrips

    Scaffold(containerColor = bgColor) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            // ── 1. Top Bar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DriverOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = DriverOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("FleetSync", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = darkBlue)
                        if (companyName.isNotBlank()) {
                            Text(companyName, fontSize = 12.sp, color = textGray)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (driverStatus == "On Trip") DriverOrange.copy(alpha = 0.15f)
                                else DriverGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            driverStatus,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (driverStatus == "On Trip") DriverOrange else DriverGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = darkBlue)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = darkBlue)
                    }
                }
            }

            // ── 2. Greeting ─────────────────────────────────────────────────
            Text(
                "Hello, $driverName 👋",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = darkBlue
            )
            Text(
                if (assignedTrips.isEmpty()) "No pending trips right now."
                else "${assignedTrips.size} trip${if (assignedTrips.size > 1) "s" else ""} assigned to you",
                fontSize = 14.sp,
                color = textGray,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── 3. Voice alert bar ─────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = darkBlue
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 4.dp, top = 4.dp))
                        Box(modifier = Modifier.size(8.dp).background(DriverOrange, CircleShape))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("RECENT VOICE ALERT", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Route deviation detected ahead.", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Replay", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 4. Assigned Trips ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("MY TRIPS", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = textGray, letterSpacing = 1.sp)
                if (!isLoading) {
                    Surface(color = DriverOrange.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)) {
                        Text(
                            "${assignedTrips.size} Active",
                            color = DriverOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = DriverOrange)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Loading your trips…", color = textGray, fontSize = 13.sp)
                        }
                    }
                }
                assignedTrips.isEmpty() -> {
                    EmptyTripsCard(isDarkMode = isDarkMode, cardBg = cardBg)
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(assignedTrips, key = { it.tripId }) { trip ->
                            val context = LocalContext.current
                            TripCard(
                                trip            = trip,
                                cardBg          = cardBg,
                                isDarkMode      = isDarkMode,
                                onMarkInTransit = { 
                                    viewModel.markInTransit(trip.tripId)
                                    val intent = Intent(context, LocationTrackingService::class.java).apply {
                                        putExtra(LocationTrackingService.EXTRA_VEHICLE_ID, trip.vehicleNumber)
                                    }
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        context.startForegroundService(intent)
                                    } else {
                                        context.startService(intent)
                                    }
                                },
                                onMarkDelivered = { 
                                    viewModel.markDelivered(trip.tripId)
                                    val intent = Intent(context, LocationTrackingService::class.java)
                                    context.stopService(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Trip Card ────────────────────────────────────────────────────────────────
@Composable
fun TripCard(
    trip: TripModel,
    cardBg: Color,
    isDarkMode: Boolean,
    onMarkInTransit: () -> Unit,
    onMarkDelivered: () -> Unit
) {
    val context     = LocalContext.current
    val statusColor = when (trip.status) {
        "In Transit" -> DriverOrange
        "Delivered"  -> DriverGreen
        else         -> Color(0xFF6366F1)   // Pending = indigo
    }
    val textColor = if (isDarkMode) Color.White else Color(0xFF1E293B)
    val dateStr   = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(trip.timestamp))

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // Top row: Trip ID + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            trip.tripId.take(12).uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = textColor
                        )
                        Text(dateStr, fontSize = 11.sp, color = DriverGray)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.12f)) {
                    Text(
                        trip.status,
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

            // Route row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(8.dp).background(DriverGreen, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    trip.origin,
                    fontSize = 13.sp,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = DriverGray, modifier = Modifier.size(16.dp))
                Text(
                    trip.destination,
                    fontSize = 13.sp,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).background(DriverRed, CircleShape))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle info row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = DriverGray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(trip.vehicleNumber.ifBlank { "Vehicle not assigned" }, fontSize = 12.sp, color = DriverGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            when (trip.status) {
                "Pending" -> {
                    Button(
                        onClick = onMarkInTransit,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DriverNavy)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Trip", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                "In Transit" -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                // Open Google Maps with directions from origin to destination
                                val origin      = Uri.encode(trip.origin)
                                val destination = Uri.encode(trip.destination)
                                val url = "https://www.google.com/maps/dir/?api=1" +
                                          "&origin=$origin&destination=$destination&travelmode=driving"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                intent.setPackage("com.google.android.apps.maps")
                                // Fallback to browser if Maps app not installed
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape    = RoundedCornerShape(12.dp),
                            border   = BorderStroke(1.dp, DriverOrange)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = DriverOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Map", color = DriverOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Button(
                            onClick  = onMarkDelivered,
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = DriverGreen)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delivered", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// ─── Empty state card ─────────────────────────────────────────────────────────
@Composable
fun EmptyTripsCard(isDarkMode: Boolean, cardBg: Color) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(DriverGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DriverGreen, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("All Clear!", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = if (isDarkMode) Color.White else DriverNavy)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "No pending or active trips.\nYour fleet manager will assign your next trip shortly.",
                fontSize = 13.sp,
                color = DriverGray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

// ─── Legacy helpers kept for backward compatibility ────────────────────────────
@Composable
fun ReasonItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.height(72.dp), color = Color(0xFFF1F5F9), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, color = Color(0xFF475569))
        }
    }
}

@Composable
fun TaskPoint(label: String, address: String, icon: @Composable () -> Unit, showLine: Boolean) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            if (showLine) {
                Box(modifier = Modifier.width(2.dp).weight(1f).padding(vertical = 4.dp).background(Color(0xFFE2E8F0)))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.padding(bottom = if (showLine) 24.dp else 0.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(2.dp))
            Text(address, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B), lineHeight = 20.sp)
        }
    }
}

@Composable
fun StatusCard(label: String, value: String, unit: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color(0xFF1E293B), modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.width(4.dp))
                Text(unit, fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 3.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DriverDashboardScreenPreview() {
    FleetSyncTheme {
        DriverDashboard()
    }
}

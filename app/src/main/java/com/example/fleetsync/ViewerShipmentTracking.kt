package com.example.fleetsync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientMapTrackingScreen(
    tripId: String,
    onBackClick: () -> Unit,
    viewModel: CustomerTrackingViewModel = viewModel()
) {
    val trip = viewModel.currentTrip
    val isInvalid = viewModel.isTripInvalid

    // Theme Colors
    val navyBlue = Color(0xFF1B1B54)
    val accentOrange = Color(0xFFE68A1E)
    val cardBg = Color.White

    LaunchedEffect(tripId) {
        viewModel.observeTrip(tripId)
    }

    if (isInvalid) {
        // Handle session expiration
        return
    }

    if (trip == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = accentOrange)
        }
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(trip.latitude, trip.longitude), 12f)
    }

    // Update camera when coordinates change
    LaunchedEffect(trip.latitude, trip.longitude) {
        cameraPositionState.animate(
            com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                LatLng(trip.latitude, trip.longitude), 14f
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Shipment #${trip.tripId}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = navyBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FB))
        ) {
            // Live Map Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    Marker(
                        state = MarkerState(position = LatLng(trip.latitude, trip.longitude)),
                        title = trip.vehicleNumber,
                        snippet = "Current Location"
                    )
                }
                
                // Status Badge on Map
                Surface(
                    modifier = Modifier.padding(16.dp).align(Alignment.TopEnd),
                    shape = RoundedCornerShape(8.dp),
                    color = accentOrange
                ) {
                    Text(
                        trip.status.uppercase(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Info Section
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // ETA, Tolls, Distance Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoCardSmall(Modifier.weight(1f), "Tolls", "${trip.passedTolls}/${trip.totalTolls}", Color(0xFF6366F1))
                    InfoCardSmall(Modifier.weight(1f), "ETA", trip.eta.ifBlank { "--" }, Color(0xFF10B981))
                    InfoCardSmall(Modifier.weight(1.1f), "Distance", "${trip.distanceKm} km", accentOrange)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Vehicle & Driver Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(50.dp).background(accentOrange.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalShipping, null, tint = accentOrange, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(trip.vehicleNumber, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Text("In Transit", fontSize = 13.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(trip.assignedDriverName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(trip.assignedDriverPhone, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Transit Journey Section
                Text(
                    "TRANSIT JOURNEY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        JourneyPoint(trip.origin, "Source", true, trip.timestamp)
                        Box(modifier = Modifier.width(2.dp).height(30.dp).padding(start = 11.dp).background(Color.LightGray))
                        JourneyPoint(trip.destination, "Destination", false)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Contact Actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { /* Call Driver */ },
                        modifier = Modifier.weight(1f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = navyBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Call Driver", fontWeight = FontWeight.Bold)
                    }
                    
                    OutlinedButton(
                        onClick = { /* Support */ },
                        modifier = Modifier.weight(1f).height(54.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.HeadsetMic, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Support")
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun InfoCardSmall(modifier: Modifier, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun JourneyPoint(name: String, type: String, isReached: Boolean, timestamp: Long = 0) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(24.dp).background(if (isReached) Color(0xFFE68A1E) else Color.LightGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isReached) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(type, fontSize = 12.sp, color = Color.Gray)
            if (timestamp > 0) {
                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                Text(sdf.format(Date(timestamp)), fontSize = 11.sp, color = Color(0xFFE68A1E))
            }
        }
    }
}

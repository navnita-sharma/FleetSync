package com.example.fleetsync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import com.example.fleetsync.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOrderScreen(
    trip: TripModel,
    isDarkMode: Boolean,
    onBackClick: () -> Unit
) {
    val bgColor = if (isDarkMode) Color(0xFF0F0F0F) else Color(0xFFF8F9FB)
    val cardBg = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkMode) Color.White else Color(0xFF1A1F71)
    val accentOrange = Color(0xFFE68A1E)

    // Build the timeline points: Source -> Tolls -> Destination
    val timelinePoints = remember(trip) {
        mutableListOf<TimelinePoint>().apply {
            add(TimelinePoint(trip.origin, "Source", true, trip.timestamp))
            trip.routeTolls.forEachIndexed { index, toll ->
                add(TimelinePoint(toll, "Toll Plaza", index <= trip.currentTollIndex))
            }
            add(TimelinePoint(trip.destination, "Destination", trip.status == "Delivered"))
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Track Shipment", fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Toll Progress Card
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoCard(Modifier.weight(1f), "Tolls Crossed", "${trip.passedTolls}/${trip.totalTolls}", Color(0xFF6366F1), cardBg, isDarkMode)
                InfoCard(Modifier.weight(1f), "ETA", trip.eta.ifBlank { "Calculating..." }, Color(0xFF10B981), cardBg, isDarkMode)
                InfoCard(Modifier.weight(1.1f), "Distance", trip.distanceKm.toString(), Color(0xFFE68A1E), cardBg, isDarkMode, unit = "km")
            }

            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(52.dp).background(accentOrange.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalShipping, null, tint = accentOrange, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(trip.vehicleNumber, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = textColor)
                        Text(trip.status, fontSize = 14.sp, color = accentOrange, fontWeight = FontWeight.Bold)
                    }
                    
                    // Driver Info (on the right)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(trip.assignedDriverName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
                        if (trip.assignedDriverPhone.isNotBlank()) {
                            Text(trip.assignedDriverPhone, fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Live Location / Map Placeholder
            if (trip.status == "In Transit") {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("LIVE LOCATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Coordinates: ${trip.latitude}, ${trip.longitude}",
                            fontSize = 14.sp,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                        if (trip.lastUpdated > 0) {
                            Text(
                                "Last updated: ${SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date(trip.lastUpdated))}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Text("TRACKING TIMELINE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(timelinePoints) { index, point ->
                    TimelineItem(
                        point = point,
                        isLast = index == timelinePoints.size - 1,
                        isDarkMode = isDarkMode,
                        accentColor = if (point.isReached) accentOrange else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    modifier: Modifier, 
    label: String, 
    value: String, 
    color: Color, 
    cardBg: Color, 
    isDarkMode: Boolean,
    unit: String = ""
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp), 
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
            if (unit.isNotBlank()) {
                Text(unit, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
            }
            Text(label, fontSize = 11.sp, color = if (isDarkMode) Color.Gray else Color.DarkGray, fontWeight = FontWeight.Bold)
        }
    }
}

data class TimelinePoint(
    val name: String,
    val type: String,
    val isReached: Boolean,
    val timestamp: Long = 0L
)

@Composable
fun TimelineItem(
    point: TimelinePoint,
    isLast: Boolean,
    isDarkMode: Boolean,
    accentColor: Color
) {
    val textColor = if (isDarkMode) Color.White else Color.Black
    val lineColor = if (point.isReached) accentColor else Color.LightGray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Vertical Line & Dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(if (point.isReached) accentColor else Color.LightGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (point.isReached) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(lineColor)
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        // Content
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(point.name, fontWeight = FontWeight.Bold, color = if (point.isReached) textColor else Color.Gray)
            Text(point.type, fontSize = 12.sp, color = Color.Gray)
            if (point.timestamp > 0) {
                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                Text(sdf.format(Date(point.timestamp)), fontSize = 11.sp, color = accentColor)
            }
        }
    }
}

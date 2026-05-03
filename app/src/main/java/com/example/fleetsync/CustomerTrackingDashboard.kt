package com.example.fleetsync

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomerTrackingDashboard(
    onLogout: () -> Unit
) {
    val navyCore = Color(0xFF14145C)
    val backgroundGray = Color(0xFFF4F4F8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGray)
    ) {
        // Top App Bar
        Surface(
            color = navyCore,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Client Portal", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Track your active shipments", color = Color.LightGray, fontSize = 14.sp)
                }

                // Logout Button
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List of Active Shipments
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "ACTIVE ORDERS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Dummy Order 1
            item {
                ShipmentCard(
                    orderId = "ORD-8829-XYZ",
                    status = "In Transit",
                    destination = "Indore, MP",
                    eta = "Today, 4:30 PM",
                    onClick = { /* TODO: Open Live Map */ }
                )
            }

            // Dummy Order 2
            item {
                ShipmentCard(
                    orderId = "ORD-4410-ABC",
                    status = "Out for Delivery",
                    destination = "Bhopal, MP",
                    eta = "Today, 6:00 PM",
                    onClick = { /* TODO: Open Live Map */ }
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun ShipmentCard(
    orderId: String,
    status: String,
    destination: String,
    eta: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                color = Color(0xFFEAEAF3),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = Color(0xFF14145C),
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(text = orderId, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A24))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "To: $destination", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "ETA: $eta", fontSize = 13.sp, color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold)
            }

            // Map Action Icon
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = "View Map",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
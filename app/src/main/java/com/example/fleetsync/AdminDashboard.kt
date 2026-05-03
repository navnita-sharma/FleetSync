package com.example.fleetsync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard() {
    val adminNavy = Color(0xFF0D1260)
    val backgroundGray = Color(0xFFF8F9FA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Console", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = { /* Logout */ }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = adminNavy
                )
            )
        },
        containerColor = backgroundGray
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Fleet Compliance Overview",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = adminNavy
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminStatCard("Total Fleets", "128", Icons.Default.Business, adminNavy, Modifier.weight(1f))
                    AdminStatCard("Active Drivers", "1,402", Icons.Default.Person, Color(0xFF10B981), Modifier.weight(1f))
                }
            }

            item {
                AdminStatCard("Compliance Rate", "98.5%", Icons.Default.Gavel, Color(0xFFF59E0B), Modifier.fillMaxWidth())
            }

            item {
                Text(
                    "System Alerts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = adminNavy,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(3) { index ->
                AlertItem(
                    title = if (index == 0) "License Expiring: Driver #442" else "Unauthorized Route: Fleet #09",
                    time = "2h ago",
                    severity = if (index == 0) Color(0xFFEF4444) else Color(0xFFF59E0B)
                )
            }
        }
    }
}

@Composable
fun AdminStatCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Text(title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AlertItem(title: String, time: String, severity: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(severity, RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(time, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

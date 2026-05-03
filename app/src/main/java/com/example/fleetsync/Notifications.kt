package com.example.fleetsync

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Legacy local model kept for backward compat (no longer rendered) ──────────
data class NotificationItem(
    val id: Int, val title: String, val body: String, val time: String,
    val type: NotifType, val isRead: Boolean = false
)
enum class NotifType { ALERT, INFO, SUCCESS, WARNING }

// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    isDarkMode: Boolean = false,
    onBackClick: () -> Unit = {},
    viewModel: NotificationsViewModel = viewModel()
) {
    val bgColor   = if (isDarkMode) Color(0xFF0F0F0F) else Color(0xFFF8F9FB)
    val cardBg    = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
    val textColor = if (isDarkMode) Color.White else Color(0xFF1A1F71)
    val orange    = Color(0xFFE68A1E)

    val notifications = viewModel.notifications
    val unreadCount   = viewModel.unreadCount
    val isLoading     = viewModel.isLoading

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Unread", "Alerts")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        if (unreadCount > 0) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(orange, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$unreadCount", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.markAllRead() }) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Mark all read", tint = textColor)
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
        ) {
            // ── Tabs ─────────────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = bgColor,
                contentColor     = orange,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color    = orange
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize   = 13.sp
                            )
                        },
                        selectedContentColor   = orange,
                        unselectedContentColor = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = orange)
                    }
                }
                notifications.isEmpty() -> {
                    EmptyNotificationsState(isDarkMode = isDarkMode)
                }
                else -> {
                    val filtered = when (selectedTab) {
                        1    -> notifications.filter { !it.isRead }
                        2    -> notifications.filter { it.type == "ALERT" || it.type == "WARNING" }
                        else -> notifications
                    }

                    if (filtered.isEmpty()) {
                        EmptyNotificationsState(isDarkMode = isDarkMode)
                    } else {
                        LazyColumn(
                            contentPadding       = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement  = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.notifId }) { notif ->
                                LiveNotifCard(
                                    notif      = notif,
                                    cardBg     = cardBg,
                                    isDarkMode = isDarkMode,
                                    onTap      = { if (!notif.isRead) viewModel.markRead(notif.notifId) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// ─── Live notification card ───────────────────────────────────────────────────
@Composable
fun LiveNotifCard(
    notif: NotificationModel,
    cardBg: Color,
    isDarkMode: Boolean,
    onTap: () -> Unit
) {
    val (iconBg, iconColor, icon) = when (notif.type) {
        "ALERT"   -> Triple(Color(0xFFFFE5E5), Color(0xFFD32F2F), Icons.Default.Warning)
        "WARNING" -> Triple(Color(0xFFFFF3CD), Color(0xFFF57F17), Icons.Default.WarningAmber)
        "SUCCESS" -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.CheckCircle)
        else      -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), Icons.Default.Info)
    }

    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault())
        .format(Date(notif.timestamp))

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .clickable { onTap() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (!notif.isRead) cardBg
                             else if (isDarkMode) Color(0xFF151515) else Color(0xFFF8F9FB)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notif.isRead) 2.dp else 0.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            // Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier             = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment    = Alignment.CenterVertically
                ) {
                    Text(
                        notif.title,
                        fontWeight = if (!notif.isRead) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontSize   = 14.sp,
                        color      = if (isDarkMode) Color.White else Color(0xFF1A1F71),
                        modifier   = Modifier.weight(1f)
                    )
                    Text(timeStr, fontSize = 11.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    notif.body,
                    fontSize   = 12.sp,
                    color      = Color.Gray,
                    lineHeight = 17.sp
                )
                // Role badge
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (notif.targetRole == "Driver") Color(0xFF6366F1).copy(alpha = 0.1f)
                            else Color(0xFFE68A1E).copy(alpha = 0.1f)
                ) {
                    Text(
                        text     = if (notif.targetRole == "Driver") "For Driver" else "For Fleet Owner",
                        fontSize = 10.sp,
                        color    = if (notif.targetRole == "Driver") Color(0xFF6366F1) else Color(0xFFE68A1E),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Unread dot
            if (!notif.isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE68A1E))
                )
            }
        }
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────
@Composable
fun EmptyNotificationsState(isDarkMode: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE68A1E).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.NotificationsNone,
                    contentDescription = null,
                    tint = Color(0xFFE68A1E),
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "All caught up!",
                fontSize   = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = if (isDarkMode) Color.White else Color(0xFF1A1F71)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "No notifications here yet.\nYou'll be notified when something important happens.",
                fontSize   = 13.sp,
                color      = Color.Gray,
                lineHeight = 18.sp,
                textAlign  = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ─── Legacy NotifCard (kept so nothing else breaks) ──────────────────────────
@Composable
fun NotifCard(notif: NotificationItem, cardBg: Color, isDarkMode: Boolean) {
    val (iconBg, iconColor, icon) = when (notif.type) {
        NotifType.ALERT   -> Triple(Color(0xFFFFE5E5), Color(0xFFD32F2F), Icons.Default.Warning)
        NotifType.WARNING -> Triple(Color(0xFFFFF3CD), Color(0xFFF57F17), Icons.Default.WarningAmber)
        NotifType.SUCCESS -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.CheckCircle)
        NotifType.INFO    -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), Icons.Default.Info)
    }
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (!notif.isRead) cardBg else if (isDarkMode) Color(0xFF151515) else Color(0xFFF8F9FB)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notif.isRead) 2.dp else 0.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(notif.title, fontWeight = if (!notif.isRead) FontWeight.ExtraBold else FontWeight.SemiBold, fontSize = 14.sp, color = if (isDarkMode) Color.White else Color(0xFF1A1F71))
                    Text(notif.time, fontSize = 11.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(notif.body, fontSize = 12.sp, color = Color.Gray, lineHeight = 17.sp)
            }
            if (!notif.isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE68A1E)))
            }
        }
    }
}

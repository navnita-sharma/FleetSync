package com.example.fleetsync

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit = {}
) {
    var userName by remember { mutableStateOf("Loading...") }
    var userRole  by remember { mutableStateOf("...") }
    var userId    by remember { mutableStateOf("...") }
    var saveSnack by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userName = document.getString("name") ?: "Alex Rivera"
                        userRole = document.getString("role") ?: "Logistics Owner"
                        userId   = "ID: ${uid.take(8).uppercase()}"
                    }
                }
        }
    }

    val darkBg      = if (isDarkMode) Color(0xFF0F0F0F) else Color(0xFFF8F9FB)
    val cardBg      = if (isDarkMode) Color(0xFF1A1A1A) else Color.White
    val accentOrange = Color(0xFFE68A1E)
    val textGray    = if (isDarkMode) Color(0xFF94A3B8) else Color.Gray
    val textColor   = if (isDarkMode) Color.White else Color.Black

    // Snackbar for save confirmation
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(saveSnack) {
        if (saveSnack) {
            snackbarHostState.showSnackbar("Settings saved")
            saveSnack = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowBackIosNew, contentDescription = null, tint = textGray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back", color = textGray)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = darkBg)
            )
        },
        bottomBar = {
            Surface(color = darkBg, tonalElevation = 8.dp) {
                Button(
                    onClick = { saveSnack = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = darkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Section
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (isDarkMode) Color(0xFF2D3748) else Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = if (isDarkMode) Color.White else Color(0xFF1A1F71), modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(userName, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(userRole, color = textGray, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Green))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(userId, color = textGray, fontSize = 12.sp)
                        }
                    }
                    Icon(Icons.Default.Verified, contentDescription = null, tint = accentOrange)
                }
            }

            // ── Display Appearance ─────────────────────────────────────────────
            SettingsSectionTitle("DISPLAY APPEARANCE", isDarkMode)
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Interface Theme", color = textColor, fontWeight = FontWeight.Bold)
                            Text("Choose your visual environment", color = textGray, fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(if (isDarkMode) Color(0xFF2D3748) else Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = accentOrange, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ThemeSwitcher(isDarkMode, onThemeChanged)
                }
            }

            // ── UI Language ────────────────────────────────────────────────────
            SettingsSectionTitle("INTERFACE LANGUAGE", isDarkMode)
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("App Language", color = textColor, fontWeight = FontWeight.Bold)
                            Text("Requires app restart to apply", color = textGray, fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(if (isDarkMode) Color(0xFF2D3748) else Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = accentOrange, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // UI Language switcher — calls activity.recreate() only for UI lang
                    UiLanguageSwitcher(isDarkMode)
                }
            }

            // ── Voice Language ─────────────────────────────────────────────────
            SettingsSectionTitle("VOICE ANNOUNCEMENTS", isDarkMode)
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Announcement Language", color = textColor, fontWeight = FontWeight.Bold)
                            Text("Language for toll crossing alerts", color = textGray, fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(if (isDarkMode) Color(0xFF2D3748) else Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = accentOrange, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Voice Language switcher — saves pref ONLY, no app restart
                    VoiceLanguageSwitcher(isDarkMode)
                }
            }

            // ── Notifications ──────────────────────────────────────────────────
            SettingsSectionTitle("NOTIFICATIONS", isDarkMode)
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    NotificationToggle("Toll Transaction Alerts", Icons.Default.Flip, true, isDarkMode)
                    HorizontalDivider(color = textColor.copy(alpha = 0.05f))
                    NotificationToggle("System Updates", Icons.Default.Refresh, true, isDarkMode)
                    HorizontalDivider(color = textColor.copy(alpha = 0.05f))
                    NotificationToggle("Weekly Reports", Icons.Default.Assessment, false, isDarkMode)
                }
            }

            // ── Security ───────────────────────────────────────────────────────
            SettingsSectionTitle("SECURITY", isDarkMode)
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SecurityItem("Change Security PIN", "Last changed 4 months ago", Icons.Default.Lock, isDarkMode)
                    HorizontalDivider(color = textColor.copy(alpha = 0.05f))
                    SecurityItem("Two-Factor Authentication", "STATUS: ENABLED", Icons.Default.VerifiedUser, isDarkMode)
                }
            }

            // Privacy Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkMode) 0.dp else 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row {
                        Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = accentOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Privacy & Compliance", color = textColor, fontWeight = FontWeight.Bold)
                            Text(
                                "Your data is handled according to the Digital Personal Data Protection (DPDP) Act.",
                                color = textGray,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Text("TERMS OF SERVICE", color = accentOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(24.dp))
                        Text("PRIVACY POLICY", color = accentOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Log Out $userName", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── UI Language Switcher — calls recreate() for proper locale reload ─────────
@Composable
fun UiLanguageSwitcher(isDarkMode: Boolean) {
    val context = LocalContext.current
    val currentLang = remember { mutableStateOf(LanguageManager.getLanguage(context)) }
    var pendingLang  by remember { mutableStateOf<String?>(null) }
    var showDialog   by remember { mutableStateOf(false) }

    val containerBg    = if (isDarkMode) Color(0xFF0F0F0F) else Color(0xFFEDF2F7)
    val selectionColor = if (isDarkMode) Color(0xFF2D3748) else Color.White
    val accentOrange   = Color(0xFFE68A1E)

    val languages = listOf(
        "en" to "English",
        "hi" to "हिन्दी",
        "mr" to "मराठी",
        "gu" to "ગુજરાતી",
        "ta" to "தமிழ்",
        "te" to "తెలుగు"
    )

    if (showDialog && pendingLang != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false; pendingLang = null },
            title = { Text("Restart App?") },
            text  = { Text("The app will restart to apply the new language.") },
            confirmButton = {
                TextButton(onClick = {
                    LanguageManager.setLanguage(context, pendingLang!!)
                    currentLang.value = pendingLang!!
                    showDialog = false
                    pendingLang = null
                    (context as? android.app.Activity)?.recreate()
                }) { Text("Restart", color = accentOrange, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false; pendingLang = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerBg, RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        languages.chunked(2).forEach { rowLangs ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowLangs.forEach { (code, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentLang.value == code) selectionColor else Color.Transparent)
                            .border(
                                width = if (currentLang.value == code) 2.dp else 0.dp,
                                color = if (currentLang.value == code) accentOrange else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (currentLang.value != code) {
                                    pendingLang = code
                                    showDialog  = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (currentLang.value == code)
                                (if (isDarkMode) Color.White else Color(0xFF1A1F71)) else Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = if (currentLang.value == code) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ─── Voice Language Switcher — saves pref ONLY, NO app restart, NO crash ──────
@Composable
fun VoiceLanguageSwitcher(isDarkMode: Boolean) {
    val context     = LocalContext.current
    val currentLang = remember { mutableStateOf(LanguageManager.getVoiceLanguage(context)) }

    val containerBg    = if (isDarkMode) Color(0xFF0F0F0F) else Color(0xFFEDF2F7)
    val selectionColor = if (isDarkMode) Color(0xFF2D3748) else Color.White
    val accentOrange   = Color(0xFFE68A1E)

    val languages = listOf(
        "en" to "English",
        "hi" to "हिन्दी",
        "mr" to "मराठी",
        "gu" to "ગુજરાતી",
        "ta" to "தமிழ்",
        "te" to "తెలుగు"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerBg, RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        languages.chunked(2).forEach { rowLangs ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowLangs.forEach { (code, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentLang.value == code) selectionColor else Color.Transparent)
                            .border(
                                width = if (currentLang.value == code) 2.dp else 0.dp,
                                color = if (currentLang.value == code) accentOrange else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                // Only save pref — TTS picks it up on next notification
                                LanguageManager.setVoiceLanguage(context, code)
                                currentLang.value = code
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (currentLang.value == code)
                                (if (isDarkMode) Color.White else Color(0xFF1A1F71)) else Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = if (currentLang.value == code) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Confirmation hint
        Text(
            "🔊 Voice language applies to the next toll/alert notification",
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )
    }
}

// ─── Legacy LanguageSwitcher kept for backward compat ─────────────────────────
@Composable
fun LanguageSwitcher(isDarkMode: Boolean) = UiLanguageSwitcher(isDarkMode)

@Composable
fun SettingsSectionTitle(title: String, isDarkMode: Boolean) {
    Text(
        text = title,
        color = if (isDarkMode) Color(0xFF4A5568) else Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun ThemeSwitcher(isDarkMode: Boolean, onThemeChanged: (Boolean) -> Unit) {
    val containerBg    = if (isDarkMode) Color(0xFF0F0F0F) else Color(0xFFEDF2F7)
    val selectionColor = if (isDarkMode) Color(0xFF2D3748) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(containerBg, RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(if (!isDarkMode) selectionColor else Color.Transparent)
                .clickable { onThemeChanged(false) },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LightMode, contentDescription = null, tint = if (!isDarkMode) Color(0xFF1A1F71) else Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Light", color = if (!isDarkMode) Color(0xFF1A1F71) else Color.Gray, fontSize = 14.sp)
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(if (isDarkMode) selectionColor else Color.Transparent)
                .clickable { onThemeChanged(true) },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DarkMode, contentDescription = null, tint = if (isDarkMode) Color.White else Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dark", color = if (isDarkMode) Color.White else Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun NotificationToggle(title: String, icon: ImageVector, initialValue: Boolean, isDarkMode: Boolean) {
    var checked by remember { mutableStateOf(initialValue) }
    val accentOrange = Color(0xFFE68A1E)
    val textColor    = if (isDarkMode) Color.White else Color.Black

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(if (isDarkMode) Color(0xFF2D3748) else Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (isDarkMode) Color.White else Color(0xFF1A1F71), modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = textColor, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor   = Color.White,
                checkedTrackColor   = accentOrange,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = if (isDarkMode) Color(0xFF2D3748) else Color(0xFFCBD5E0)
            )
        )
    }
}

@Composable
fun SecurityItem(title: String, subtitle: String, icon: ImageVector, isDarkMode: Boolean) {
    val textColor = if (isDarkMode) Color.White else Color.Black
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = textColor, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

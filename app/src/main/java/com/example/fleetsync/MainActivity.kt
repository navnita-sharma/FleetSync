package com.example.fleetsync

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fleetsync.ui.theme.FleetSyncTheme
import com.google.firebase.FirebaseApp

// --- 1. MAIN ACTIVITY ---
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FleetSyncTheme {
                val authViewModel: AuthViewModel = viewModel()
                AppNavigation(authViewModel = authViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause")
    }

    @SuppressLint("MissingSuperCall")
    override fun onDestroy() {
        Log.d("MainActivity", "onDestroy")
        super.onDestroy()
    }
}

// --- Theme Colors ---
val BrandNavy = Color(0xFF0D1260)
val BackgroundGray = Color(0xFFF8F9FA)
val FieldBackground = Color(0xFFEBECEF)
val TextDark = Color(0xFF1E202C)
val TextMuted = Color(0xFF5A5D6B)
val BorderGray = Color(0xFFE2E4E9)

// --- 2. LOGIN SCREEN ---
@Composable
fun FleetSyncLoginScreen(
    authState: AuthState,
    onLoginClick: (String, String) -> Unit,
    onSignUpClick: () -> Unit,
    onTrackShipmentClick: () -> Unit
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo
            Text(text = "FleetSync", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = BrandNavy, letterSpacing = (-0.5).sp)

            Spacer(modifier = Modifier.height(48.dp))

            // Headers
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Welcome Back", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = BrandNavy)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Manage your logistics network with precision.", fontSize = 15.sp, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(32.dp))

            AuthToggleSwitch(onSignUpClick = onSignUpClick)

            Spacer(modifier = Modifier.height(32.dp))

            EmailOrPhoneInputField(value = emailOrPhone, onValueChange = { emailOrPhone = it })

            Spacer(modifier = Modifier.height(20.dp))

            PasswordInputField(value = password, onValueChange = { password = it })

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onLoginClick(emailOrPhone, password) },
                enabled = authState !is AuthState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = BrandNavy.copy(alpha = 0.4f)),
                colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Access Dashboard", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGray)
                Text("OR SECURE LOGIN WITH", modifier = Modifier.padding(horizontal = 16.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark, letterSpacing = 1.sp)
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGray)
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoogleIconPlaceholder()
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Continue with Google", color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGray)
                Text(
                    text = "EXPECTING A DELIVERY?",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    letterSpacing = 1.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { onTrackShipmentClick() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF3F4F6)),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandNavy)
            ) {
                Icon(Icons.Default.Map, contentDescription = null, tint = BrandNavy)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Track a Shipment",
                    color = BrandNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// --- 3. UI COMPONENTS ---

@Composable
fun AuthToggleSwitch(onSignUpClick: () -> Unit = {}) {
    var isLoginSelected by remember { mutableStateOf(true) }
    Surface(color = Color(0xFFF3F3F5), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(56.dp)) {
        Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = if (isLoginSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = if (isLoginSelected) 1.dp else 0.dp,
                modifier = Modifier.weight(1f).fillMaxHeight().clickable { isLoginSelected = true }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Login", fontWeight = if (isLoginSelected) FontWeight.SemiBold else FontWeight.Medium, color = if (isLoginSelected) BrandNavy else TextMuted, fontSize = 15.sp)
                }
            }
            Surface(
                color = if (!isLoginSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = if (!isLoginSelected) 1.dp else 0.dp,
                modifier = Modifier.weight(1f).fillMaxHeight().clickable {
                    isLoginSelected = false
                    onSignUpClick()
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Sign-Up", fontWeight = if (!isLoginSelected) FontWeight.SemiBold else FontWeight.Medium, color = if (!isLoginSelected) BrandNavy else TextMuted, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun EmailOrPhoneInputField(value: String, onValueChange: (String) -> Unit) {
    Column {
        Text("EMAIL ADDRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("name@company.com", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FieldBackground,
                unfocusedContainerColor = FieldBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = BrandNavy,
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark
            ),
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
    }
}

@Composable
fun PasswordInputField(value: String, onValueChange: (String) -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark, letterSpacing = 1.sp)
            Text("Forgot Password?", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandNavy, modifier = Modifier.clickable { })
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("••••••••", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FieldBackground,
                unfocusedContainerColor = FieldBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = BrandNavy,
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Password",
                        tint = TextDark
                    )
                }
            }
        )
    }
}

@Composable
fun GoogleIconPlaceholder() {
    Box(modifier = Modifier.size(24.dp).background(Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
        Text("G", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFFDB4437))
    }
}
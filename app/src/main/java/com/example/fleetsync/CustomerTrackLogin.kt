package com.example.fleetsync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fleetsync.ui.theme.FleetSyncTheme
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CustomerTrackLoginScreen(
    onTrackClick: (String) -> Unit, 
    onBackClick: () -> Unit = {},
    viewModel: CustomerTrackingViewModel = viewModel()
) {
    var shipmentId by remember { mutableStateOf("") }
    var passcode by remember { mutableStateOf("") }
    
    val verificationError = viewModel.verificationError

    val cardBg = Color.White
    val accentBlue = Color(0xFF1A1F71)
    val textGray = Color(0xFF64748B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFC), Color(0xFFFFF7ED)),
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = accentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Shipment Tracking",
                    color = accentBlue,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Secure Access Badge
                    Surface(
                        color = Color(0xFFE0E7FF),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = accentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SECURE ACCESS",
                                color = accentBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Track Your\nShipment",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B),
                        lineHeight = 40.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Access real-time logistics data. This secure link was shared by your fleet owner to ensure transparency.",
                        color = textGray,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Input Fields
                    Text(
                        text = "SHIPMENT ID",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textGray,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TrackingTextField(
                        value = shipmentId,
                        onValueChange = { shipmentId = it },
                        placeholder = "e.g., 550e8400-e29b-41d4-a716..."
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "6-DIGIT PASSCODE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textGray,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TrackingTextField(
                        value = passcode,
                        onValueChange = { if (it.length <= 6) passcode = it },
                        placeholder = ". . . . . ."
                    )
                    
                    if (verificationError != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = verificationError,
                            color = Color.Red,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Track Button
                    Button(
                        onClick = { 
                            if (shipmentId.isNotBlank() && passcode.length == 6) {
                                viewModel.verifyShipment(shipmentId, passcode) {
                                    onTrackClick(it)
                                }
                            }
                        },
                        enabled = !viewModel.isVerifying,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (viewModel.isVerifying) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Track Now",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Help Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.Help,
                                contentDescription = null,
                                tint = textGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Need Help?",
                                color = Color(0xFF1E293B),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            TextButton(onClick = { }) {
                                Text(
                                    "CONTACT SUPPORT",
                                    color = accentBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                "•",
                                color = Color.LightGray,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            TextButton(onClick = { }) {
                                Text(
                                    "FAQ",
                                    color = accentBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Text(
                text = "POWERED BY PRECISION NAVIGATOR PRO ©\n2024",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                color = textGray,
                letterSpacing = 1.5.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TrackingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                placeholder,
                color = Color(0xFF94A3B8),
                fontSize = 16.sp
            )
        },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFE2E8F0).copy(alpha = 0.5f),
            unfocusedContainerColor = Color(0xFFE2E8F0).copy(alpha = 0.5f),
            disabledContainerColor = Color(0xFFE2E8F0).copy(alpha = 0.5f),
            cursorColor = Color.Black,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
fun CustomerTrackLoginScreenPreview() {
    FleetSyncTheme {
        CustomerTrackLoginScreen(onTrackClick = { _ -> })
    }
}

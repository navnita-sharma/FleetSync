package com.example.fleetsync

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OTPVerificationScreen(
    isDarkMode: Boolean = true,
    phone: String = "",
    onBackClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onVerifyClick: (String) -> Unit
) {
    val darkBg = if (isDarkMode) Color(0xFF131824) else Color(0xFFF8F9FB)
    val cardBg = if (isDarkMode) Color(0xFF1E2533) else Color.White
    val accentOrange = Color(0xFFE68A1E)
    val textGray = if (isDarkMode) Color(0xFF94A3B8) else Color.Gray
    val textColor = if (isDarkMode) Color.White else Color.Black

    var otpCode by remember { mutableStateOf("") }
    val otpLength = 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        // Top Section
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Back button and Header
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = if (isDarkMode) Color.White else Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "SECURITY NODE 04",
                    color = accentOrange.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Verify Identity",
                color = textColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = buildAnnotatedString {
                    append("Enter the 6-digit code sent to $phone for secure ")
                    withStyle(style = SpanStyle(color = accentOrange)) {
                        append("logistics access.")
                    }
                },
                color = textGray,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // OTP Input Boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(otpLength) { index ->
                    OTPBox(
                        value = otpCode.getOrNull(index)?.toString() ?: "",
                        isFocused = otpCode.length == index,
                        modifier = Modifier.weight(1f),
                        isDarkMode = isDarkMode
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFE11D48),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Resend Code Badge
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(cardBg, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = accentOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("Resend code in ")
                            withStyle(style = SpanStyle(color = accentOrange, fontWeight = FontWeight.Bold)) {
                                append("00:45")
                            }
                        },
                        color = textGray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Verify Button
            Button(
                onClick = { if (!isLoading && otpCode.length == otpLength) onVerifyClick(otpCode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentOrange),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading && otpCode.length == otpLength
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (isLoading) "Verifying..." else "Verify & Proceed",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = textGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "DPDP COMPLIANT & ENCRYPTED",
                        color = textGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    "Toll Transaction Data Verification System",
                    color = textGray.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Numeric Keypad
        NumericKeypad(
            onKeyPress = { char ->
                if (!isLoading && otpCode.length < otpLength) otpCode += char
            },
            onDelete = {
                if (!isLoading && otpCode.isNotEmpty()) otpCode = otpCode.dropLast(1)
            },
            isDarkMode = isDarkMode
        )
    }
}

@Composable
fun OTPBox(value: String, isFocused: Boolean, modifier: Modifier = Modifier, isDarkMode: Boolean) {
    val accentOrange = Color(0xFFE68A1E)
    val boxBg = if (isDarkMode) Color(0xFF1E2533) else Color(0xFFEDF2F7)
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(boxBg, RoundedCornerShape(8.dp))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) accentOrange else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (value.isNotEmpty()) {
            Text(
                text = value,
                color = if (isDarkMode) Color.White else Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(50))
            )
        }
    }
}

@Composable
fun NumericKeypad(onKeyPress: (String) -> Unit, onDelete: () -> Unit, isDarkMode: Boolean) {
    val keypadBg = if (isDarkMode) Color(0xFF131824) else Color(0xFFEDF2F7)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(keypadBg.copy(alpha = 0.95f))
            .padding(vertical = 16.dp)
    ) {
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "backspace")
        )

        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    KeypadButton(
                        key = key,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        onClick = {
                            if (key == "backspace") onDelete()
                            else if (key.isNotEmpty()) onKeyPress(key)
                        },
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
    }
}

@Composable
fun KeypadButton(key: String, modifier: Modifier, onClick: () -> Unit, isDarkMode: Boolean) {
    Box(
        modifier = modifier.clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        when (key) {
            "backspace" -> Icon(
                Icons.Default.Backspace,
                contentDescription = "Delete",
                tint = if (isDarkMode) Color.White else Color.Black
            )
            "" -> {}
            else -> Text(
                text = key,
                color = if (isDarkMode) Color.White else Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

package com.example.fleetsync
 
import com.example.fleetsync.AuthState

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel

// Theme Colors
val SignUpNavyCore = Color(0xFF14145C)
val SignUpFieldBackground = Color(0xFFE8E8EC)
val SignUpTextDark = Color(0xFF1A1A24)
val SignUpTextGray = Color(0xFF6B6B78)
val SignUpLabelBlue = Color(0xFF1C1C66)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    isDarkMode: Boolean = false,
    authState: AuthState = AuthState.Idle,
    authViewModel: AuthViewModel = viewModel(),
    onRegisterClick: (email: String, password: String, name: String, phone: String, role: String, companyName: String, companyId: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            if (isDarkMode) Color(0xFF0F0F0F) else Color(0xFFF4F4F8),
            if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFF9F3EA),
            if (isDarkMode) Color(0xFF151515) else Color(0xFFEAEAF3),
            if (isDarkMode) Color(0xFF0F0F0F) else Color(0xFFF5F5F9)
        )
    )

    var selectedRole by remember { mutableStateOf("Fleet Owner") }

    // Core Firebase Fields
    var fullName     by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }

    // Fleet Owner extra fields
    var companyName    by remember { mutableStateOf("") }
    var gstId          by remember { mutableStateOf("") }
    var officeAddress  by remember { mutableStateOf("") }

    // Driver extra fields
    var dob                by remember { mutableStateOf("") }
    var dlNumber           by remember { mutableStateOf("") }
    var expiryDate         by remember { mutableStateOf("") }
    var emergencyContact   by remember { mutableStateOf("") }
    var residentialAddress by remember { mutableStateOf("") }
    // Driver company selection
    var selectedCompany    by remember { mutableStateOf<CompanyModel?>(null) }
    var companyExpanded    by remember { mutableStateOf(false) }

    // Fetch companies when Driver tab is selected
    LaunchedEffect(selectedRole) {
        if (selectedRole == "Driver") {
            authViewModel.fetchCompanies()
        }
    }

    val isLoading    = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = if (isDarkMode) Color.White else SignUpNavyCore,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onBackClick() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Logistics Core",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkMode) Color.White else SignUpNavyCore
                    )
                }
                Text(
                    text = "LC",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else SignUpNavyCore
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Header
            Text(
                text = "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDarkMode) Color.White else SignUpNavyCore
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Join the network of the Precision Navigators.",
                fontSize = 14.sp,
                color = if (isDarkMode) Color.LightGray else SignUpTextGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Role Toggle
            RoleToggle(selectedRole) { selectedRole = it }

            Spacer(modifier = Modifier.height(24.dp))

            // Core Fields
            CustomLabelledField(
                label = "FULL NAME",
                placeholder = "John Doe",
                value = fullName,
                onValueChange = { fullName = it },
                isDarkMode = isDarkMode
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Re-using the CustomLabelledField for Email
            CustomLabelledField(
                label = "EMAIL ADDRESS",
                placeholder = "name@example.com",
                value = email,
                onValueChange = { email = it },
                isDarkMode = isDarkMode,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            MobileNumberField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                isDarkMode = isDarkMode
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                value = password,
                onValueChange = { password = it },
                isDarkMode = isDarkMode
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Role-Specific Fields
            if (selectedRole == "Fleet Owner") {
                CustomLabelledField(
                    label = "COMPANY NAME",
                    placeholder = "Logistics Corp Inc.",
                    value = companyName,
                    onValueChange = { companyName = it },
                    isDarkMode = isDarkMode
                )
                Spacer(modifier = Modifier.height(16.dp))
                CustomLabelledField(
                    label = "GST / BUSINESS ID",
                    placeholder = "27AAAAA0000A1Z5",
                    value = gstId,
                    onValueChange = { gstId = it },
                    isDarkMode = isDarkMode
                )
                Spacer(modifier = Modifier.height(16.dp))
                CustomLabelledField(
                    label = "OFFICE ADDRESS",
                    placeholder = "Floor, Building, Area, City",
                    value = officeAddress,
                    onValueChange = { officeAddress = it },
                    modifier = Modifier.height(64.dp),
                    isDarkMode = isDarkMode
                )
            } else {
                // ── Driver-specific fields ──────────────────────────────────

                // Company selection dropdown
                val companies          = authViewModel.availableCompanies
                val isLoadingCompanies = authViewModel.isLoadingCompanies
                val companyFetchError  = authViewModel.companiesFetchError

                Text(
                    text = "SELECT FLEET COMPANY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.LightGray else SignUpLabelBlue,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Error banner with Retry button
                if (companyFetchError.isNotBlank() && !isLoadingCompanies) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFEDED)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFC53030),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = companyFetchError,
                                fontSize = 12.sp,
                                color = Color(0xFFC53030),
                                modifier = Modifier.weight(1f),
                                lineHeight = 16.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            TextButton(
                                onClick = { authViewModel.fetchCompanies() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Retry", fontWeight = FontWeight.Bold, color = SignUpNavyCore, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                ExposedDropdownMenuBox(
                    expanded = companyExpanded && companies.isNotEmpty(),
                    onExpandedChange = {
                        if (companies.isNotEmpty()) companyExpanded = !companyExpanded
                    }
                ) {
                    TextField(
                        value = when {
                            isLoadingCompanies      -> "Loading companies…"
                            selectedCompany != null -> selectedCompany!!.companyName
                            companies.isEmpty()     -> if (companyFetchError.isNotBlank())
                                                          "Could not load — tap Retry above"
                                                       else
                                                          "No companies registered yet"
                            else                    -> "Tap to select your fleet"
                        },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            if (isLoadingCompanies) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = SignUpNavyCore
                                )
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = companyExpanded)
                            }
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor   = if (isDarkMode) Color(0xFF2D2D35) else SignUpFieldBackground,
                            unfocusedContainerColor = if (isDarkMode) Color(0xFF2D2D35) else SignUpFieldBackground,
                            focusedIndicatorColor   = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor        = if (isDarkMode) Color.White else SignUpTextDark,
                            unfocusedTextColor      = if (isDarkMode) Color.LightGray else Color(0xFF888888)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = companyExpanded && companies.isNotEmpty(),
                        onDismissRequest = { companyExpanded = false }
                    ) {
                        companies.forEach { company ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.BusinessCenter,
                                            contentDescription = null,
                                            tint = SignUpNavyCore,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(company.companyName, fontWeight = FontWeight.Medium)
                                    }
                                },
                                onClick = {
                                    selectedCompany = company
                                    companyExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                CustomLabelledField(
                    label = "DATE OF BIRTH",
                    placeholder = "DD/MM/YYYY",
                    value = dob,
                    onValueChange = { dob = it },
                    isDarkMode = isDarkMode
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CustomLabelledField(
                        label = "DL NUMBER",
                        placeholder = "DL-12345678",
                        value = dlNumber,
                        onValueChange = { dlNumber = it },
                        modifier = Modifier.weight(1f),
                        isDarkMode = isDarkMode
                    )
                    CustomLabelledField(
                        label = "EXPIRY DATE",
                        placeholder = "MM/YY",
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        modifier = Modifier.weight(1f),
                        isDarkMode = isDarkMode
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                CustomLabelledField(
                    label = "EMERGENCY CONTACT",
                    placeholder = "Name & Number",
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    isDarkMode = isDarkMode
                )
                Spacer(modifier = Modifier.height(16.dp))
                CustomLabelledField(
                    label = "RESIDENTIAL ADDRESS",
                    placeholder = "House No, Street, Area, City",
                    value = residentialAddress,
                    onValueChange = { residentialAddress = it },
                    modifier = Modifier.height(64.dp),
                    isDarkMode = isDarkMode
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Terms Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                var checked by remember { mutableStateOf(false) }
                Checkbox(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SignUpNavyCore,
                        uncheckedColor = if (isDarkMode) Color.Gray else Color(0xFFCBD5E1)
                    )
                )
                Text(
                    text = buildAnnotatedString {
                        append("I agree to the ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else SignUpNavyCore, textDecoration = TextDecoration.Underline)) {
                            append("Terms of Service")
                        }
                        append(" and ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else SignUpNavyCore, textDecoration = TextDecoration.Underline)) {
                            append("Privacy Policy")
                        }
                        append(".")
                    },
                    fontSize = 13.sp,
                    color = if (isDarkMode) Color.White else SignUpTextDark,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Firebase Registration Button
            Button(
                onClick = {
                    if (fullName.isNotBlank() && email.isNotBlank() && mobileNumber.isNotBlank() && password.isNotBlank()) {
                        if (selectedRole == "Driver" && selectedCompany == null) {
                            Toast.makeText(context, "Please select your fleet company", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onRegisterClick(
                            email.trim(),
                            password,
                            fullName,
                            mobileNumber,
                            selectedRole,
                            companyName,
                            selectedCompany?.companyId ?: ""
                        )
                    } else {
                        Toast.makeText(context, "Please fill in all core fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = SignUpNavyCore),
                colors = ButtonDefaults.buttonColors(containerColor = SignUpNavyCore),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = Color(0xFFE11D48),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Already have an account? ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = if (isDarkMode) Color.White else SignUpNavyCore)) {
                            append("Log in")
                        }
                    },
                    fontSize = 14.sp,
                    color = if (isDarkMode) Color.LightGray else SignUpTextGray,
                    modifier = Modifier.clickable { onBackClick() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun RoleToggle(selectedRole: String, onRoleSelected: (String) -> Unit) {
    Surface(
        color = Color(0xFFF4F4F5),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fleet Owner Tab
            val isFleetOwner = selectedRole == "Fleet Owner"
            Surface(
                color = if (isFleetOwner) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = if (isFleetOwner) 2.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onRoleSelected("Fleet Owner") }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.BusinessCenter,
                        contentDescription = null,
                        tint = if (isFleetOwner) SignUpNavyCore else SignUpTextGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fleet Owner",
                        fontWeight = if (isFleetOwner) FontWeight.Bold else FontWeight.Medium,
                        color = if (isFleetOwner) SignUpNavyCore else SignUpTextGray,
                        fontSize = 14.sp
                    )
                }
            }

            // Driver Tab
            val isDriver = selectedRole == "Driver"
            Surface(
                color = if (isDriver) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = if (isDriver) 2.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onRoleSelected("Driver") }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = if (isDriver) SignUpNavyCore else SignUpTextGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Driver",
                        fontWeight = if (isDriver) FontWeight.Bold else FontWeight.Medium,
                        color = if (isDriver) SignUpNavyCore else SignUpTextGray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CustomLabelledField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    isDarkMode: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier   // modifier goes on the COLUMN so weight() constrains the whole field
) {
    // ── IMPORTANT: modifier is applied to the Column, NOT the TextField.
    // Passing Modifier.weight(1f) or Modifier.height() here correctly constrains
    // both the label and the input field as a unit inside a Row.
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color.LightGray else SignUpLabelBlue,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0xFFA0A0AB)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = if (isDarkMode) Color(0xFF2D2D35) else SignUpFieldBackground,
                unfocusedContainerColor = if (isDarkMode) Color(0xFF2D2D35) else SignUpFieldBackground,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor             = SignUpNavyCore,
                focusedTextColor        = if (isDarkMode) Color.White else SignUpTextDark,
                unfocusedTextColor      = if (isDarkMode) Color.White else SignUpTextDark
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@Composable
fun MobileNumberField(value: String, onValueChange: (String) -> Unit, isDarkMode: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCountryCode by remember { mutableStateOf("+91") }
    val countryCodes = listOf("+91", "+1", "+44", "+971", "+61")

    Column {
        Text(
            text = "MOBILE NUMBER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color.LightGray else SignUpLabelBlue,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box {
                Surface(
                    color = if (isDarkMode) Color(0xFF2D2D35) else SignUpFieldBackground,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .width(90.dp)
                        .height(56.dp)
                        .clickable { expanded = true }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedCountryCode, fontWeight = FontWeight.SemiBold, color = if (isDarkMode) Color.White else SignUpTextDark)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = SignUpTextGray)
                    }
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    countryCodes.forEach { code ->
                        DropdownMenuItem(
                            text = { Text(code) },
                            onClick = {
                                selectedCountryCode = code
                                expanded = false
                            }
                        )
                    }
                }
            }

            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("000-000-0000", color = Color(0xFFA0A0AB)) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = if (isDarkMode) Color(0xFF2D2D35) else SignUpFieldBackground,
                    unfocusedContainerColor = if (isDarkMode) Color(0xFF2D2D35) else SignUpFieldBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = SignUpNavyCore,
                    focusedTextColor = if (isDarkMode) Color.White else SignUpTextDark,
                    unfocusedTextColor = if (isDarkMode) Color.White else SignUpTextDark
                ),
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
        }
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isDarkMode: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "PASSWORD",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkMode) Color.LightGray else SignUpLabelBlue,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("••••••••", color = Color(0xFFA0A0AB)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = if (isDarkMode) Color(0xFF2D2D35) else SignUpFieldBackground,
                unfocusedContainerColor = if (isDarkMode) Color(0xFF2D2D35) else SignUpFieldBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = SignUpNavyCore,
                focusedTextColor = if (isDarkMode) Color.White else SignUpTextDark,
                unfocusedTextColor = if (isDarkMode) Color.White else SignUpTextDark
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Password Visibility",
                        tint = if (isDarkMode) Color.White else SignUpTextDark
                    )
                }
            }
        )
    }
}
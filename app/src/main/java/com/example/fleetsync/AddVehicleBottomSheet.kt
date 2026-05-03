package com.example.fleetsync

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleBottomSheet(
    onDismiss: () -> Unit,
    onSave: (VehicleModel) -> Unit
) {
    var vehicleNumber by remember { mutableStateOf("") }
    var chassisNumber by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("Truck") }
    var model by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("Diesel") }
    var capacity by remember { mutableStateOf("") }
    var insuranceExpiry by remember { mutableStateOf("") }
    var pucExpiry by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Add New Vehicle", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = vehicleNumber,
            onValueChange = { vehicleNumber = it },
            label = { Text("Vehicle Number") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = chassisNumber,
            onValueChange = { chassisNumber = it },
            label = { Text("Chassis Number") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Vehicle Type Dropdown
        var typeExpanded by remember { mutableStateOf(false) }
        val types = listOf("Truck", "Van", "Lorry", "Trailer", "Pickup")
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = !typeExpanded }
        ) {
            OutlinedTextField(
                value = vehicleType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Vehicle Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                types.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            vehicleType = type
                            typeExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            label = { Text("Model (e.g. Tata Prima)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Fuel Type Dropdown
        var fuelExpanded by remember { mutableStateOf(false) }
        val fuels = listOf("Diesel", "Petrol", "CNG", "Electric")
        ExposedDropdownMenuBox(
            expanded = fuelExpanded,
            onExpandedChange = { fuelExpanded = !fuelExpanded }
        ) {
            OutlinedTextField(
                value = fuelType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fuel Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = fuelExpanded,
                onDismissRequest = { fuelExpanded = false }
            ) {
                fuels.forEach { fuel ->
                    DropdownMenuItem(
                        text = { Text(fuel) },
                        onClick = {
                            fuelType = fuel
                            fuelExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = capacity,
            onValueChange = { capacity = it },
            label = { Text("Capacity (Tons)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Insurance Expiry
        OutlinedTextField(
            value = insuranceExpiry,
            onValueChange = { insuranceExpiry = it },
            label = { Text("Insurance Expiry Date") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    DatePickerDialog(context, { _, y, m, d ->
                        insuranceExpiry = "$d/${m+1}/$y"
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // PUC Expiry
        OutlinedTextField(
            value = pucExpiry,
            onValueChange = { pucExpiry = it },
            label = { Text("PUC Expiry Date") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = {
                    DatePickerDialog(context, { _, y, m, d ->
                        pucExpiry = "$d/${m+1}/$y"
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (vehicleNumber.isBlank() || model.isBlank() || capacity.isBlank()) {
                    Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                } else {
                    onSave(
                        VehicleModel(
                            vehicleNumber = vehicleNumber,
                            chassisNumber = chassisNumber,
                            vehicleType = vehicleType,
                            model = model,
                            fuelType = fuelType,
                            capacity = capacity,
                            insuranceExpiry = insuranceExpiry,
                            pucExpiry = pucExpiry
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save Vehicle")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
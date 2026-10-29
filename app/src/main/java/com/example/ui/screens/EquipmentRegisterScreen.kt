package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EquipmentEntity
import com.example.ui.components.WfhHeader
import com.example.ui.theme.*

@Composable
fun EquipmentRegisterScreen(
    equipment: List<EquipmentEntity>,
    onAddEquipment: (EquipmentEntity) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = WfhDarkBackground,
        topBar = {
            WfhHeader(
                title = "Equipment Register",
                subtitle = "Occupational Hygiene Instrumentation",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = WfhRedPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_equipment_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Instrument")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    color = WfhSurfaceCard,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Calibration",
                            tint = ComplianceGreenLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "All listed instruments require SANAS 17025 accredited calibration before deployment in mining environments.",
                            fontSize = 11.sp,
                            color = WfhTextSecondary
                        )
                    }
                }
            }

            items(equipment) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "${item.manufacturer} • Model: ${item.model}",
                                    fontSize = 12.sp,
                                    color = WfhTextSecondary
                                )
                            }

                            Surface(
                                color = if (item.isWarning) CriticalRedContainer else ComplianceGreenContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = item.status,
                                    color = if (item.isWarning) CriticalRed else ComplianceGreenLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = WfhBorderSubtle)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Serial Number", fontSize = 10.sp, color = WfhTextMuted)
                                Text(item.serialNumber, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                            Column {
                                Text("Asset ID", fontSize = 10.sp, color = WfhTextMuted)
                                Text(item.assetIdentifier, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = WfhGold)
                            }
                            Column {
                                Text("Calibrated Date", fontSize = 10.sp, color = WfhTextMuted)
                                Text(item.calibrationDate, fontSize = 12.sp, color = Color.White)
                            }
                            Column {
                                Text("Expiry Date", fontSize = 10.sp, color = WfhTextMuted)
                                Text(item.calibrationExpiryDate, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (item.isWarning) CriticalRed else ComplianceGreenLight)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var manufacturer by remember { mutableStateOf("") }
        var model by remember { mutableStateOf("") }
        var serial by remember { mutableStateOf("") }
        var expiry by remember { mutableStateOf("2026-12-31") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = WfhSurfaceElevated,
            title = { Text("Register New Instrument", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Instrument Name (e.g. Kestrel 4000)") }
                    )
                    OutlinedTextField(
                        value = manufacturer,
                        onValueChange = { manufacturer = it },
                        label = { Text("Manufacturer") }
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model Identifier") }
                    )
                    OutlinedTextField(
                        value = serial,
                        onValueChange = { serial = it },
                        label = { Text("Serial Number") }
                    )
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { expiry = it },
                        label = { Text("Calibration Expiry Date") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotEmpty()) {
                            onAddEquipment(
                                EquipmentEntity(
                                    id = "eq_${System.currentTimeMillis()}",
                                    name = name,
                                    manufacturer = manufacturer.ifEmpty { "Certified Vendor" },
                                    model = model.ifEmpty { "Standard" },
                                    serialNumber = serial.ifEmpty { "SN-9988" },
                                    assetIdentifier = "WFH-INST-${System.currentTimeMillis().toString().takeLast(3)}",
                                    calibrationDate = "2026-06-01",
                                    calibrationExpiryDate = expiry,
                                    status = "Calibrated",
                                    isWarning = false
                                )
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary)
                ) {
                    Text("Add Instrument")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = WfhTextSecondary)
                }
            }
        )
    }
}

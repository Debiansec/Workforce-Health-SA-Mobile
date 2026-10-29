package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.Customer
import com.example.data.model.CustomerSite
import com.example.data.model.ChecklistTemplate
import com.example.ui.components.WfhHeader
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSiteSelectionScreen(
    customers: List<Customer>,
    sites: List<CustomerSite>,
    facilities: List<String>,
    selectedCustomer: Customer?,
    selectedSite: CustomerSite?,
    selectedFacility: String,
    selectedTemplateTitle: String,
    onSelectCustomer: (Customer) -> Unit,
    onSelectSite: (CustomerSite) -> Unit,
    onSelectFacility: (String) -> Unit,
    onSelectTemplateTitle: (String) -> Unit,
    onStartInspection: () -> Unit,
    onBack: () -> Unit,
    onAddCustomer: (Customer) -> Unit,
    onAddSite: (CustomerSite) -> Unit
) {
    var showAddSiteDialog by remember { mutableStateOf(false) }
    var templateDropdownExpanded by remember { mutableStateOf(false) }
    var facilityDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = WfhDarkBackground,
        topBar = {
            WfhHeader(
                title = "New Assessment Setup",
                subtitle = "Select Company, Site & Facility",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Checklist Module Title Selector (Required by Section 7 of instructions)
                Text(
                    text = "Checklist Module Type",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = WfhTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = templateDropdownExpanded,
                    onExpandedChange = { templateDropdownExpanded = !templateDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedTemplateTitle,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("template_title_dropdown"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WfhRedPrimary,
                            unfocusedBorderColor = WfhBorderSubtle,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = WfhSurfaceCard,
                            unfocusedContainerColor = WfhSurfaceCard
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = templateDropdownExpanded,
                        onDismissRequest = { templateDropdownExpanded = false },
                        modifier = Modifier.background(WfhSurfaceElevated)
                    ) {
                        ChecklistTemplate.TEMPLATE_TITLES.forEach { title ->
                            DropdownMenuItem(
                                text = { Text(title, color = Color.White) },
                                onClick = {
                                    onSelectTemplateTitle(title)
                                    templateDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Customer Selection
            item {
                Text(
                    text = "Customer Company",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = WfhTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    customers.forEach { customer ->
                        val isSelected = selectedCustomer?.id == customer.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectCustomer(customer) }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) WfhRedPrimary else WfhBorderSubtle,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) WfhSurfaceElevated else WfhSurfaceCard
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) WfhRedPrimary else WfhSurfaceDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = "Company",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = customer.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "VAT: ${customer.vatNumber} • Mining Right: ${customer.miningRightRef}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WfhTextSecondary
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectCustomer(customer) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = WfhRedPrimary,
                                        unselectedColor = WfhTextMuted
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Site Selection for chosen Customer
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mine / Operating Site",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = WfhTextSecondary
                    )
                    TextButton(onClick = { showAddSiteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = WfhRedVibrant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Site", color = WfhRedVibrant, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                val customerSites = sites.filter { it.customerId == selectedCustomer?.id }
                if (customerSites.isEmpty()) {
                    Text(
                        text = "No sites registered for this customer.",
                        color = WfhTextMuted,
                        fontSize = 12.sp
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        customerSites.forEach { site ->
                            val isSelected = selectedSite?.id == site.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectSite(site) }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) WfhRedPrimary else WfhBorderSubtle,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) WfhSurfaceElevated else WfhSurfaceCard
                                ),
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
                                                text = site.siteName,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Text(
                                                text = "${site.propertyDescription}, ${site.location}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = WfhTextSecondary
                                            )
                                        }
                                        Surface(
                                            color = Color(0x33C4121A),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "DME: ${site.dmeMineCode}",
                                                color = Color(0xFFFFDAD6),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = WfhBorderSubtle)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Contacts details required by prompt
                                    Text(
                                        text = "• Mine Manager: ${site.mineManager} (${site.mineManagerContact})",
                                        fontSize = 11.sp,
                                        color = WfhTextSecondary
                                    )
                                    Text(
                                        text = "• Appointed Hygienist: ${site.siteOccupationalHygienist}",
                                        fontSize = 11.sp,
                                        color = WfhTextSecondary
                                    )
                                    Text(
                                        text = "• Medical Practitioner: ${site.occupationalMedicalPractitioner}",
                                        fontSize = 11.sp,
                                        color = WfhTextSecondary
                                    )
                                    Text(
                                        text = "• GPS: ${site.gpsCoordinates}",
                                        fontSize = 11.sp,
                                        color = WfhGold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Facility Selection
            item {
                Text(
                    text = "Facility / Location",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = WfhTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = facilityDropdownExpanded,
                    onExpandedChange = { facilityDropdownExpanded = !facilityDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedFacility,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = facilityDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WfhRedPrimary,
                            unfocusedBorderColor = WfhBorderSubtle,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = WfhSurfaceCard,
                            unfocusedContainerColor = WfhSurfaceCard
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = facilityDropdownExpanded,
                        onDismissRequest = { facilityDropdownExpanded = false },
                        modifier = Modifier.background(WfhSurfaceElevated)
                    ) {
                        facilities.forEach { fac ->
                            DropdownMenuItem(
                                text = { Text(fac, color = Color.White) },
                                onClick = {
                                    onSelectFacility(fac)
                                    facilityDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Start Assessment CTA
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onStartInspection,
                    enabled = selectedSite != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("launch_inspection_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Launch Inspection Wizard",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showAddSiteDialog && selectedCustomer != null) {
        var siteName by remember { mutableStateOf("") }
        var property by remember { mutableStateOf("") }
        var loc by remember { mutableStateOf("") }
        var dme by remember { mutableStateOf("") }
        var manager by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddSiteDialog = false },
            containerColor = WfhSurfaceElevated,
            title = { Text("Register New Mine Site", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = siteName,
                        onValueChange = { siteName = it },
                        label = { Text("Site / Mine Name") }
                    )
                    OutlinedTextField(
                        value = property,
                        onValueChange = { property = it },
                        label = { Text("Farm / Property Description") }
                    )
                    OutlinedTextField(
                        value = loc,
                        onValueChange = { loc = it },
                        label = { Text("Location & Province") }
                    )
                    OutlinedTextField(
                        value = dme,
                        onValueChange = { dme = it },
                        label = { Text("DME Mine Code") }
                    )
                    OutlinedTextField(
                        value = manager,
                        onValueChange = { manager = it },
                        label = { Text("Mine Manager & Contact") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (siteName.isNotEmpty()) {
                            onAddSite(
                                CustomerSite(
                                    id = "site_${System.currentTimeMillis()}",
                                    customerId = selectedCustomer.id,
                                    customerName = selectedCustomer.name,
                                    siteName = siteName,
                                    propertyDescription = property,
                                    location = loc,
                                    province = "Limpopo",
                                    dmeMineCode = dme.ifEmpty { "99999" },
                                    mineManager = manager.ifEmpty { "Site Lead" },
                                    mineManagerContact = "contact@site.co.za",
                                    siteOccupationalHygienist = "Appointed OH",
                                    occupationalMedicalPractitioner = "Appointed OMP"
                                )
                            )
                            showAddSiteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary)
                ) {
                    Text("Save Site")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSiteDialog = false }) {
                    Text("Cancel", color = WfhTextSecondary)
                }
            }
        )
    }
}

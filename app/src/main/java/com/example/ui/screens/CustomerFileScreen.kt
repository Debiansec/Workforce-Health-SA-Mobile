package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.*
import com.example.ui.components.WfhHeader
import com.example.ui.theme.*

@Composable
fun CustomerFileScreen(
    customerFile: CustomerOperationalFile = CustomerFileData.AFRIMAT_FILE,
    onStartAssessmentForCustomer: (CustomerSite) -> Unit = {},
    onBack: () -> Unit
) {
    var selectedSectionIndex by remember { mutableStateOf(0) }
    val sectionTabs = listOf(
        "Overview",
        "Legal & Structure",
        "Appointments",
        "HEG Registers",
        "TMM Fleet",
        "Emergency & Fire",
        "Statutory COPs"
    )

    Scaffold(
        containerColor = WfhDarkBackground,
        topBar = {
            WfhHeader(
                title = "Customer Operational File",
                subtitle = "${customerFile.customerName} • ${customerFile.documentNumber}",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable Tab Row for Document Sections
            ScrollableTabRow(
                selectedTabIndex = selectedSectionIndex,
                containerColor = WfhSurfaceCard,
                contentColor = Color.White,
                edgePadding = 16.dp
            ) {
                sectionTabs.forEachIndexed { index, tabTitle ->
                    Tab(
                        selected = selectedSectionIndex == index,
                        onClick = { selectedSectionIndex = index },
                        text = {
                            Text(
                                text = tabTitle,
                                fontSize = 12.sp,
                                fontWeight = if (selectedSectionIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSectionIndex == index) WfhRedVibrant else WfhTextSecondary
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedSectionIndex) {
                    0 -> {
                        // Section: Overview & Document Cover
                        item {
                            DocumentHeaderBanner(customerFile = customerFile)
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Mining Operation Summary",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = customerFile.operationDescription,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = WfhTextSecondary,
                                        lineHeight = 20.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = WfhBorderSubtle)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    InfoRowItem("Commodities Produced:", customerFile.commoditiesProduced)
                                    InfoRowItem("Degree of Mechanisation:", customerFile.mechanisationLevel)
                                    InfoRowItem("GPS Coordinates:", customerFile.gpsCoordinates, highlightColor = WfhGold)
                                    InfoRowItem("Property Description:", customerFile.propertyDescription)
                                    InfoRowItem("District & Province:", customerFile.locationDescription)
                                }
                            }
                        }

                        item {
                            ActionPromptCard(
                                title = "Launch Inspection for ${customerFile.siteName}",
                                subtitle = "Start ablution or occupational hygiene assessment linked to this file",
                                buttonText = "Conduct Assessment",
                                onAction = {
                                    val site = CustomerSite(
                                        id = "site_afrimat_kuipersbult",
                                        customerId = "cust_afrimat",
                                        customerName = customerFile.customerName,
                                        siteName = customerFile.siteName,
                                        propertyDescription = customerFile.propertyDescription,
                                        location = customerFile.locationDescription,
                                        province = "Limpopo",
                                        dmeMineCode = "26001",
                                        mineManager = customerFile.appointments.find { it.roleTitle == "Mine Manager" }?.let { "${it.personName} (${it.telephone})" } ?: "Mr P K Lekgeu",
                                        mineManagerContact = "peter.lekgeu@afrimat.co.za",
                                        siteOccupationalHygienist = customerFile.appointments.find { it.roleTitle.contains("Hygienist") }?.let { "${it.personName} (${it.email})" } ?: "Mr N B Letlape",
                                        occupationalMedicalPractitioner = "Dr K O Sebopa",
                                        gpsCoordinates = customerFile.gpsCoordinates
                                    )
                                    onStartAssessmentForCustomer(site)
                                }
                            )
                        }
                    }

                    1 -> {
                        // Section: Legal & Corporate Structure
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CorporateFare, contentDescription = null, tint = WfhRedVibrant)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "1. Legal Entity & Registration (Page 4)",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    InfoRowItem("Registered Legal Name:", customerFile.legalEntityName)
                                    InfoRowItem("Trading Name:", customerFile.registeredTradeName)
                                    InfoRowItem("Company Registration No:", customerFile.companyRegistrationNumber)
                                    InfoRowItem("VAT Registration No:", customerFile.vatRegistrationNumber)
                                    InfoRowItem("Physical Address (CIPC):", customerFile.registeredPhysicalAddress)
                                    InfoRowItem("Postal Address:", customerFile.registeredPostalAddress)

                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = WfhBorderSubtle)
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Apartment, contentDescription = null, tint = WfhGold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Control Group Information (Page 6-7)",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    InfoRowItem("Control Group Name:", customerFile.controlGroupName)
                                    InfoRowItem("Control Group Reg No:", customerFile.controlGroupRegNumber)
                                    InfoRowItem("Control Group VAT No:", customerFile.controlGroupVatNumber)
                                    InfoRowItem("Head Office Address:", customerFile.controlGroupAddress)

                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = WfhBorderSubtle)
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Gavel, contentDescription = null, tint = ComplianceGreenLight)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Mining Authorisations & Licences",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    InfoRowItem("Mining Right Number:", customerFile.miningRightNumber, highlightColor = ComplianceGreenLight)
                                    InfoRowItem("Water Use License No:", customerFile.waterLicenseNumber, highlightColor = ComplianceGreenLight)
                                }
                            }
                        }
                    }

                    2 -> {
                        // Section: Legal Appointments Directory (Pages 8-30)
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
                                    Icon(Icons.Default.Badge, contentDescription = null, tint = WfhRedVibrant)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Statutory Appointments Register (MHSA Act 29 of 1996)",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${customerFile.appointments.size} Formally Appointed Officials & Committee Members",
                                            fontSize = 11.sp,
                                            color = WfhTextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        items(customerFile.appointments) { appt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, WfhBorderSubtle, RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = appt.roleTitle,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Surface(
                                            color = WfhSurfaceElevated,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = appt.sectionRef,
                                                color = WfhGold,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = appt.personName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WfhRedVibrant
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Tel: ${appt.telephone} | Mob: ${appt.mobile}",
                                            fontSize = 11.sp,
                                            color = WfhTextSecondary
                                        )
                                    }

                                    Text(
                                        text = "Email: ${appt.email}",
                                        fontSize = 11.sp,
                                        color = WfhTextMuted
                                    )

                                    if (appt.appointmentDocRef != "Refer to letter of appointment") {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Doc Ref: ${appt.appointmentDocRef}",
                                            fontSize = 10.sp,
                                            color = ComplianceGreenLight
                                        )
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // Section: Homogeneous Exposure Groups (HEGs) (Pages 41-42)
                        item {
                            Surface(
                                color = WfhSurfaceCard,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Homogeneous Exposure Groups (HEGs) & Activity Codes",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Operational sampling and assessment zones per DMR codebook standards",
                                        fontSize = 11.sp,
                                        color = WfhTextSecondary
                                    )
                                }
                            }
                        }

                        items(customerFile.hegs) { heg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, WfhBorderSubtle, RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Area ${heg.areaNumber}: ${heg.areaDescription}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                        Surface(
                                            color = Color(0x33D32F2F),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Code ${heg.dmeCode}",
                                                color = Color(0xFFFFDAD6),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "HEG ${heg.hegNumber}: ${heg.description}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WfhGold
                                    )
                                    Text(
                                        text = "Activity Classification: ${heg.dmeDescription}",
                                        fontSize = 11.sp,
                                        color = WfhTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    4 -> {
                        // Section: Trackless Mobile Machines (TMM Inventory) (Annexure B, Page 126)
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
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = WfhGold)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Trackless Mobile Machines Inventory (Annexure B)",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${customerFile.mobileMachines.size} Active Mobile Units On-Site",
                                            fontSize = 11.sp,
                                            color = WfhTextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        items(customerFile.mobileMachines) { machine ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, WfhBorderSubtle, RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${machine.make} ${machine.model}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${machine.description} • Power: ${machine.powerKw}",
                                            fontSize = 11.sp,
                                            color = WfhTextSecondary
                                        )
                                    }

                                    Surface(
                                        color = WfhSurfaceElevated,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = machine.fleetNumber,
                                            fontWeight = FontWeight.Bold,
                                            color = WfhGold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    5 -> {
                        // Section: Emergency & Firefighting Equipment (Pages 115, 122)
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                                colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocalHospital, contentDescription = null, tint = CriticalRed)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Designated Emergency Medical Centres",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    customerFile.emergencyHospitals.forEach { hospital ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Emergency, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = hospital, fontSize = 12.sp, color = WfhTextSecondary)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Firefighting Equipment Register (Appendix A8.9)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }

                        items(customerFile.fireExtinguishers) { fire ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, WfhBorderSubtle, RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${fire.extinguisherCode}: ${fire.location}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = fire.typeAndSize,
                                            fontSize = 11.sp,
                                            color = WfhTextSecondary
                                        )
                                    }

                                    Surface(
                                        color = CriticalRedContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "#${fire.number}",
                                            color = CriticalRed,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    6 -> {
                        // Section: Statutory Codes of Practice (MCOPs, Page 98)
                        item {
                            Surface(
                                color = WfhSurfaceCard,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Statutory Mandatory Codes of Practice (Section 10)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Managed in an Integrated Operational Management System (IMS)",
                                        fontSize = 11.sp,
                                        color = WfhTextSecondary
                                    )
                                }
                            }
                        }

                        items(customerFile.mandatoryCOPs) { cop ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, WfhBorderSubtle, RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = ComplianceGreenLight, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = cop,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun DocumentHeaderBanner(customerFile: CustomerOperationalFile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14171E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = WfhRedPrimary,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "OFFICIAL CUSTOMER FILE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "Doc Ref: ${customerFile.documentNumber}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WfhGold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = customerFile.customerName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = Color.White
            )

            Text(
                text = "${customerFile.siteName} • ${customerFile.locationDescription}",
                style = MaterialTheme.typography.bodyMedium,
                color = WfhTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = WfhBorderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Drafting Date", fontSize = 10.sp, color = WfhTextMuted)
                    Text(customerFile.draftingDate, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Revision", fontSize = 10.sp, color = WfhTextMuted)
                    Text(customerFile.revision, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Next Revision", fontSize = 10.sp, color = WfhTextMuted)
                    Text(customerFile.nextRevisionDate, fontSize = 12.sp, color = WfhGold, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun InfoRowItem(label: String, value: String, highlightColor: Color? = null) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, color = WfhTextMuted)
        Text(
            text = value,
            fontSize = 12.sp,
            color = highlightColor ?: Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ActionPromptCard(
    title: String,
    subtitle: String,
    buttonText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WfhRedPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CriticalRedContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Text(subtitle, color = Color(0xFFFFDAD6), fontSize = 11.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

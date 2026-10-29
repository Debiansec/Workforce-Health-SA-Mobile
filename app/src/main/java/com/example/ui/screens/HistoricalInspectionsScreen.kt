package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.local.InspectionEntity
import com.example.data.util.CameraCaptureHelper
import com.example.data.util.InspectionExportHelper
import com.example.ui.components.WfhHeader
import com.example.ui.theme.*
import java.io.File
import java.util.Locale

enum class InspectionSortOrder(val displayName: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    SCORE_DESC("Highest Score"),
    SCORE_ASC("Lowest Score"),
    LOCATION_ASC("Location (A-Z)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricalInspectionsScreen(
    inspections: List<InspectionEntity>,
    onViewInspectionPdf: (InspectionEntity) -> Unit,
    onNavigateToNewForm: () -> Unit,
    onUpdateInspection: (InspectionEntity) -> Unit,
    onDeleteInspection: (Long) -> Unit,
    onNavigateToTrends: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") } // "ALL", "COMPLIANT", "PENDING", "NON_COMPLIANT"
    var selectedSortOrder by remember { mutableStateOf(InspectionSortOrder.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Dialog state
    var inspectionToDelete by remember { mutableStateOf<InspectionEntity?>(null) }
    var inspectionToEdit by remember { mutableStateOf<InspectionEntity?>(null) }
    var isExportingCsv by remember { mutableStateOf(false) }
    var viewingPhotoPath by remember { mutableStateOf<String?>(null) }

    // Filter and sort the Room database inspections
    val filteredInspections = remember(inspections, searchQuery, selectedStatusFilter, selectedSortOrder) {
        inspections.filter { item ->
            val matchesFilter = when (selectedStatusFilter) {
                "COMPLIANT" -> item.status.equals("COMPLIANT", ignoreCase = true)
                "PENDING" -> item.status.equals("PENDING", ignoreCase = true)
                "NON_COMPLIANT" -> item.status.equals("NON_COMPLIANT", ignoreCase = true)
                else -> true
            }

            val query = searchQuery.trim().lowercase()
            val matchesSearch = query.isEmpty() ||
                item.siteLocation.lowercase().contains(query) ||
                item.inspectorName.lowercase().contains(query) ||
                item.notes.lowercase().contains(query) ||
                item.date.lowercase().contains(query) ||
                item.status.lowercase().contains(query)

            matchesFilter && matchesSearch
        }.let { list ->
            when (selectedSortOrder) {
                InspectionSortOrder.DATE_DESC -> list.sortedByDescending { it.date }
                InspectionSortOrder.DATE_ASC -> list.sortedBy { it.date }
                InspectionSortOrder.SCORE_DESC -> list.sortedByDescending { it.safetyScore }
                InspectionSortOrder.SCORE_ASC -> list.sortedBy { it.safetyScore }
                InspectionSortOrder.LOCATION_ASC -> list.sortedBy { it.siteLocation }
            }
        }
    }

    val totalCount = inspections.size
    val compliantCount = inspections.count { it.status.equals("COMPLIANT", ignoreCase = true) }
    val pendingCount = inspections.count { it.status.equals("PENDING", ignoreCase = true) }
    val nonCompliantCount = inspections.count { it.status.equals("NON_COMPLIANT", ignoreCase = true) }
    val averageScore = if (totalCount > 0) inspections.map { it.safetyScore }.average() else 0.0

    Scaffold(
        containerColor = WfhDarkBackground,
        topBar = {
            WfhHeader(
                title = "Historical Inspections",
                subtitle = "Room DB Register • ${inspections.size} Audits Recorded",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewForm,
                containerColor = WfhRedPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_new_inspection")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Inspection")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Inspection", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // KPI Overview Summary Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "STATUTORY AUDIT REPOSITORY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WfhGold
                                )
                                Text(
                                    text = "Historical Occupational Hygiene Records",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (onNavigateToTrends != null) {
                                    OutlinedButton(
                                        onClick = { onNavigateToTrends() },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ComplianceGreenLight),
                                        border = BorderStroke(1.dp, ComplianceGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .height(38.dp)
                                            .testTag("historical_view_trends_button")
                                    ) {
                                        Icon(
                                            Icons.Default.TrendingUp,
                                            contentDescription = "Trends",
                                            tint = ComplianceGreenLight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Trends", color = ComplianceGreenLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // CSV Export Primary Button
                                Button(
                                    onClick = {
                                        isExportingCsv = true
                                        try {
                                            val csvFile = InspectionExportHelper.exportInspectionsToCsv(context, inspections)
                                            isExportingCsv = false
                                            InspectionExportHelper.shareCsvFile(context, csvFile, inspections.size)
                                        } catch (e: Exception) {
                                            isExportingCsv = false
                                            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WfhSurfaceElevated),
                                    border = BorderStroke(1.dp, WfhRedVibrant),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .height(38.dp)
                                        .testTag("export_all_csv_button")
                                ) {
                                    Icon(
                                        Icons.Default.FileDownload,
                                        contentDescription = "Export CSV",
                                        tint = WfhRedVibrant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export CSV", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats metrics row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatMetricPill(
                                label = "Total Audits",
                                value = totalCount.toString(),
                                accentColor = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            StatMetricPill(
                                label = "Avg Safety Score",
                                value = String.format(Locale.US, "%.1f%%", averageScore),
                                accentColor = if (averageScore >= 80) ComplianceGreenLight else ReviewAmber,
                                modifier = Modifier.weight(1.2f)
                            )
                            StatMetricPill(
                                label = "Compliant",
                                value = compliantCount.toString(),
                                accentColor = ComplianceGreenLight,
                                modifier = Modifier.weight(1f)
                            )
                            StatMetricPill(
                                label = "Pending",
                                value = pendingCount.toString(),
                                accentColor = ReviewAmber,
                                modifier = Modifier.weight(1f)
                            )
                            StatMetricPill(
                                label = "Non-compliant",
                                value = nonCompliantCount.toString(),
                                accentColor = CriticalRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Search Bar & Sort Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("historical_search_input"),
                        placeholder = { Text("Search site, inspector, notes...", fontSize = 13.sp, color = WfhTextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = WfhTextSecondary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = WfhTextSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = WfhSurfaceDark,
                            unfocusedContainerColor = WfhSurfaceDark,
                            focusedBorderColor = WfhRedPrimary,
                            unfocusedBorderColor = WfhBorderSubtle,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Sort dropdown button
                    Box {
                        OutlinedButton(
                            onClick = { showSortMenu = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, WfhBorderSubtle),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = WfhSurfaceDark, contentColor = Color.White),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("historical_sort_button")
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(selectedSortOrder.displayName.take(7), fontSize = 11.sp)
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier
                                .background(WfhSurfaceDark)
                                .border(1.dp, WfhBorderSubtle, RoundedCornerShape(8.dp))
                        ) {
                            InspectionSortOrder.values().forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = order.displayName,
                                            color = if (selectedSortOrder == order) WfhRedVibrant else Color.White,
                                            fontWeight = if (selectedSortOrder == order) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        selectedSortOrder = order
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Filter Chips Bar
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filterOptions = listOf(
                        "ALL" to "All Audits ($totalCount)",
                        "COMPLIANT" to "Compliant ($compliantCount)",
                        "PENDING" to "Pending ($pendingCount)",
                        "NON_COMPLIANT" to "Non-compliant ($nonCompliantCount)"
                    )

                    items(filterOptions) { (key, label) ->
                        val isSelected = selectedStatusFilter == key
                        val activeColor = when (key) {
                            "COMPLIANT" -> ComplianceGreen
                            "PENDING" -> ReviewAmber
                            "NON_COMPLIANT" -> CriticalRed
                            else -> WfhRedPrimary
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) activeColor.copy(alpha = 0.2f) else WfhSurfaceDark,
                            border = BorderStroke(1.dp, if (isSelected) activeColor else WfhBorderSubtle),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedStatusFilter = key }
                                .testTag("filter_chip_$key")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(activeColor)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else WfhTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historical Inspections (${filteredInspections.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = WfhTextSecondary
                    )

                    if (searchQuery.isNotEmpty() || selectedStatusFilter != "ALL") {
                        Text(
                            text = "Reset Filters",
                            fontSize = 12.sp,
                            color = WfhRedVibrant,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                searchQuery = ""
                                selectedStatusFilter = "ALL"
                            }
                        )
                    }
                }
            }

            // Empty State
            if (filteredInspections.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = WfhTextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Historical Inspections Found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your search query or status filter.",
                                fontSize = 12.sp,
                                color = WfhTextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    searchQuery = ""
                                    selectedStatusFilter = "ALL"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Clear Filters")
                            }
                        }
                    }
                }
            }

            // Scrollable List of Historical Inspection Cards
            items(filteredInspections, key = { it.id }) { inspection ->
                HistoricalInspectionCard(
                    inspection = inspection,
                    onViewPdf = { onViewInspectionPdf(inspection) },
                    onSharePdf = {
                        val pdfFile = InspectionExportHelper.generateInspectionPdf(context, inspection)
                        InspectionExportHelper.shareInspectionPdf(context, pdfFile, inspection)
                    },
                    onExportCsv = {
                        val csvFile = InspectionExportHelper.exportInspectionsToCsv(context, listOf(inspection))
                        InspectionExportHelper.shareCsvFile(context, csvFile, 1)
                    },
                    onEdit = { inspectionToEdit = inspection },
                    onDelete = { inspectionToDelete = inspection },
                    onViewPhoto = { viewingPhotoPath = it }
                )
            }

            item {
                Spacer(modifier = Modifier.height(60.dp)) // Padding for FAB
            }
        }
    }

    // Delete Confirmation Dialog
    inspectionToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { inspectionToDelete = null },
            containerColor = WfhSurfaceCard,
            title = {
                Text("Delete Inspection Record?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to permanently delete inspection #${item.id} for '${item.siteLocation}'? This cannot be undone.",
                    color = WfhTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteInspection(item.id)
                        inspectionToDelete = null
                        Toast.makeText(context, "Inspection #${item.id} deleted from Room DB", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectionToDelete = null }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    // Edit Inspection Dialog
    inspectionToEdit?.let { item ->
        EditInspectionDialog(
            inspection = item,
            onDismiss = { inspectionToEdit = null },
            onSave = { updated ->
                onUpdateInspection(updated)
                inspectionToEdit = null
                Toast.makeText(context, "Inspection #${updated.id} updated in Room DB", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Full Screen Photo Evidence Viewer Dialog
    viewingPhotoPath?.let { path ->
        AlertDialog(
            onDismissRequest = { viewingPhotoPath = null },
            modifier = Modifier.testTag("historical_photo_preview_dialog"),
            containerColor = WfhSurfaceCard,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Site Photo Evidence",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = { viewingPhotoPath = null }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black)
                    ) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = "Inspection Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = File(path).name,
                        fontSize = 11.sp,
                        color = WfhTextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewingPhotoPath = null },
                    colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close Preview")
                }
            }
        )
    }
}

@Composable
fun HistoricalInspectionCard(
    inspection: InspectionEntity,
    onViewPdf: () -> Unit,
    onSharePdf: () -> Unit,
    onExportCsv: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewPhoto: (String) -> Unit = {}
) {
    val statusColor = when (inspection.status.uppercase()) {
        "COMPLIANT" -> ComplianceGreen
        "PENDING" -> ReviewAmber
        else -> CriticalRed
    }

    val statusBg = when (inspection.status.uppercase()) {
        "COMPLIANT" -> ComplianceGreenContainer
        "PENDING" -> ReviewAmberContainer
        else -> CriticalRedContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WfhBorderSubtle, RoundedCornerShape(14.dp))
            .testTag("historical_inspection_card_${inspection.id}"),
        colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: ID + Date + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = WfhSurfaceElevated,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, WfhBorderSubtle)
                    ) {
                        Text(
                            text = "#${inspection.id}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WfhGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = inspection.date,
                        fontSize = 12.sp,
                        color = WfhTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, statusColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = inspection.status.replace("_", " ").uppercase(),
                            color = statusColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Site Location with building icon
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = WfhRedVibrant,
                    modifier = Modifier.size(18.dp).padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = inspection.siteLocation,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Row: Inspector + Safety Score Progress Gauge
            Surface(
                color = WfhSurfaceDark,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, WfhBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Inspector info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(WfhRedPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = inspection.inspectorName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Inspector / Assessor", fontSize = 10.sp, color = WfhTextSecondary)
                            Text(
                                text = inspection.inspectorName,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    // Safety Score Progress Badge
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Safety Score", fontSize = 10.sp, color = WfhTextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                progress = { (inspection.safetyScore / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.size(24.dp),
                                color = statusColor,
                                trackColor = statusBg,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f%%", inspection.safetyScore),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = statusColor
                            )
                        }
                    }
                }
            }

            // Observations & Notes if present
            if (inspection.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Notes,
                        contentDescription = null,
                        tint = WfhTextSecondary,
                        modifier = Modifier.size(15.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = inspection.notes,
                        fontSize = 11.5.sp,
                        color = Color(0xFFD1D5DB),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
            }

            // Photographic Evidence Strip if photos are attached
            val photoList = remember(inspection.photoUris) { inspection.getPhotoList() }
            if (photoList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Photos",
                            tint = WfhRedVibrant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Site Evidence (${photoList.size} Photos)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = "Tap to enlarge",
                        fontSize = 10.sp,
                        color = WfhTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(photoList.size) { pIndex ->
                        val pPath = photoList[pIndex]
                        Card(
                            modifier = Modifier
                                .size(width = 80.dp, height = 70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, WfhBorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { onViewPhoto(pPath) }
                                .testTag("historical_inspection_photo_thumb_${inspection.id}_$pIndex"),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = WfhSurfaceDark)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = File(pPath),
                                    contentDescription = "Site Photo ${pIndex + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(Color(0xAA000000), RoundedCornerShape(topStart = 4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "#${pIndex + 1}",
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = WfhBorderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: View PDF Form, Share PDF, Export CSV, More Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View PDF Form (Primary)
                Button(
                    onClick = onViewPdf,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(42.dp)
                        .testTag("card_view_pdf_${inspection.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = "View PDF",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF Viewer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Quick PDF Share
                OutlinedButton(
                    onClick = onSharePdf,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(42.dp)
                        .testTag("card_share_pdf_${inspection.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, WfhBorderSubtle)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share PDF",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 12.sp)
                }

                // Single CSV Export
                IconButton(
                    onClick = onExportCsv,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(WfhSurfaceElevated)
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(8.dp))
                        .testTag("card_csv_${inspection.id}")
                ) {
                    Icon(
                        Icons.Default.FileDownload,
                        contentDescription = "Export CSV",
                        tint = WfhGold,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Edit Button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(WfhSurfaceElevated)
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(8.dp))
                        .testTag("card_edit_${inspection.id}")
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CriticalRedContainer)
                        .border(1.dp, CriticalRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .testTag("card_delete_${inspection.id}")
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = CriticalRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatMetricPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = WfhSurfaceDark,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, WfhBorderSubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 9.sp, color = WfhTextSecondary, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditInspectionDialog(
    inspection: InspectionEntity,
    onDismiss: () -> Unit,
    onSave: (InspectionEntity) -> Unit
) {
    var siteLocation by remember { mutableStateOf(inspection.siteLocation) }
    var safetyScoreText by remember { mutableStateOf(inspection.safetyScore.toString()) }
    var date by remember { mutableStateOf(inspection.date) }
    var inspectorName by remember { mutableStateOf(inspection.inspectorName) }
    var status by remember { mutableStateOf(inspection.status) }
    var notes by remember { mutableStateOf(inspection.notes) }
    var attachedPhotos by remember { mutableStateOf(inspection.getPhotoList()) }

    val context = LocalContext.current
    var pendingFile by remember { mutableStateOf<File?>(null) }

    val editCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingFile
        if (success && file != null && file.exists()) {
            attachedPhotos = attachedPhotos + file.absolutePath
            Toast.makeText(context, "Site photo attached!", Toast.LENGTH_SHORT).show()
        }
        pendingFile = null
    }

    val editPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val (file, uri) = CameraCaptureHelper.createCaptureFile(context)
                pendingFile = file
                editCameraLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Camera launch failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WfhSurfaceCard,
        title = {
            Text("Edit Inspection #${inspection.id}", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = siteLocation,
                    onValueChange = { siteLocation = it },
                    label = { Text("Site Location") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = safetyScoreText,
                        onValueChange = { safetyScoreText = it },
                        label = { Text("Score (%)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1.3f)
                    )
                }

                OutlinedTextField(
                    value = inspectorName,
                    onValueChange = { inspectorName = it },
                    label = { Text("Inspector Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Status Selector
                Text("Compliance Status", fontSize = 11.sp, color = WfhTextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("COMPLIANT", "PENDING", "NON_COMPLIANT").forEach { s ->
                        val isSelected = status.equals(s, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) WfhRedPrimary else WfhSurfaceDark,
                            border = BorderStroke(1.dp, if (isSelected) WfhRedPrimary else WfhBorderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { status = s }
                        ) {
                            Text(
                                text = s.take(4),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Site Photos Row in Edit Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Site Photos (${attachedPhotos.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = {
                                val hasCam = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasCam) {
                                    try {
                                        val (file, uri) = CameraCaptureHelper.createCaptureFile(context)
                                        pendingFile = file
                                        editCameraLauncher.launch(uri)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    editPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp).testTag("edit_dialog_take_photo_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera", fontSize = 10.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val sample = CameraCaptureHelper.createSampleSitePhoto(context, siteLocation)
                                attachedPhotos = attachedPhotos + sample
                            },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("+ Sample", fontSize = 10.sp, color = WfhGold)
                        }
                    }
                }

                if (attachedPhotos.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(attachedPhotos.size) { pIndex ->
                            val path = attachedPhotos[pIndex]
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .border(1.dp, WfhBorderSubtle, RoundedCornerShape(6.dp))
                            ) {
                                AsyncImage(
                                    model = File(path),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                IconButton(
                                    onClick = {
                                        attachedPhotos = attachedPhotos.filterIndexed { i, _ -> i != pIndex }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(20.dp)
                                        .background(Color(0xCC000000), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = CriticalRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observations / Notes") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val score = safetyScoreText.toDoubleOrNull() ?: inspection.safetyScore
                    onSave(
                        inspection.copy(
                            siteLocation = siteLocation,
                            safetyScore = score,
                            date = date,
                            inspectorName = inspectorName,
                            status = status,
                            notes = notes,
                            photoUris = attachedPhotos.joinToString("|")
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )

    // Full Screen Photo Viewer inside Historical Screen
}

package com.example.ui.components

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
import com.example.data.local.InspectionEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InspectionStatusDashboard(
    inspections: List<InspectionEntity>,
    onCreateInspection: (siteLocation: String, safetyScore: Double, date: String, inspectorName: String, status: String, notes: String) -> Unit,
    onUpdateInspection: (InspectionEntity) -> Unit,
    onDeleteInspection: (Long) -> Unit,
    onOpenNewInspectionForm: (() -> Unit)? = null,
    onViewHistoricalList: (() -> Unit)? = null,
    onViewTrends: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingInspection by remember { mutableStateOf<InspectionEntity?>(null) }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "PENDING", "COMPLIANT", "NON_COMPLIANT"

    val total = inspections.size
    val pendingList = inspections.filter { it.status.equals("PENDING", ignoreCase = true) }
    val compliantList = inspections.filter { it.status.equals("COMPLIANT", ignoreCase = true) }
    val nonCompliantList = inspections.filter { it.status.equals("NON_COMPLIANT", ignoreCase = true) }

    val pendingCount = pendingList.size
    val compliantCount = compliantList.size
    val nonCompliantCount = nonCompliantList.size

    val pendingRatio = if (total > 0) pendingCount.toFloat() / total else 0f
    val compliantRatio = if (total > 0) compliantCount.toFloat() / total else 0f
    val nonCompliantRatio = if (total > 0) nonCompliantCount.toFloat() / total else 0f

    val averageScore = if (total > 0) {
        inspections.map { it.safetyScore }.average()
    } else 0.0

    val filteredList = when (selectedFilter) {
        "PENDING" -> pendingList
        "COMPLIANT" -> compliantList
        "NON_COMPLIANT" -> nonCompliantList
        else -> inspections
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, WfhBorderSubtle, RoundedCornerShape(16.dp))
            .testTag("inspection_status_dashboard"),
        colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(WfhRedPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "INSPECTION STATUS SUMMARY",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = WfhRedVibrant
                        )
                    }
                    Text(
                        text = "Safety Compliance & Audit Tracking",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = { onOpenNewInspectionForm?.invoke() ?: run { showAddDialog = true } },
                    modifier = Modifier
                        .size(38.dp)
                        .background(WfhRedPrimary, CircleShape)
                        .testTag("add_inspection_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Inspection",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CIRCULAR PROGRESS INDICATORS SECTION
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Pending Indicator
                CircularStatusIndicator(
                    label = "Pending",
                    count = pendingCount,
                    ratio = pendingRatio,
                    color = ReviewAmber,
                    trackColor = ReviewAmberContainer,
                    testTag = "indicator_pending"
                )

                // 2. Compliant Indicator
                CircularStatusIndicator(
                    label = "Compliant",
                    count = compliantCount,
                    ratio = compliantRatio,
                    color = ComplianceGreenLight,
                    trackColor = ComplianceGreenContainer,
                    testTag = "indicator_compliant"
                )

                // 3. Non-Compliant Indicator
                CircularStatusIndicator(
                    label = "Non-compliant",
                    count = nonCompliantCount,
                    ratio = nonCompliantRatio,
                    color = CriticalRed,
                    trackColor = CriticalRedContainer,
                    testTag = "indicator_non_compliant"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Metric Pill Bar
            Surface(
                color = WfhSurfaceDark,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Total",
                            tint = WfhTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Total Inspections: $total",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Avg Safety Score: ",
                            fontSize = 12.sp,
                            color = WfhTextSecondary
                        )
                        val scoreColor = when {
                            averageScore >= 80 -> ComplianceGreenLight
                            averageScore >= 65 -> ReviewAmber
                            else -> CriticalRed
                        }
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f%%", averageScore),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = scoreColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onOpenNewInspectionForm?.invoke() ?: run { showAddDialog = true } },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("open_new_inspection_form_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle)
                ) {
                    Icon(
                        imageVector = Icons.Default.PostAdd,
                        contentDescription = "New Form",
                        tint = WfhRedVibrant,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New Form",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (onViewHistoricalList != null) {
                    Button(
                        onClick = { onViewHistoricalList() },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("view_historical_inspections_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = WfhSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WfhRedVibrant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Historical List",
                            tint = WfhRedVibrant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "History & PDF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                if (onViewTrends != null) {
                    Button(
                        onClick = { onViewTrends() },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("view_safety_trends_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ComplianceGreenContainer),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ComplianceGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Safety Trends",
                            tint = ComplianceGreenLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Trends",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ComplianceGreenLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filters = listOf(
                    "ALL" to "All ($total)",
                    "PENDING" to "Pending ($pendingCount)",
                    "COMPLIANT" to "Compliant ($compliantCount)",
                    "NON_COMPLIANT" to "Non-compliant ($nonCompliantCount)"
                )
                filters.forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) WfhRedPrimary else WfhSurfaceElevated)
                            .clickable { selectedFilter = key }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else WfhTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // List of Inspections
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No inspections matching filter.",
                        fontSize = 12.sp,
                        color = WfhTextMuted
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredList.take(5).forEach { inspection ->
                        InspectionItemCard(
                            inspection = inspection,
                            onEdit = { editingInspection = inspection },
                            onDelete = { onDeleteInspection(inspection.id) }
                        )
                    }
                }
            }
        }
    }

    // Dialog for Adding New Inspection
    if (showAddDialog) {
        InspectionFormDialog(
            title = "Log New Inspection",
            initialSite = "Letaba Crushers — Tarentaalrant Quarry & Plant",
            initialScore = "85.0",
            initialDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            initialInspector = "David Nkosi",
            initialStatus = "COMPLIANT",
            initialNotes = "Routine inspection completed with positive occupational hygiene findings.",
            onDismiss = { showAddDialog = false },
            onSave = { site, score, date, inspector, status, notes ->
                onCreateInspection(site, score, date, inspector, status, notes)
                showAddDialog = false
            }
        )
    }

    // Dialog for Editing Inspection
    if (editingInspection != null) {
        val insp = editingInspection!!
        InspectionFormDialog(
            title = "Edit Inspection #${insp.id}",
            initialSite = insp.siteLocation,
            initialScore = insp.safetyScore.toString(),
            initialDate = insp.date,
            initialInspector = insp.inspectorName,
            initialStatus = insp.status,
            initialNotes = insp.notes,
            onDismiss = { editingInspection = null },
            onSave = { site, score, date, inspector, status, notes ->
                onUpdateInspection(
                    insp.copy(
                        siteLocation = site,
                        safetyScore = score,
                        date = date,
                        inspectorName = inspector,
                        status = status,
                        notes = notes
                    )
                )
                editingInspection = null
            }
        )
    }
}

@Composable
private fun CircularStatusIndicator(
    label: String,
    count: Int,
    ratio: Float,
    color: Color,
    trackColor: Color,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { ratio },
                modifier = Modifier.fillMaxSize(),
                color = color,
                trackColor = trackColor,
                strokeWidth = 7.dp
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(ratio * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
                Text(
                    text = "$count",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = "$count Total",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = WfhTextMuted
        )
    }
}

@Composable
private fun InspectionItemCard(
    inspection: InspectionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = WfhSurfaceDark,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = inspection.siteLocation,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${inspection.date} • Inspector: ${inspection.inspectorName}",
                        fontSize = 11.sp,
                        color = WfhTextSecondary
                    )
                }

                // Safety Score Badge
                val (scoreBg, scoreFg) = when {
                    inspection.safetyScore >= 80 -> Pair(ComplianceGreenContainer, ComplianceGreenLight)
                    inspection.safetyScore >= 65 -> Pair(ReviewAmberContainer, ReviewAmber)
                    else -> Pair(CriticalRedContainer, CriticalRed)
                }

                Surface(
                    color = scoreBg,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Text(
                        text = "${inspection.safetyScore.toInt()}% Score",
                        color = scoreFg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Pill
                val (statusText, statusBg, statusColor) = when (inspection.status.uppercase()) {
                    "COMPLIANT" -> Triple("Compliant", ComplianceGreenContainer, ComplianceGreenLight)
                    "NON_COMPLIANT" -> Triple("Non-compliant", CriticalRedContainer, CriticalRed)
                    else -> Triple("Pending Review", ReviewAmberContainer, ReviewAmber)
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = WfhTextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = CriticalRed,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            if (inspection.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notes: ${inspection.notes}",
                    fontSize = 11.sp,
                    color = WfhTextMuted,
                    maxLines = 2
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InspectionFormDialog(
    title: String,
    initialSite: String,
    initialScore: String,
    initialDate: String,
    initialInspector: String,
    initialStatus: String,
    initialNotes: String,
    onDismiss: () -> Unit,
    onSave: (site: String, score: Double, date: String, inspector: String, status: String, notes: String) -> Unit
) {
    var siteLocation by remember { mutableStateOf(initialSite) }
    var safetyScore by remember { mutableStateOf(initialScore) }
    var date by remember { mutableStateOf(initialDate) }
    var inspectorName by remember { mutableStateOf(initialInspector) }
    var status by remember { mutableStateOf(initialStatus) }
    var notes by remember { mutableStateOf(initialNotes) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    val statusOptions = listOf(
        "PENDING" to "Pending",
        "COMPLIANT" to "Compliant",
        "NON_COMPLIANT" to "Non-compliant"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WfhSurfaceElevated,
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = siteLocation,
                    onValueChange = { siteLocation = it },
                    label = { Text("Site Location") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = safetyScore,
                        onValueChange = { safetyScore = it },
                        label = { Text("Safety Score (0-100)") },
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = inspectorName,
                    onValueChange = { inspectorName = it },
                    label = { Text("Inspector Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Status dropdown
                ExposedDropdownMenuBox(
                    expanded = statusDropdownExpanded,
                    onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = statusOptions.find { it.first.equals(status, ignoreCase = true) }?.second ?: status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = statusDropdownExpanded,
                        onDismissRequest = { statusDropdownExpanded = false },
                        modifier = Modifier.background(WfhSurfaceCard)
                    ) {
                        statusOptions.forEach { (key, display) ->
                            DropdownMenuItem(
                                text = { Text(display, color = Color.White) },
                                onClick = {
                                    status = key
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Inspection Notes / Findings") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val scoreDouble = safetyScore.toDoubleOrNull() ?: 75.0
                    onSave(
                        siteLocation.ifEmpty { "Main Site" },
                        scoreDouble,
                        date.ifEmpty { "2026-10-26" },
                        inspectorName.ifEmpty { "David Nkosi" },
                        status,
                        notes
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary)
            ) {
                Text("Save Inspection", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = WfhTextSecondary)
            }
        }
    )
}

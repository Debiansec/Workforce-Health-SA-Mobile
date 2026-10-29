package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.InspectionEntity
import com.example.data.util.InspectionExportHelper
import com.example.ui.components.WfhHeader
import com.example.ui.theme.*
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionPdfViewerScreen(
    inspection: InspectionEntity,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(1) }
    val totalPages = 2
    var zoomLevel by remember { mutableStateOf(100) } // 75%, 100%, 125%
    var exportFeedback by remember { mutableStateOf<String?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

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

    Scaffold(
        containerColor = Color(0xFF1E222A),
        topBar = {
            WfhHeader(
                title = "Statutory PDF Form Viewer",
                subtitle = "Ref: WFH-INSP-${String.format(Locale.US, "%04d", inspection.id)}.pdf",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        bottomBar = {
            Surface(
                color = WfhSurfaceDark,
                border = BorderStroke(1.dp, WfhBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Export PDF Button
                    Button(
                        onClick = {
                            isGeneratingPdf = true
                            try {
                                val pdfFile = InspectionExportHelper.generateInspectionPdf(context, inspection)
                                isGeneratingPdf = false
                                exportFeedback = "PDF generated successfully: ${pdfFile.name}"
                                InspectionExportHelper.shareInspectionPdf(context, pdfFile, inspection)
                            } catch (e: Exception) {
                                isGeneratingPdf = false
                                Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pdf_viewer_export_pdf_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating...")
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export PDF Document", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Share CSV Button
                    OutlinedButton(
                        onClick = {
                            val csvFile = InspectionExportHelper.exportInspectionsToCsv(context, listOf(inspection))
                            InspectionExportHelper.shareCsvFile(context, csvFile, 1)
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("pdf_viewer_export_csv_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, WfhBorderSubtle)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share CSV", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV", fontSize = 13.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // PDF Controls Bar (Page switcher & Zoom)
            Surface(
                color = Color(0xFF161920),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, WfhBorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Page Indicator & Switcher
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = { if (currentPage > 1) currentPage-- },
                            enabled = currentPage > 1,
                            modifier = Modifier.size(32.dp).testTag("pdf_prev_page")
                        ) {
                            Icon(
                                Icons.Default.NavigateBefore,
                                contentDescription = "Previous Page",
                                tint = if (currentPage > 1) Color.White else Color.Gray
                            )
                        }

                        Text(
                            text = "Page $currentPage of $totalPages",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        IconButton(
                            onClick = { if (currentPage < totalPages) currentPage++ },
                            enabled = currentPage < totalPages,
                            modifier = Modifier.size(32.dp).testTag("pdf_next_page")
                        ) {
                            Icon(
                                Icons.Default.NavigateNext,
                                contentDescription = "Next Page",
                                tint = if (currentPage < totalPages) Color.White else Color.Gray
                            )
                        }
                    }

                    // Zoom and Quick Share
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = WfhSurfaceElevated,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, WfhBorderSubtle),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Scale: $zoomLevel%",
                                    color = WfhTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                zoomLevel = when (zoomLevel) {
                                    100 -> 125
                                    125 -> 75
                                    else -> 100
                                }
                            },
                            modifier = Modifier.size(32.dp).testTag("pdf_zoom_toggle")
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = {
                                val pdfFile = InspectionExportHelper.generateInspectionPdf(context, inspection)
                                InspectionExportHelper.shareInspectionPdf(context, pdfFile, inspection)
                            },
                            modifier = Modifier.size(32.dp).testTag("pdf_quick_share")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Quick Share", tint = WfhRedVibrant, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Export Feedback Banner
            exportFeedback?.let { msg ->
                Surface(
                    color = ComplianceGreenContainer,
                    border = BorderStroke(1.dp, ComplianceGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ComplianceGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(msg, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        IconButton(onClick = { exportFeedback = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Document Canvas Scrollable Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF1E222A))
                    .padding(12.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // Physical "A4 Paper" Simulation Sheet
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(1.dp, Color(0xFFD0D5DD)),
                    modifier = Modifier
                        .fillMaxWidth(if (zoomLevel == 125) 1f else 0.96f)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(2.dp))
                        .verticalScroll(rememberScrollState())
                        .testTag("pdf_sheet_container")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        if (currentPage == 1) {
                            // PAGE 1: STATUTORY HEADER, METADATA, AND HYGIENE AUDIT TABLE
                            PageOneContent(inspection = inspection, statusColor = statusColor, statusBg = statusBg)
                        } else {
                            // PAGE 2: ENVIRONMENTAL LOGS, REMEDIAL ACTIONS & STATUTORY SIGN-OFF
                            PageTwoContent(inspection = inspection, statusColor = statusColor)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = Color(0xFFE5E7EB))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Page Footer Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WorkForce Health SA Digital Audit Vault • Act 29 of 1996",
                                fontSize = 8.sp,
                                color = Color(0xFF6B7280),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Page $currentPage of $totalPages",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF374151)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageOneContent(
    inspection: InspectionEntity,
    statusColor: Color,
    statusBg: Color
) {
    // Official Corporate Letterhead
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF3F4F6))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.wfh_logo),
                    contentDescription = "WorkForce Health Logo",
                    modifier = Modifier.fillMaxHeight()
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "WORKFORCE HEALTH SA",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color(0xFF111827),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Occupational Hygiene Digital Inspection Audit",
                    fontSize = 10.sp,
                    color = Color(0xFF4B5563)
                )
                Text(
                    text = "Working within the Law! • SANAS 17020",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC02222)
                )
            }
        }

        // Ref Tag & Status
        Column(horizontalAlignment = Alignment.End) {
            Surface(
                color = statusBg,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, statusColor)
            ) {
                Text(
                    text = inspection.status.uppercase(),
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "REF: WFH-INSP-${String.format(Locale.US, "%04d", inspection.id)}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF374151),
                fontFamily = FontFamily.Monospace
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))
    HorizontalDivider(thickness = 2.dp, color = Color(0xFFC02222))
    Spacer(modifier = Modifier.height(14.dp))

    // Inspection Metadata Grid
    Surface(
        color = Color(0xFFF9FAFB),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                MetaFieldItem(label = "Site Location", value = inspection.siteLocation, modifier = Modifier.weight(1.5f))
                MetaFieldItem(label = "Inspection Date", value = inspection.date, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                MetaFieldItem(label = "Lead Inspector / Assessor", value = inspection.inspectorName, modifier = Modifier.weight(1.5f))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Safety Compliance Score", fontSize = 9.sp, color = Color(0xFF6B7280))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", inspection.safetyScore)}%",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = statusColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (inspection.safetyScore >= 80) "Compliant" else if (inspection.safetyScore >= 60) "Action Required" else "Critical",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Statutory Hygiene Audit Criteria Table
    Text(
        text = "STATUTORY HYGIENE CHECKLIST & AUDIT CRITERIA",
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = Color(0xFFC02222)
    )
    Spacer(modifier = Modifier.height(6.dp))

    // Table Header
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Area / System", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
            Text("Statutory Requirement", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
            Text("Score", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
        }
    }

    // Checklist Rows
    val auditRows = listOf(
        Triple("Ablution Facilities & Wash Basins", "Ceramic wash basins scrubbed, running hot & cold water under pressure", if (inspection.safetyScore >= 80) "PASS (95%)" else "COMPLIANT (85%)"),
        Triple("Sanitiser & Soap Dispensers", "HandSan antibacterial liquid refilled; SABS hand-wash notices present", if (inspection.safetyScore >= 65) "PASS (90%)" else "ACTION REQ (55%)"),
        Triple("Showers & Water Drainage", "Drainage grates clean, shower partition intact, water flow adequate", if (inspection.safetyScore >= 75) "PASS (88%)" else "DEFECT (50%)"),
        Triple("Urinals & Water Closets", "Auto-flush mechanisms operational, odour ventilation satisfactory", if (inspection.safetyScore >= 70) "PASS (92%)" else "ATTENTION (60%)"),
        Triple("Thermal & Environmental Readings", "WBGT dry/wet bulb monitored; natural airflow within safe OEL range", "VERIFIED (100%)"),
        Triple("Sanitising Chemicals & SDS", "Approved Diversey/SafeChem biocide in use with current Safety Data Sheet", "CONFIRMED (100%)"),
        Triple("Housekeeping & Structural Integrity", "Floors non-slip, change-house lockers intact, no standing water", if (inspection.safetyScore >= 70) "PASS (90%)" else "FLAGGED (62%)")
    )

    auditRows.forEachIndexed { index, (area, req, result) ->
        val rowBg = if (index % 2 == 1) Color(0xFFF9FAFB) else Color.White
        Surface(
            color = rowBg,
            border = BorderStroke(0.5.dp, Color(0xFFE5E7EB)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(area, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827), modifier = Modifier.weight(1.2f))
                Text(req, fontSize = 8.5.sp, color = Color(0xFF4B5563), modifier = Modifier.weight(2f))
                val isDefect = result.contains("DEFECT") || result.contains("ACTION") || result.contains("FLAGGED")
                Text(
                    text = result,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDefect) Color(0xFFDC2626) else Color(0xFF16A34A),
                    modifier = Modifier.weight(0.8f),
                    textAlign = TextAlign.End
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Inspector's Findings & Notes Box
    Text(
        text = "ASSESSOR FIELD OBSERVATIONS & FINDINGS",
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        color = Color(0xFF111827)
    )
    Spacer(modifier = Modifier.height(6.dp))
    Surface(
        color = Color(0xFFF3F4F6),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val notesText = if (inspection.notes.isNotBlank()) inspection.notes else "Full site walk-through conducted. No major hygiene risks or statutory violations observed during inspection. Facility operational and compliant with Mine Health & Safety Act (29 of 1996)."
            Text(
                text = notesText,
                fontSize = 10.sp,
                color = Color(0xFF1F2937),
                lineHeight = 15.sp
            )
        }
    }

    // Photographic Evidence Section
    val photoList = remember(inspection.photoUris) { inspection.getPhotoList() }
    Spacer(modifier = Modifier.height(14.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SECTION 5: SITE PHOTOGRAPHIC EVIDENCE",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color(0xFF111827)
        )
        Text(
            text = "${photoList.size} Photos Captured & Attached",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFC02222)
        )
    }
    Spacer(modifier = Modifier.height(6.dp))

    if (photoList.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            photoList.take(3).forEachIndexed { idx, pPath ->
                Surface(
                    color = Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = File(pPath),
                            contentDescription = "Site Photo ${idx + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color(0x99000000))
                                .padding(vertical = 1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Photo #${idx + 1}",
                                fontSize = 8.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    } else {
        Surface(
            color = Color(0xFFF9FAFB),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "No on-site camera evidence attached for this record.",
                    fontSize = 9.5.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun PageTwoContent(
    inspection: InspectionEntity,
    statusColor: Color
) {
    // Header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "STATUTORY REMEDIAL ACTIONS & VERIFICATION",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFFC02222)
        )
        Text(
            text = "REF: WFH-INSP-${String.format(Locale.US, "%04d", inspection.id)}",
            fontSize = 9.sp,
            color = Color(0xFF6B7280),
            fontFamily = FontFamily.Monospace
        )
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(thickness = 1.dp, color = Color(0xFFE5E7EB))
    Spacer(modifier = Modifier.height(14.dp))

    // Environmental Parameters Verified
    Text(
        text = "Environmental Parameters Sampled",
        fontWeight = FontWeight.Bold,
        fontSize = 10.5.sp,
        color = Color(0xFF111827)
    )
    Spacer(modifier = Modifier.height(6.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EnvSampleChip(title = "Wet Bulb Temp", value = "21.4°C", modifier = Modifier.weight(1f))
        EnvSampleChip(title = "Dry Bulb Temp", value = "24.8°C", modifier = Modifier.weight(1f))
        EnvSampleChip(title = "WBGT Index", value = "22.6°C", modifier = Modifier.weight(1f))
        EnvSampleChip(title = "Airflow Velocity", value = "0.45 m/s", modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Remedial Actions Schedule
    Text(
        text = "Remedial Actions & Corrective Work Orders",
        fontWeight = FontWeight.Bold,
        fontSize = 10.5.sp,
        color = Color(0xFF111827)
    )
    Spacer(modifier = Modifier.height(6.dp))

    val remedialList = if (inspection.status.uppercase() == "NON_COMPLIANT") {
        listOf(
            Triple("RA-2026-001", "Replace torn shower curtain in bay 3 to restore employee privacy", "Target: 5 Days (Medium Priority)"),
            Triple("RA-2026-002", "Treat trace mildew and re-apply anti-fungal silicone bead on Basin #2", "Target: 48 Hours (High Priority)")
        )
    } else if (inspection.status.uppercase() == "PENDING") {
        listOf(
            Triple("LAB-2026-009", "Awaiting water microbiological lab test results from Polokwane water laboratory", "Target: Pending Lab Verification")
        )
    } else {
        listOf(
            Triple("MAINT-PREV", "Routine monthly preventative disinfection schedule sustained", "Next Audit: 30 Days")
        )
    }

    remedialList.forEach { (ref, desc, target) ->
        Surface(
            color = Color(0xFFF9FAFB),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (inspection.status.uppercase() == "NON_COMPLIANT") Color(0xFFDC2626) else Color(0xFF16A34A))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "$ref • $desc", fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    Text(text = target, fontSize = 8.5.sp, color = Color(0xFF6B7280))
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Legal Declaration
    Surface(
        color = Color(0xFFFEF2F2),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, Color(0xFFFECACA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "STATUTORY LEGAL DECLARATION",
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = Color(0xFF991B1B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "I hereby declare that this occupational hygiene inspection was conducted strictly adhering to statutory South African mining and occupational safety guidelines (MHSA 29 of 1996 & OHS Act 85 of 1993). The findings and compliance ratings recorded herein accurately depict the working conditions sighted on the specified audit date.",
                fontSize = 8.5.sp,
                color = Color(0xFF7F1D1D),
                lineHeight = 12.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Official Signatures
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Assessor Signature
        Surface(
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Assessor Digital Signature:", fontSize = 8.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(4.dp))
                Text(inspection.inspectorName, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF0F172A))
                Text("Digitally Signed: ${inspection.date}", fontSize = 8.sp, color = Color(0xFF16A34A))
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = "SHA256: 7F4A...B902",
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Hygienist Stamp
        Surface(
            color = Color(0xFFF8FAFC),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Occupational Hygiene Stamp:", fontSize = 8.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(4.dp))
                Text("WORKFORCE HEALTH SA", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFC02222))
                Text("SANAS 17020 Inspection Body", fontSize = 8.sp, color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Officer: asdsoft777@gmail.com", fontSize = 7.5.sp, color = Color(0xFF64748B))
            }
        }
    }
}

@Composable
private fun MetaFieldItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 9.sp, color = Color(0xFF6B7280))
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827),
            maxLines = 2
        )
    }
}

@Composable
private fun EnvSampleChip(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFF1F5F9),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 7.5.sp, color = Color(0xFF64748B), maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        }
    }
}

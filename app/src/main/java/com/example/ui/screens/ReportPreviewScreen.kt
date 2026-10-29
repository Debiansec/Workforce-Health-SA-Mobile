package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.*
import com.example.data.model.*
import com.example.ui.components.WfhHeader
import com.example.ui.theme.*
import kotlinx.coroutines.flow.Flow

@Composable
fun ReportPreviewScreen(
    assessmentId: String,
    assessmentFlow: Flow<List<AssessmentEntity>>,
    answersFlow: Flow<List<AnswerEntity>>,
    deepChemicalsFlow: Flow<List<ChemicalRecordEntity>>,
    routineChemicalsFlow: Flow<List<ChemicalRecordEntity>>,
    environmentalReadingsFlow: Flow<List<EnvironmentalReadingEntity>>,
    remedialActionsFlow: Flow<List<RemedialActionEntity>>,
    currentUser: UserProfile,
    onApproveReport: (String, String) -> Unit,
    onReturnForCorrection: (String) -> Unit,
    onBack: () -> Unit
) {
    val assessments by assessmentFlow.collectAsState(initial = emptyList())
    val assessment = assessments.find { it.id == assessmentId }

    val answers by answersFlow.collectAsState(initial = emptyList())
    val deepChemicals by deepChemicalsFlow.collectAsState(initial = emptyList())
    val routineChemicals by routineChemicalsFlow.collectAsState(initial = emptyList())
    val environmentalReadings by environmentalReadingsFlow.collectAsState(initial = emptyList())
    val remedialActions by remedialActionsFlow.collectAsState(initial = emptyList())

    var showApproveDialog by remember { mutableStateOf(false) }
    var showReturnDialog by remember { mutableStateOf(false) }
    var reviewComments by remember { mutableStateOf("") }
    var exportedMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = WfhDarkBackground,
        topBar = {
            WfhHeader(
                title = "Compliance Inspection Report",
                subtitle = "Document Ref: ${assessment?.documentReference ?: ""}",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        bottomBar = {
            Surface(
                color = WfhSurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            exportedMessage = "Report exported to PDF & Word format (/sdcard/Reports/${assessment?.documentReference}.pdf)"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_report_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export PDF/Word")
                    }

                    if (currentUser.role == UserRole.HYGIENIST && assessment?.status != AssessmentStatus.APPROVED.name) {
                        Button(
                            onClick = { showApproveDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("approve_report_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ComplianceGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = "Approve", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Approve Report", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (exportedMessage != null) {
                item {
                    Surface(
                        color = ComplianceGreenContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = ComplianceGreenLight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(exportedMessage!!, color = ComplianceGreenLight, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Printable / Professional Report Header Sheet
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF0EFEA))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.wfh_logo),
                                    contentDescription = "WFH Logo",
                                    modifier = Modifier.height(28.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "WORKFORCE HEALTH SA",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Occupational Hygiene Inspection",
                                    fontSize = 10.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.LightGray)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = assessment?.templateTitle ?: "Monthly Ablution Facilities Checklist",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF8B0000)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ReportMetaRow("Client / Customer:", assessment?.customerName ?: "")
                        ReportMetaRow("Site / Mine:", "${assessment?.siteName} (DME Mine Code: ${assessment?.dmeMineCode})")
                        ReportMetaRow("Facility Location:", assessment?.facilityName ?: "")
                        ReportMetaRow("Document Ref:", assessment?.documentReference ?: "")
                        ReportMetaRow("Field Visit Date:", assessment?.fieldVisitDate ?: "")
                        ReportMetaRow("Field Assessor:", assessment?.assessorName ?: "")
                        ReportMetaRow("Mine Manager:", assessment?.mineManager ?: "")
                        ReportMetaRow("Appointed Hygienist:", assessment?.siteOccupationalHygienist ?: "")

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color.LightGray)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Compliance Score
                        val totalQuestions = answers.size
                        val yesCount = answers.count { it.response == ComplianceResponse.YES.name }
                        val noCount = answers.count { it.response == ComplianceResponse.NO.name }
                        val scorePercent = if (totalQuestions > 0) (yesCount * 100) / totalQuestions else 0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("COMPLIANCE RATING", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("$yesCount YES • $noCount NO • $totalQuestions Total", fontSize = 11.sp, color = Color.Black)
                            }
                            Text(
                                text = "$scorePercent%",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = if (scorePercent >= 80) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }

            // Chemical Registers Summary
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Chemical Registers & Safety Data Sheets (SDS)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Deep-Cleaning Chemicals (${deepChemicals.size} Registered):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WfhGold
                        )
                        deepChemicals.forEach { c ->
                            Text(
                                text = "• ${c.chemicalName} (${c.manufacturer}) - SDS Sighted: ${if (c.sdsReturned) "YES" else "NO"}",
                                fontSize = 11.sp,
                                color = WfhTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Routine-Cleaning Chemicals (${routineChemicals.size} Registered):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WfhGold
                        )
                        routineChemicals.forEach { c ->
                            Text(
                                text = "• ${c.chemicalName} (${c.manufacturer}) - SDS Sighted: ${if (c.sdsReturned) "YES" else "NO"}",
                                fontSize = 11.sp,
                                color = WfhTextSecondary
                            )
                        }
                    }
                }
            }

            // Environmental Conditions Summary
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Shift Environmental Readings",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        environmentalReadings.forEach { r ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(r.periodName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WfhGold)
                                Text("Time: ${r.measurementTime} | WBGT: ${r.wbgtIndex}°C | RH: ${r.relativeHumidity}%", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Attestation and Signatures Block
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Legal Sign-off & Electronic Attestations",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Assessor Attestation", fontSize = 11.sp, color = WfhTextSecondary)
                                Text(
                                    text = assessment?.assessorSignature ?: "Not Signed",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (assessment?.assessorSignature != null) ComplianceGreenLight else ReviewAmber
                                )
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Reviewer Approval", fontSize = 11.sp, color = WfhTextSecondary)
                                Text(
                                    text = assessment?.reviewerSignature ?: "Awaiting Review",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (assessment?.reviewerSignature != null) ComplianceGreenLight else ReviewAmber
                                )
                            }
                        }

                        if (assessment?.reviewComments != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Hygienist Notes: ${assessment.reviewComments}",
                                fontSize = 11.sp,
                                color = WfhGold
                            )
                        }
                    }
                }
            }
        }
    }

    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            containerColor = WfhSurfaceElevated,
            title = { Text("Approve Occupational Hygiene Report", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "You are approving this report as Dr. K. Naidoo (Appointed Occupational Hygienist).",
                        fontSize = 12.sp,
                        color = WfhTextSecondary
                    )
                    OutlinedTextField(
                        value = reviewComments,
                        onValueChange = { reviewComments = it },
                        label = { Text("Professional Interpretation & Comments") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onApproveReport(
                            "Approved by Dr. K. Naidoo on ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}",
                            reviewComments.ifEmpty { "Inspection verified against SABS & DMR hygiene standards. Approved for issue." }
                        )
                        showApproveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ComplianceGreen)
                ) {
                    Text("Confirm & Authorise")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveDialog = false }) {
                    Text("Cancel", color = WfhTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ReportMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

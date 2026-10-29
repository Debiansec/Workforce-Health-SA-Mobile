package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.AssessmentEntity
import com.example.data.local.EquipmentEntity
import com.example.data.local.InspectionEntity
import com.example.data.local.RemedialActionEntity
import com.example.data.model.AssessmentStatus
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.components.InspectionStatusDashboard
import com.example.ui.components.WfhHeader
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    user: UserProfile,
    assessments: List<AssessmentEntity>,
    remedialActions: List<RemedialActionEntity>,
    equipmentList: List<EquipmentEntity>,
    inspections: List<InspectionEntity> = emptyList(),
    isOnline: Boolean,
    isSyncing: Boolean,
    onSyncClick: () -> Unit,
    onToggleOnline: () -> Unit,
    onStartNewAssessment: () -> Unit,
    onContinueAssessment: (String) -> Unit,
    onOpenReport: (String) -> Unit,
    onViewRemedialActions: () -> Unit,
    onViewEquipment: () -> Unit,
    onViewTrips: () -> Unit,
    onViewProfile: () -> Unit,
    onSwitchRoleClick: () -> Unit,
    onViewCustomerFiles: () -> Unit = {},
    onCreateInspection: (siteLocation: String, safetyScore: Double, date: String, inspectorName: String, status: String, notes: String) -> Unit = { _, _, _, _, _, _ -> },
    onUpdateInspection: (InspectionEntity) -> Unit = {},
    onDeleteInspection: (Long) -> Unit = {},
    onNavigateToNewInspectionForm: () -> Unit = {},
    onViewHistoricalInspections: () -> Unit = {},
    onViewSafetyTrends: () -> Unit = {}
) {
    val inProgressAssessments = assessments.filter {
        it.status == AssessmentStatus.IN_PROGRESS.name || it.status == AssessmentStatus.DRAFT.name
    }
    val awaitingReviewAssessments = assessments.filter {
        it.status == AssessmentStatus.SUBMITTED_FOR_REVIEW.name
    }
    val completedAssessments = assessments.filter {
        it.status == AssessmentStatus.APPROVED.name
    }
    val openRemedialActions = remedialActions.filter { it.status == "OPEN" || it.status == "IN_PROGRESS" }

    Scaffold(
        containerColor = WfhDarkBackground,
        topBar = {
            WfhHeader(
                userRole = user.role,
                onProfileClick = onViewProfile,
                isOnline = isOnline,
                isSyncing = isSyncing,
                onSyncClick = onSyncClick,
                onToggleOnline = onToggleOnline,
                onNavigateToCustomerFiles = onViewCustomerFiles,
                onNavigateToDashboard = { /* Already on dashboard */ },
                onNavigateToNewAssessment = onStartNewAssessment,
                onNavigateToTrips = onViewTrips,
                onNavigateToRemedial = onViewRemedialActions,
                onNavigateToEquipment = onViewEquipment,
                onNavigateToHistoricalInspections = onViewHistoricalInspections,
                onNavigateToSafetyTrends = onViewSafetyTrends
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

                // Hero Graphic Banner from attached asset
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.hero_industrial),
                            contentDescription = "Industrial Safety at Sunset",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xCC0D0F12),
                                            Color(0xF00D0F12)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = WfhRedPrimary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = user.role.displayName.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "Working within the Law!",
                                    color = WfhGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Welcome, ${user.fullName}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        // Switch role button
                        IconButton(
                            onClick = onSwitchRoleClick,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(32.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch Role",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Real Operational KPI Stats Grid
            item {
                Text(
                    text = "Operational Assessment Metrics",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = WfhTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "In Progress",
                        count = inProgressAssessments.size.toString(),
                        icon = Icons.Default.PendingActions,
                        accentColor = ReviewAmber,
                        containerColor = ReviewAmberContainer
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "For Review",
                        count = awaitingReviewAssessments.size.toString(),
                        icon = Icons.Default.FactCheck,
                        accentColor = WfhRedVibrant,
                        containerColor = CriticalRedContainer
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Approved",
                        count = completedAssessments.size.toString(),
                        icon = Icons.Default.CheckCircle,
                        accentColor = ComplianceGreenLight,
                        containerColor = ComplianceGreenContainer
                    )
                }
            }

            // Quick Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onStartNewAssessment,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("start_new_assessment_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "New",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Inspection", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onViewTrips,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("track_trip_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Trip",
                            tint = WfhRedVibrant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Site Trips & GPS", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Inspection Status Summary with Circular Progress Indicators (Pending, Compliant, Non-compliant)
            item {
                InspectionStatusDashboard(
                    inspections = inspections,
                    onCreateInspection = onCreateInspection,
                    onUpdateInspection = onUpdateInspection,
                    onDeleteInspection = onDeleteInspection,
                    onOpenNewInspectionForm = onNavigateToNewInspectionForm,
                    onViewHistoricalList = onViewHistoricalInspections,
                    onViewTrends = onViewSafetyTrends
                )
            }

            // Module Navigation Cards
            item {
                Text(
                    text = "Registers & Hygiene Workflows",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = WfhTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorkflowTile(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_customer_files_tile"),
                        title = "Customer Files",
                        subtitle = "Afrimat Dossier (PDF)",
                        icon = Icons.Default.FolderShared,
                        tint = WfhRedVibrant,
                        onClick = onViewCustomerFiles
                    )
                    WorkflowTile(
                        modifier = Modifier.weight(1f),
                        title = "Remedial Actions",
                        subtitle = "${openRemedialActions.size} Open Issues",
                        icon = Icons.Default.WarningAmber,
                        tint = if (openRemedialActions.isNotEmpty()) CriticalRed else ComplianceGreenLight,
                        onClick = onViewRemedialActions
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorkflowTile(
                        modifier = Modifier.weight(1f),
                        title = "Equipment Register",
                        subtitle = "${equipmentList.size} Instruments",
                        icon = Icons.Default.BuildCircle,
                        tint = WfhGold,
                        onClick = onViewEquipment
                    )
                    WorkflowTile(
                        modifier = Modifier.weight(1f),
                        title = "Site GPS & Trips",
                        subtitle = "Kuipersbult & Mines",
                        icon = Icons.Default.Navigation,
                        tint = ReviewAmber,
                        onClick = onViewTrips
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorkflowTile(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_historical_inspections_tile"),
                        title = "Historical Audits",
                        subtitle = "Room DB (${inspections.size}) • PDF & CSV",
                        icon = Icons.Default.History,
                        tint = WfhRedVibrant,
                        onClick = onViewHistoricalInspections
                    )
                    WorkflowTile(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_safety_trends_tile"),
                        title = "Safety Trends",
                        subtitle = "Time-Series Chart • 75% Target",
                        icon = Icons.Default.TrendingUp,
                        tint = ComplianceGreenLight,
                        onClick = onViewSafetyTrends
                    )
                }
            }

            // Active Assessments List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Site Assessments",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "${assessments.size} Recorded",
                        style = MaterialTheme.typography.bodySmall,
                        color = WfhTextMuted
                    )
                }
            }

            if (assessments.isEmpty()) {
                item {
                    Surface(
                        color = WfhSurfaceCard,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AssignmentLate,
                                contentDescription = "Empty",
                                tint = WfhTextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Assessments Found",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Tap 'Start Inspection' to launch the Monthly Ablution Facilities Checklist.",
                                style = MaterialTheme.typography.bodySmall,
                                color = WfhTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(assessments) { item ->
                    AssessmentListItem(
                        assessment = item,
                        onContinue = { onContinueAssessment(item.id) },
                        onOpenReport = { onOpenReport(item.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    count: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color
) {
    Card(
        modifier = modifier.border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = count,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = WfhTextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WorkflowTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(WfhSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = WfhTextSecondary
                )
            }
        }
    }
}

@Composable
private fun AssessmentListItem(
    assessment: AssessmentEntity,
    onContinue: () -> Unit,
    onOpenReport: () -> Unit
) {
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
                        text = assessment.templateTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "${assessment.customerName} • ${assessment.facilityName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = WfhTextSecondary
                    )
                }

                val (statusColor, containerColor) = when (assessment.status) {
                    AssessmentStatus.APPROVED.name -> Pair(ComplianceGreenLight, ComplianceGreenContainer)
                    AssessmentStatus.SUBMITTED_FOR_REVIEW.name -> Pair(WfhRedVibrant, CriticalRedContainer)
                    else -> Pair(ReviewAmber, ReviewAmberContainer)
                }

                Surface(
                    color = containerColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = assessment.status.replace("_", " "),
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = { assessment.progressPercent / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (assessment.progressPercent == 100) ComplianceGreenLight else WfhRedPrimary,
                    trackColor = WfhSurfaceElevated,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${assessment.progressPercent}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ref: ${assessment.documentReference} • ${assessment.fieldVisitDate}",
                    fontSize = 11.sp,
                    color = WfhTextMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onOpenReport,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Report",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Report", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onContinue,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (assessment.progressPercent == 100) "Review" else "Continue",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.graphics.Paint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InspectionEntity
import com.example.ui.components.WfhHeader
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.abs

enum class TrendTimeFilter(val label: String) {
    ALL_TIME("All Time"),
    LAST_30_DAYS("Last 30 Days"),
    LAST_90_DAYS("Last 90 Days")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyTrendAnalyticsScreen(
    inspections: List<InspectionEntity>,
    onViewInspectionPdf: (InspectionEntity) -> Unit,
    onNavigateToNewInspection: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedTimeFilter by remember { mutableStateOf(TrendTimeFilter.ALL_TIME) }
    var selectedSiteFilter by remember { mutableStateOf("ALL") }
    var selectedComplianceFilter by remember { mutableStateOf("ALL") } // "ALL", "COMPLIANT", "NON_COMPLIANT"

    // Extract unique sites from the database for filtering
    val uniqueSites = remember(inspections) {
        inspections.map { it.siteLocation.trim() }.distinct().sorted()
    }

    // Filter and chronologically sort inspections for the time-series chart
    val sortedFilteredInspections = remember(
        inspections,
        selectedTimeFilter,
        selectedSiteFilter,
        selectedComplianceFilter
    ) {
        inspections.filter { item ->
            val matchesSite = selectedSiteFilter == "ALL" || item.siteLocation.trim() == selectedSiteFilter
            val matchesCompliance = when (selectedComplianceFilter) {
                "COMPLIANT" -> item.safetyScore >= 75.0 || item.status.equals("COMPLIANT", ignoreCase = true)
                "NON_COMPLIANT" -> item.safetyScore < 75.0 || item.status.equals("NON_COMPLIANT", ignoreCase = true)
                else -> true
            }
            matchesSite && matchesCompliance
        }.sortedBy { it.date }
    }

    // Selected point index on the chart for interactive tooltip
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    // Reset selected point when data changes
    LaunchedEffect(sortedFilteredInspections) {
        if (sortedFilteredInspections.isNotEmpty()) {
            selectedPointIndex = sortedFilteredInspections.lastIndex
        } else {
            selectedPointIndex = null
        }
    }

    // Calculate Key Performance Metrics
    val totalInspections = sortedFilteredInspections.size
    val averageScore = if (totalInspections > 0) {
        sortedFilteredInspections.map { it.safetyScore }.average()
    } else 0.0

    val compliantCount = sortedFilteredInspections.count { it.safetyScore >= 75.0 }
    val passRate = if (totalInspections > 0) {
        (compliantCount.toDouble() / totalInspections) * 100.0
    } else 0.0

    val maxScore = sortedFilteredInspections.maxOfOrNull { it.safetyScore } ?: 0.0
    val minScore = sortedFilteredInspections.minOfOrNull { it.safetyScore } ?: 0.0

    // Calculate score momentum (difference between recent half and older half)
    val momentum = remember(sortedFilteredInspections) {
        if (sortedFilteredInspections.size >= 4) {
            val mid = sortedFilteredInspections.size / 2
            val firstHalfAvg = sortedFilteredInspections.take(mid).map { it.safetyScore }.average()
            val secondHalfAvg = sortedFilteredInspections.drop(mid).map { it.safetyScore }.average()
            secondHalfAvg - firstHalfAvg
        } else 0.0
    }

    val selectedInspection = selectedPointIndex?.let { idx ->
        if (idx in sortedFilteredInspections.indices) sortedFilteredInspections[idx] else null
    }

    Scaffold(
        containerColor = WfhDarkBackground,
        topBar = {
            WfhHeader(
                title = "Safety Trends & Analytics",
                subtitle = "Historical Room DB Telemetry • DMRE 75% Target",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("safety_trend_analytics_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header & Subtitle Banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = "Trend Icon",
                                tint = WfhRedVibrant,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Safety Score Chronology",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Interactive visualization of occupational hygiene & safety compliance",
                            style = MaterialTheme.typography.bodySmall,
                            color = WfhTextSecondary
                        )
                    }

                    Button(
                        onClick = onNavigateToNewInspection,
                        colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("analytics_log_new_audit_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Audit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // High-Impact KPI Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Average Safety Score
                    val avgColor = if (averageScore >= 75.0) ComplianceGreenLight else ReviewAmber
                    AnalyticsKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Avg Safety Score",
                        value = if (totalInspections > 0) String.format(Locale.US, "%.1f%%", averageScore) else "N/A",
                        subtitle = if (averageScore >= 75.0) "Compliant" else "Sub-standard",
                        accentColor = avgColor,
                        icon = Icons.Default.HealthAndSafety
                    )

                    // Statutory 75% Pass Rate
                    AnalyticsKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "75% Pass Rate",
                        value = if (totalInspections > 0) String.format(Locale.US, "%.0f%%", passRate) else "N/A",
                        subtitle = "$compliantCount / $totalInspections passed",
                        accentColor = if (passRate >= 80.0) ComplianceGreenLight else ReviewAmber,
                        icon = Icons.Default.Verified
                    )

                    // Score Momentum / Trend
                    val momentumText = when {
                        momentum > 0.5 -> String.format(Locale.US, "+%.1f%%", momentum)
                        momentum < -0.5 -> String.format(Locale.US, "%.1f%%", momentum)
                        else -> "Stable"
                    }
                    val momentumColor = when {
                        momentum > 0.5 -> ComplianceGreenLight
                        momentum < -0.5 -> CriticalRed
                        else -> WfhGold
                    }
                    AnalyticsKpiCard(
                        modifier = Modifier.weight(1f),
                        title = "Trend Momentum",
                        value = momentumText,
                        subtitle = if (momentum >= 0) "Improving" else "Degrading",
                        accentColor = momentumColor,
                        icon = if (momentum >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown
                    )
                }
            }

            // Site Filter Chips
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter by Customer / Site:",
                            fontSize = 12.sp,
                            color = WfhTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (selectedSiteFilter != "ALL" || selectedComplianceFilter != "ALL") {
                            Text(
                                text = "Reset Filters",
                                fontSize = 11.sp,
                                color = WfhGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    selectedSiteFilter = "ALL"
                                    selectedComplianceFilter = "ALL"
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedSiteFilter == "ALL",
                                onClick = { selectedSiteFilter = "ALL" },
                                label = { Text("All Sites (${inspections.size})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WfhRedPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = WfhSurfaceElevated,
                                    labelColor = WfhTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedSiteFilter == "ALL",
                                    borderColor = WfhBorderSubtle,
                                    selectedBorderColor = WfhRedPrimary
                                )
                            )
                        }

                        items(uniqueSites) { site ->
                            val count = inspections.count { it.siteLocation.trim() == site }
                            val shortName = site.substringBefore("—").trim()
                            FilterChip(
                                selected = selectedSiteFilter == site,
                                onClick = { selectedSiteFilter = site },
                                label = { Text("$shortName ($count)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WfhRedPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = WfhSurfaceElevated,
                                    labelColor = WfhTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedSiteFilter == site,
                                    borderColor = WfhBorderSubtle,
                                    selectedBorderColor = WfhRedPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Compliance Status Filter Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val filters = listOf(
                            Triple("ALL", "All Audits", Color.White),
                            Triple("COMPLIANT", "Compliant (≥75%)", ComplianceGreenLight),
                            Triple("NON_COMPLIANT", "Non-compliant (<75%)", CriticalRed)
                        )
                        filters.forEach { (key, label, tint) ->
                            val isSelected = selectedComplianceFilter == key
                            Surface(
                                color = if (isSelected) tint.copy(alpha = 0.2f) else WfhSurfaceDark,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isSelected) tint else WfhBorderSubtle),
                                modifier = Modifier.clickable { selectedComplianceFilter = key }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) tint else WfhTextMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Main Interactive Compose Canvas Chart Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Chart Top Legend & Instructions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(ComplianceGreenLight)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Score Trendline", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)

                                Spacer(modifier = Modifier.width(14.dp))

                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .height(2.dp)
                                        .background(WfhGold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("75% DMRE Target", fontSize = 11.sp, color = WfhGold)
                            }

                            Text(
                                text = "Touch/drag to inspect",
                                fontSize = 10.sp,
                                color = WfhTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (sortedFilteredInspections.isEmpty()) {
                            // Empty State
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .background(WfhSurfaceDark, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = null,
                                        tint = WfhTextMuted,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No inspections match the selected filters",
                                        color = WfhTextSecondary,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            selectedSiteFilter = "ALL"
                                            selectedComplianceFilter = "ALL"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WfhSurfaceElevated),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Clear Filters", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            // Native Jetpack Compose Canvas Interactive Chart
                            InteractiveSafetyScoreCanvas(
                                inspections = sortedFilteredInspections,
                                selectedIndex = selectedPointIndex,
                                onSelectIndex = { selectedPointIndex = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .testTag("safety_score_trend_canvas")
                            )
                        }

                        // Selected Point Detailed Tooltip Card
                        if (selectedInspection != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            SelectedAuditDetailBanner(
                                inspection = selectedInspection,
                                onViewPdf = { onViewInspectionPdf(selectedInspection) }
                            )
                        }
                    }
                }
            }

            // Chronological Inspections Breakdown Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contributing Audit Records (${sortedFilteredInspections.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Min: ${String.format(Locale.US, "%.0f%%", minScore)} | Max: ${String.format(Locale.US, "%.0f%%", maxScore)}",
                        fontSize = 11.sp,
                        color = WfhGold
                    )
                }
            }

            itemsIndexed(sortedFilteredInspections.reversed()) { index, inspection ->
                InspectionChronologyCard(
                    inspection = inspection,
                    isSelected = selectedInspection?.id == inspection.id,
                    onCardClick = {
                        val originalIdx = sortedFilteredInspections.indexOf(inspection)
                        if (originalIdx != -1) {
                            selectedPointIndex = originalIdx
                        }
                    },
                    onViewPdf = { onViewInspectionPdf(inspection) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * High-performance Jetpack Compose Canvas Chart
 * Replaces web-based D3 / Recharts with a 60fps native smooth curve,
 * 75% statutory guideline, gradient fill, and touch scrubber.
 */
@Composable
fun InteractiveSafetyScoreCanvas(
    inspections: List<InspectionEntity>,
    selectedIndex: Int?,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Text paint for Y-axis labels and guideline text
    val textPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.parseColor("#A0A5B2")
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
    }

    val guidelineTextPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.parseColor("#FFB300")
            textSize = with(density) { 9.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }
    }

    val dateTextPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.parseColor("#6B7280")
            textSize = with(density) { 9.sp.toPx() }
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    Canvas(
        modifier = modifier
            .pointerInput(inspections) {
                detectTapGestures { offset ->
                    val paddingLeft = 40.dp.toPx()
                    val paddingRight = 16.dp.toPx()
                    val chartWidth = size.width - paddingLeft - paddingRight
                    val count = inspections.size

                    if (count == 1) {
                        onSelectIndex(0)
                        return@detectTapGestures
                    }

                    val stepX = chartWidth / (count - 1).coerceAtLeast(1)
                    val touchX = (offset.x - paddingLeft).coerceIn(0f, chartWidth)
                    val nearestIndex = (touchX / stepX).toInt().coerceIn(0, count - 1)
                    onSelectIndex(nearestIndex)
                }
            }
            .pointerInput(inspections) {
                detectDragGestures { change, _ ->
                    val paddingLeft = 40.dp.toPx()
                    val paddingRight = 16.dp.toPx()
                    val chartWidth = size.width - paddingLeft - paddingRight
                    val count = inspections.size

                    if (count > 0) {
                        val stepX = chartWidth / (count - 1).coerceAtLeast(1)
                        val touchX = (change.position.x - paddingLeft).coerceIn(0f, chartWidth)
                        val nearestIndex = ((touchX + stepX / 2) / stepX).toInt().coerceIn(0, count - 1)
                        onSelectIndex(nearestIndex)
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height

        val paddingLeft = 44.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 30.dp.toPx()

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        if (chartWidth <= 0 || chartHeight <= 0) return@Canvas

        // 1. Draw Y-Axis Grid Lines & Percentage Labels (0%, 25%, 50%, 75%, 100%)
        val gridLevels = listOf(0.0, 25.0, 50.0, 75.0, 100.0)
        gridLevels.forEach { scoreLevel ->
            val y = paddingTop + chartHeight - (scoreLevel / 100.0 * chartHeight).toFloat()

            // Horizontal Grid Line
            val isThreshold = scoreLevel == 75.0
            val lineColor = if (isThreshold) WfhGold.copy(alpha = 0.6f) else WfhBorderSubtle.copy(alpha = 0.5f)
            val strokeWidth = if (isThreshold) 2.dp.toPx() else 1.dp.toPx()

            if (isThreshold) {
                // Dashed line for 75% Statutory Compliance Threshold
                drawLine(
                    color = lineColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(paddingLeft + chartWidth, y),
                    strokeWidth = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
                // Draw Target badge text
                drawContext.canvas.nativeCanvas.drawText(
                    "75% Target (DMRE)",
                    paddingLeft + 6.dp.toPx(),
                    y - 4.dp.toPx(),
                    guidelineTextPaint
                )
            } else {
                drawLine(
                    color = lineColor,
                    start = Offset(paddingLeft, y),
                    end = Offset(paddingLeft + chartWidth, y),
                    strokeWidth = strokeWidth
                )
            }

            // Draw Y-axis percentage text
            drawContext.canvas.nativeCanvas.drawText(
                "${scoreLevel.toInt()}%",
                paddingLeft - 8.dp.toPx(),
                y + 4.dp.toPx(),
                textPaint
            )
        }

        val count = inspections.size
        if (count == 0) return@Canvas

        // 2. Compute Points for Each Inspection
        val points = mutableListOf<Offset>()
        for (i in 0 until count) {
            val item = inspections[i]
            val x = if (count == 1) {
                paddingLeft + chartWidth / 2f
            } else {
                paddingLeft + (i.toFloat() / (count - 1)) * chartWidth
            }
            val clampedScore = item.safetyScore.coerceIn(0.0, 100.0)
            val y = paddingTop + chartHeight - (clampedScore / 100.0 * chartHeight).toFloat()
            points.add(Offset(x, y))
        }

        // 3. Draw Gradient Area Fill Beneath Curve
        val areaPath = Path().apply {
            moveTo(points.first().x, paddingTop + chartHeight)
            lineTo(points.first().x, points.first().y)

            if (points.size > 1) {
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX1 = p0.x + (p1.x - p0.x) / 2f
                    val controlY1 = p0.y
                    val controlX2 = p0.x + (p1.x - p0.x) / 2f
                    val controlY2 = p1.y
                    cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                }
            }

            lineTo(points.last().x, paddingTop + chartHeight)
            close()
        }

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    ComplianceGreenLight.copy(alpha = 0.35f),
                    WfhRedVibrant.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                startY = paddingTop,
                endY = paddingTop + chartHeight
            )
        )

        // 4. Draw Smooth Cubic Bezier Trendline
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            if (points.size > 1) {
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX1 = p0.x + (p1.x - p0.x) / 2f
                    val controlY1 = p0.y
                    val controlX2 = p0.x + (p1.x - p0.x) / 2f
                    val controlY2 = p1.y
                    cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                }
            }
        }

        drawPath(
            path = linePath,
            color = ComplianceGreenLight,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 5. Draw Data Point Markers & X-Axis Dates
        val displayInterval = if (count > 6) count / 5 else 1
        points.forEachIndexed { i, pt ->
            val inspection = inspections[i]
            val isCompliant = inspection.safetyScore >= 75.0
            val isCurrentSelected = selectedIndex == i

            // Draw point circle
            val pointRadius = if (isCurrentSelected) 7.dp.toPx() else 4.5f.dp.toPx()
            val pointColor = if (isCompliant) ComplianceGreenLight else CriticalRed

            drawCircle(
                color = Color.White,
                radius = pointRadius + 2.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = pointColor,
                radius = pointRadius,
                center = pt
            )

            // Draw Date on X-axis for spaced points or selected point
            if (i % displayInterval == 0 || isCurrentSelected || i == count - 1) {
                val shortDate = if (inspection.date.length >= 10) {
                    inspection.date.substring(5) // MM-DD
                } else inspection.date

                drawContext.canvas.nativeCanvas.drawText(
                    shortDate,
                    pt.x,
                    height - 6.dp.toPx(),
                    dateTextPaint
                )
            }
        }

        // 6. Draw Scrubber Vertical Guideline for Selected Point
        if (selectedIndex != null && selectedIndex in points.indices) {
            val selPt = points[selectedIndex]
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = Offset(selPt.x, paddingTop),
                end = Offset(selPt.x, paddingTop + chartHeight),
                strokeWidth = 1.5f.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )

            // Score Callout Bubble
            val selInspection = inspections[selectedIndex]
            val scoreText = String.format(Locale.US, "%.1f%%", selInspection.safetyScore)
            val bubbleColor = if (selInspection.safetyScore >= 75.0) ComplianceGreen else CriticalRed

            drawCircle(
                color = bubbleColor,
                radius = 9.dp.toPx(),
                center = selPt
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = selPt
            )
        }
    }
}

/**
 * Metric KPI Summary Card
 */
@Composable
fun AnalyticsKpiCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = WfhSurfaceCard,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, WfhBorderSubtle),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = WfhTextSecondary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )

            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = WfhTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Detailed banner for the currently scrubbed/selected audit on the chart
 */
@Composable
fun SelectedAuditDetailBanner(
    inspection: InspectionEntity,
    onViewPdf: () -> Unit
) {
    val isCompliant = inspection.safetyScore >= 75.0 || inspection.status.equals("COMPLIANT", ignoreCase = true)
    val statusBg = if (isCompliant) ComplianceGreenContainer else CriticalRedContainer
    val statusBorder = if (isCompliant) ComplianceGreen else CriticalRed
    val statusTextColor = if (isCompliant) ComplianceGreenLight else Color(0xFFFFDAD6)

    Surface(
        color = WfhSurfaceElevated,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, statusBorder.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(statusBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.0f%%", inspection.safetyScore),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = inspection.siteLocation,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Date: ${inspection.date} • Assessor: ${inspection.inspectorName}",
                            fontSize = 10.sp,
                            color = WfhTextSecondary
                        )
                    }
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, statusBorder)
                ) {
                    Text(
                        text = if (isCompliant) "COMPLIANT" else "NON-COMPLIANT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            if (inspection.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Observations: \"${inspection.notes}\"",
                    fontSize = 11.sp,
                    color = WfhTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onViewPdf,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, WfhBorderSubtle),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "PDF",
                        tint = WfhRedVibrant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Statutory PDF", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * Chronological Inspection Log Card
 */
@Composable
fun InspectionChronologyCard(
    inspection: InspectionEntity,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onViewPdf: () -> Unit
) {
    val isCompliant = inspection.safetyScore >= 75.0 || inspection.status.equals("COMPLIANT", ignoreCase = true)
    val scoreColor = if (isCompliant) ComplianceGreenLight else CriticalRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .border(
                1.dp,
                if (isSelected) WfhRedVibrant else WfhBorderSubtle,
                RoundedCornerShape(10.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) WfhSurfaceElevated else WfhSurfaceCard
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Score Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(WfhSurfaceDark)
                        .border(1.5f.dp, scoreColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format(Locale.US, "%.0f%%", inspection.safetyScore),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = inspection.siteLocation,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = inspection.date,
                            fontSize = 11.sp,
                            color = WfhGold,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = " • ${inspection.inspectorName}",
                            fontSize = 11.sp,
                            color = WfhTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(
                onClick = onViewPdf,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "View PDF",
                    tint = WfhRedVibrant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

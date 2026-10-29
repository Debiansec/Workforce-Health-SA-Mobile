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
import com.example.data.local.RemedialActionEntity
import com.example.data.model.RemedialPriority
import com.example.data.model.RemedialStatus
import com.example.ui.components.WfhHeader
import com.example.ui.theme.*

@Composable
fun RemedialActionsScreen(
    actions: List<RemedialActionEntity>,
    onUpdateStatus: (String, RemedialStatus) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Open / In Progress", "Completed")

    val filteredActions = when (selectedTab) {
        1 -> actions.filter { it.status == "OPEN" || it.status == "IN_PROGRESS" }
        2 -> actions.filter { it.status == "COMPLETED" || it.status == "VERIFIED" }
        else -> actions
    }

    Scaffold(
        containerColor = WfhDarkBackground,
        topBar = {
            WfhHeader(
                title = "Remedial Actions Register",
                subtitle = "Occupational Hygiene Corrective Log",
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = WfhSurfaceCard,
                contentColor = Color.White
            ) {
                tabs.forEachIndexed { index, tabTitle ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = tabTitle,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) WfhRedVibrant else WfhTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredActions.isEmpty()) {
                    item {
                        Surface(
                            color = WfhSurfaceCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CheckCircleOutline,
                                    contentDescription = "None",
                                    tint = ComplianceGreenLight,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No Outstanding Actions",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "All corrective measures for current filter are addressed.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = WfhTextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(filteredActions) { action ->
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
                                    Surface(
                                        color = WfhSurfaceElevated,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = action.referenceNumber,
                                            fontWeight = FontWeight.Bold,
                                            color = WfhGold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Surface(
                                        color = if (action.status == "COMPLETED" || action.status == "VERIFIED")
                                            ComplianceGreenContainer else CriticalRedContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = action.status.replace("_", " "),
                                            color = if (action.status == "COMPLETED" || action.status == "VERIFIED")
                                                ComplianceGreenLight else CriticalRed,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = action.findingDescription,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )

                                Text(
                                    text = "Related: ${action.relatedQuestion}",
                                    fontSize = 11.sp,
                                    color = WfhTextSecondary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    color = WfhSurfaceDark,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Action: ${action.recommendedAction}",
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Responsible: ${action.responsibleParty} • Target: ${action.targetDate}",
                                            fontSize = 11.sp,
                                            color = WfhTextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (action.status == "OPEN") {
                                        Button(
                                            onClick = { onUpdateStatus(action.id, RemedialStatus.IN_PROGRESS) },
                                            modifier = Modifier.height(34.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ReviewAmber)
                                        ) {
                                            Text("Mark In Progress", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (action.status != "COMPLETED" && action.status != "VERIFIED") {
                                        Button(
                                            onClick = { onUpdateStatus(action.id, RemedialStatus.COMPLETED) },
                                            modifier = Modifier.height(34.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ComplianceGreen)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Resolve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

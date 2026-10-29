package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun WfhHeader(
    title: String? = null,
    subtitle: String? = null,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    userRole: UserRole? = null,
    onProfileClick: () -> Unit = {},
    isOnline: Boolean = true,
    isSyncing: Boolean = false,
    onSyncClick: () -> Unit = {},
    onToggleOnline: () -> Unit = {},
    onNavigateToCustomerFiles: (() -> Unit)? = null,
    onNavigateToDashboard: (() -> Unit)? = null,
    onNavigateToNewAssessment: (() -> Unit)? = null,
    onNavigateToTrips: (() -> Unit)? = null,
    onNavigateToRemedial: (() -> Unit)? = null,
    onNavigateToEquipment: (() -> Unit)? = null,
    onNavigateToHistoricalInspections: (() -> Unit)? = null,
    onNavigateToSafetyTrends: (() -> Unit)? = null
) {
    Surface(
        color = WfhSurfaceDark,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = WfhBorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (showBackButton) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .testTag("header_back_button")
                                .padding(end = 8.dp)
                                .size(40.dp)
                                .background(WfhSurfaceElevated, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }

                    // Logo
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF0EFEA))
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
                            text = title ?: "WORKFORCE HEALTH SA",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = subtitle ?: "Occupational Hygiene Digital Platform",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = WfhTextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Action Bar (Sync + Online status + Profile)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Online/Offline pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isOnline) ComplianceGreenContainer else CriticalRedContainer)
                            .border(
                                1.dp,
                                if (isOnline) ComplianceGreen else CriticalRed,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onToggleOnline() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) ComplianceGreenLight else CriticalRed)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOnline) "Cloud Online" else "Offline Mode",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    // Sync Button
                    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "sync_rot"
                    )

                    IconButton(
                        onClick = onSyncClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WfhSurfaceElevated)
                            .testTag("sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Synchronise",
                            tint = if (isSyncing) WfhRedVibrant else Color.White,
                            modifier = if (isSyncing) Modifier.rotate(rotation) else Modifier
                        )
                    }

                    // User role pill / avatar
                    if (userRole != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(WfhRedPrimary)
                                .clickable { onProfileClick() }
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userRole.displayName.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Navigation Menu Button
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(WfhSurfaceElevated)
                                .testTag("header_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Main Navigation Menu",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier
                                .background(WfhSurfaceDark)
                                .border(1.dp, WfhBorderSubtle, RoundedCornerShape(8.dp))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "Customer Operational Files",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Afrimat Kuipersbult • Legal, HEG & Fleet",
                                            color = WfhGold,
                                            fontSize = 11.sp
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FolderShared,
                                        contentDescription = "Customer Files",
                                        tint = WfhRedVibrant
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToCustomerFiles?.invoke()
                                },
                                modifier = Modifier.testTag("menu_customer_files")
                            )

                            HorizontalDivider(color = WfhBorderSubtle)

                            DropdownMenuItem(
                                text = { Text("Dashboard Overview", color = Color.White, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Dashboard, contentDescription = null, tint = Color.White)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToDashboard?.invoke()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "Safety Trends & Analytics",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Interactive Chart • DMRE 75% Target",
                                            color = ComplianceGreenLight,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.TrendingUp, contentDescription = "Safety Trends", tint = ComplianceGreenLight)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToSafetyTrends?.invoke()
                                },
                                modifier = Modifier.testTag("menu_safety_trends")
                            )

                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "Historical Inspections",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Room DB • PDF Viewer & CSV Export",
                                            color = WfhGold,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.History, contentDescription = "Historical Inspections", tint = WfhRedVibrant)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToHistoricalInspections?.invoke()
                                },
                                modifier = Modifier.testTag("menu_historical_inspections")
                            )

                            DropdownMenuItem(
                                text = { Text("Start Inspection / Assessment", color = Color.White, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = WfhRedPrimary)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToNewAssessment?.invoke()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Site Trips & GPS Tracker", color = Color.White, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Navigation, contentDescription = null, tint = ReviewAmber)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToTrips?.invoke()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Remedial Actions Register", color = Color.White, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = CriticalRed)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToRemedial?.invoke()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Equipment & Instruments", color = Color.White, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.BuildCircle, contentDescription = null, tint = WfhGold)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToEquipment?.invoke()
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Assessor Profile", color = Color.White, fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.LightGray)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onProfileClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

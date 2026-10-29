package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit
) {
    var selectedRole by remember { mutableStateOf(UserRole.ASSESSOR) }
    var pin by remember { mutableStateOf("1234") }

    Scaffold(
        containerColor = WfhDarkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))

                // Logo Box
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0EFEA))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.wfh_logo),
                        contentDescription = "WorkForce Health SA",
                        modifier = Modifier.width(180.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "WORKFORCE HEALTH SA",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White
                )

                Text(
                    text = "Occupational Hygiene Digital Platform",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WfhRedVibrant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Select Authorised Role to Authenticate",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = WfhTextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Role selection cards
            val roles = listOf(
                Triple(
                    UserRole.ASSESSOR,
                    Icons.Default.AssignmentInd,
                    "Conduct inspections, capture GPS & photo evidence, save offline drafts, submit fieldwork"
                ),
                Triple(
                    UserRole.HYGIENIST,
                    Icons.Default.VerifiedUser,
                    "Review submitted assessments, enter professional interpretations, approve or return reports"
                ),
                Triple(
                    UserRole.MINE_MANAGEMENT,
                    Icons.Default.Business,
                    "View authorised site compliance, track assigned remedial actions, acknowledge findings"
                ),
                Triple(
                    UserRole.ADMIN,
                    Icons.Default.AdminPanelSettings,
                    "Manage customers, sites, checklist templates, equipment register and sync audit logs"
                )
            )

            items(roles.size) { index ->
                val (role, icon, description) = roles[index]
                val isSelected = selectedRole == role

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { selectedRole = role }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) WfhRedPrimary else WfhBorderSubtle,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .testTag("role_card_${role.name.lowercase()}"),
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
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) WfhRedPrimary else WfhSurfaceDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = role.displayName,
                                tint = if (isSelected) Color.White else WfhTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = role.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else WfhTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = WfhTextSecondary,
                                lineHeight = 15.sp
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedRole = role },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = WfhRedPrimary,
                                unselectedColor = WfhTextMuted
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))

                // Credentials info card
                Surface(
                    color = WfhSurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security",
                                tint = WfhGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Zero-Trust Device Authentication",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Assessor: David Nkosi (OH-TECH-2024-912)\nDevice ID: WFH-FIELD-TAB-04\nOffline Cache: Encrypted Room DB Active",
                            fontSize = 11.sp,
                            color = WfhTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onLoginSuccess(selectedRole) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = "Login",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Enter as ${selectedRole.displayName}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

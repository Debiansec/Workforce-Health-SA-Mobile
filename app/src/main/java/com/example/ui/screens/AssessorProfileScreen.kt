package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.UserProfile
import com.example.ui.components.WfhHeader
import com.example.ui.theme.*

@Composable
fun AssessorProfileScreen(
    user: UserProfile,
    onSaveProfile: (UserProfile) -> Unit,
    onBack: () -> Unit
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var email by remember { mutableStateOf(user.email) }
    var mobile by remember { mutableStateOf(user.mobileNumber) }
    var workMobile by remember { mutableStateOf(user.workMobile) }
    var whatsapp by remember { mutableStateOf(user.whatsAppNumber) }
    var emergencyName by remember { mutableStateOf(user.emergencyContactName) }
    var emergencyPhone by remember { mutableStateOf(user.emergencyContactPhone) }
    var jobDesc by remember { mutableStateOf(user.jobDescription) }
    var certNo by remember { mutableStateOf(user.certificationNumber) }

    var isSaved by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = WfhDarkBackground,
        topBar = {
            WfhHeader(
                title = "Assessor Profile",
                subtitle = "Occupational Hygiene Credentials",
                showBackButton = true,
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(WfhRedPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString(""),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = fullName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "${user.role.displayName} • $certNo",
                            fontSize = 12.sp,
                            color = WfhTextSecondary
                        )
                    }
                }
            }

            if (isSaved) {
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
                            Text("Profile credentials saved successfully.", color = ComplianceGreenLight, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Text("Assessor & Contact Details", fontWeight = FontWeight.Bold, color = Color.White)
            }

            item {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name & Surname") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }

            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("Mobile Number") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = workMobile,
                        onValueChange = { workMobile = it },
                        label = { Text("Work Mobile") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp Number") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }

            item {
                Text("Emergency Contact Information", fontWeight = FontWeight.Bold, color = Color.White)
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = emergencyName,
                        onValueChange = { emergencyName = it },
                        label = { Text("Emergency Contact Person") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = emergencyPhone,
                        onValueChange = { emergencyPhone = it },
                        label = { Text("Emergency Phone") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            }

            item {
                Text("Professional Hygiene Role & Certification", fontWeight = FontWeight.Bold, color = Color.White)
            }

            item {
                OutlinedTextField(
                    value = jobDesc,
                    onValueChange = { jobDesc = it },
                    label = { Text("Job Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }

            item {
                OutlinedTextField(
                    value = certNo,
                    onValueChange = { certNo = it },
                    label = { Text("SAIOH / Professional Registration No") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val updated = user.copy(
                            fullName = fullName,
                            email = email,
                            mobileNumber = mobile,
                            workMobile = workMobile,
                            whatsAppNumber = whatsapp,
                            emergencyContactName = emergencyName,
                            emergencyContactPhone = emergencyPhone,
                            jobDescription = jobDesc,
                            certificationNumber = certNo
                        )
                        onSaveProfile(updated)
                        isSaved = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_profile_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Profile Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

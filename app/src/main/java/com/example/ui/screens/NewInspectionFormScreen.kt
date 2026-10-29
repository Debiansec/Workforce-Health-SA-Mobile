package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import android.net.Uri
import coil.compose.AsyncImage
import com.example.data.model.CustomerSite
import com.example.data.model.UserProfile
import com.example.data.util.CameraCaptureHelper
import com.example.ui.components.WfhHeader
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewInspectionFormScreen(
    currentUser: UserProfile,
    availableSites: List<CustomerSite>,
    onSaveInspection: (siteLocation: String, safetyScore: Double, date: String, inspectorName: String, status: String, notes: String, photoUris: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val todayFormatted = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Form fields
    var siteLocation by remember {
        mutableStateOf(availableSites.firstOrNull()?.let { "${it.customerName} — ${it.siteName}" } ?: "Letaba Crushers — Tarentaalrant Quarry & Plant")
    }
    var safetyScore by remember { mutableStateOf(85.0) }
    var safetyScoreText by remember { mutableStateOf("85") }
    var date by remember { mutableStateOf(todayFormatted) }
    var inspectorName by remember { mutableStateOf(currentUser.fullName) }
    var notes by remember { mutableStateOf("") }
    var statusOverride by remember { mutableStateOf<String?>(null) }
    var gpsCoordinates by remember { mutableStateOf("-23.8315° S, 30.1628° E") }
    var isGpsTagged by remember { mutableStateOf(true) }

    // Camera hardware & photo attachment state
    var attachedPhotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var viewingPhotoPath by remember { mutableStateOf<String?>(null) }

    // Camera Picture Taking Launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingPhotoFile
        if (success && file != null && file.exists() && file.length() > 0) {
            attachedPhotos = attachedPhotos + file.absolutePath
            Toast.makeText(context, "Site photo captured and attached!", Toast.LENGTH_SHORT).show()
        } else {
            file?.delete()
        }
        pendingPhotoFile = null
        pendingPhotoUri = null
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val (file, uri) = CameraCaptureHelper.createCaptureFile(context)
                pendingPhotoFile = file
                pendingPhotoUri = uri
                takePictureLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to launch camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(
                context,
                "Camera hardware permission is required to photograph inspection sites",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Trigger Camera Hardware function
    val onTriggerCameraCapture = remember(context) {
        {
            val hasCameraPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (hasCameraPermission) {
                try {
                    val (file, uri) = CameraCaptureHelper.createCaptureFile(context)
                    pendingPhotoFile = file
                    pendingPhotoUri = uri
                    takePictureLauncher.launch(uri)
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to launch camera: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Speech-to-text state
    var isListening by remember { mutableStateOf(false) }
    var lastSpokenText by remember { mutableStateOf<String?>(null) }

    // Speech recognition launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = spokenMatches?.firstOrNull()?.trim()
            if (!recognizedText.isNullOrEmpty()) {
                val formattedText = recognizedText.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }
                notes = if (notes.isBlank()) {
                    "$formattedText."
                } else {
                    val cleanBase = notes.trim().trimEnd('.')
                    "$cleanBase. $formattedText."
                }
                lastSpokenText = formattedText
                Toast.makeText(context, "Voice comment recorded!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launch speech recognition helper
    val launchSpeechRecognizer = remember(context) {
        {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak inspector comments & findings...")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            try {
                isListening = true
                speechRecognizerLauncher.launch(intent)
            } catch (e: Exception) {
                isListening = false
                Toast.makeText(context, "Voice recognition service unavailable", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Microphone runtime permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchSpeechRecognizer()
        } else {
            Toast.makeText(
                context,
                "Microphone permission is required to enable voice-to-text reporting",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Function to trigger voice recording
    fun onTriggerVoiceInput() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchSpeechRecognizer()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Validation & Success feedback
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    // Computed or overridden status
    val derivedStatus = when {
        statusOverride != null -> statusOverride!!
        safetyScore >= 80.0 -> "COMPLIANT"
        safetyScore >= 65.0 -> "PENDING"
        else -> "NON_COMPLIANT"
    }

    val (statusLabel, statusBg, statusColor) = when (derivedStatus) {
        "COMPLIANT" -> Triple("Compliant", ComplianceGreenContainer, ComplianceGreenLight)
        "NON_COMPLIANT" -> Triple("Non-compliant", CriticalRedContainer, CriticalRed)
        else -> Triple("Pending Review", ReviewAmberContainer, ReviewAmber)
    }

    val quickSites = remember {
        listOf(
            "Letaba Crushers — Tarentaalrant Quarry & Plant",
            "Secondary Crushing & Screening Depot — Bay 3",
            "Palabora Mining Company — Main Smelter",
            "Workshop & Trackless Mobile Machinery Bay",
            "Administration Complex & Restrooms",
            "Weighbridge Restroom & Security Shower"
        )
    }

    Scaffold(
        containerColor = WfhDarkBackground,
        topBar = {
            WfhHeader(
                title = "New Inspection Entry",
                subtitle = "Log Safety Audit to Database",
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
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("cancel_inspection_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (siteLocation.isBlank()) {
                                errorMessage = "Please enter or select a site location."
                                return@Button
                            }
                            if (inspectorName.isBlank()) {
                                errorMessage = "Please enter the inspector's name."
                                return@Button
                            }
                            if (date.isBlank()) {
                                errorMessage = "Please enter a valid date (YYYY-MM-DD)."
                                return@Button
                            }

                            errorMessage = null
                            isSuccess = true

                            onSaveInspection(
                                siteLocation.trim(),
                                safetyScore,
                                date.trim(),
                                inspectorName.trim(),
                                derivedStatus,
                                if (isGpsTagged) "$notes [GPS: $gpsCoordinates]".trim() else notes.trim(),
                                attachedPhotos.joinToString("|")
                            )
                        },
                        modifier = Modifier
                            .weight(2f)
                            .height(50.dp)
                            .testTag("save_inspection_to_db_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save to Database",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success Banner
            if (isSuccess) {
                item {
                    Surface(
                        color = ComplianceGreenContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ComplianceGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ComplianceGreenLight)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Inspection Saved Successfully!",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Record written to Room database and linked to status indicators.",
                                    color = ComplianceGreenLight,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Error Banner
            if (errorMessage != null) {
                item {
                    Surface(
                        color = CriticalRedContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CriticalRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = CriticalRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMessage!!, color = Color(0xFFFFDAD6), fontSize = 12.sp)
                        }
                    }
                }
            }

            // Site Location Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Location",
                                tint = WfhRedVibrant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Site Location",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = siteLocation,
                            onValueChange = { siteLocation = it },
                            label = { Text("Site / Facility Location") },
                            placeholder = { Text("e.g. Letaba Crushers — Tarentaalrant Plant") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_site_location"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WfhRedPrimary,
                                unfocusedBorderColor = WfhBorderSubtle,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = WfhSurfaceDark,
                                unfocusedContainerColor = WfhSurfaceDark
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Quick Suggestions:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WfhTextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(quickSites) { quickSite ->
                                val isSelected = siteLocation == quickSite
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) WfhRedPrimary else WfhSurfaceElevated)
                                        .clickable { siteLocation = quickSite }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = quickSite.take(28) + if (quickSite.length > 28) "…" else "",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else WfhTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Safety Score Card with Slider & Status derivation
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Score",
                                    tint = WfhGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Safety Score",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            // Dynamic Score Pill
                            Surface(
                                color = statusBg,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${safetyScore.toInt()}% • $statusLabel",
                                    color = statusColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Interactive Score Slider
                        Slider(
                            value = safetyScore.toFloat(),
                            onValueChange = { newScore ->
                                safetyScore = newScore.toDouble()
                                safetyScoreText = newScore.toInt().toString()
                            },
                            valueRange = 0f..100f,
                            steps = 99,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("safety_score_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = statusColor,
                                activeTrackColor = statusColor,
                                inactiveTrackColor = WfhSurfaceElevated
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Score Scale: 0 (Critical) to 100 (Safe)",
                                fontSize = 11.sp,
                                color = WfhTextMuted
                            )

                            // Direct Number Input
                            OutlinedTextField(
                                value = safetyScoreText,
                                onValueChange = { input ->
                                    safetyScoreText = input
                                    input.toDoubleOrNull()?.let {
                                        safetyScore = it.coerceIn(0.0, 100.0)
                                    }
                                },
                                label = { Text("Exact %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .width(90.dp)
                                    .testTag("input_safety_score_exact"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = WfhRedPrimary,
                                    unfocusedBorderColor = WfhBorderSubtle
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = WfhBorderSubtle)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Status Selection / Override
                        Text(
                            text = "Status Classification:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val statusOptions = listOf(
                                Triple("PENDING", "Pending", ReviewAmber),
                                Triple("COMPLIANT", "Compliant", ComplianceGreenLight),
                                Triple("NON_COMPLIANT", "Non-compliant", CriticalRed)
                            )

                            statusOptions.forEach { (key, display, tint) ->
                                val isSelected = derivedStatus == key
                                Button(
                                    onClick = { statusOverride = key },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .testTag("status_toggle_$key"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) tint else WfhSurfaceDark,
                                        contentColor = if (isSelected) Color.White else WfhTextSecondary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle)
                                ) {
                                    Text(display, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Date & Inspector Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = "Date",
                                tint = WfhRedVibrant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Audit Date & Inspector Credentials",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = date,
                                onValueChange = { date = it },
                                label = { Text("Date (YYYY-MM-DD)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_inspection_date"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WfhRedPrimary,
                                    unfocusedBorderColor = WfhBorderSubtle,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = WfhSurfaceDark,
                                    unfocusedContainerColor = WfhSurfaceDark
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Button(
                                onClick = { date = todayFormatted },
                                modifier = Modifier
                                    .height(54.dp)
                                    .align(Alignment.CenterVertically)
                                    .testTag("today_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = WfhSurfaceElevated),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Today", fontSize = 12.sp, color = WfhGold, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = inspectorName,
                            onValueChange = { inspectorName = it },
                            label = { Text("Inspector Name & SAIOH Designation") },
                            placeholder = { Text("e.g. David Nkosi (OH-TECH-2024-912)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_inspector_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WfhRedPrimary,
                                unfocusedBorderColor = WfhBorderSubtle,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = WfhSurfaceDark,
                                unfocusedContainerColor = WfhSurfaceDark
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Observations, Voice-to-Text & GPS Tagging Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notes,
                                    contentDescription = "Notes",
                                    tint = WfhRedVibrant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Findings & Telemetry",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            // GPS Pill Toggle
                            Surface(
                                color = if (isGpsTagged) ComplianceGreenContainer else WfhSurfaceElevated,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { isGpsTagged = !isGpsTagged }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GpsFixed,
                                        contentDescription = "GPS",
                                        tint = if (isGpsTagged) ComplianceGreenLight else WfhTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isGpsTagged) "Geo-Tagged" else "No GPS",
                                        fontSize = 11.sp,
                                        color = if (isGpsTagged) ComplianceGreenLight else WfhTextMuted,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Voice-to-Text Microphone Toolbar
                        val micTransition = rememberInfiniteTransition(label = "mic_pulse")
                        val pulseScale by micTransition.animateFloat(
                            initialValue = 1.0f,
                            targetValue = if (isListening) 1.15f else 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "mic_scale"
                        )

                        Surface(
                            color = if (isListening) WfhRedPrimary.copy(alpha = 0.25f) else WfhSurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isListening) WfhRedVibrant else WfhBorderSubtle
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .scale(pulseScale)
                                            .clip(CircleShape)
                                            .background(if (isListening) WfhRedVibrant else WfhSurfaceElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                                            contentDescription = "Microphone",
                                            tint = if (isListening) Color.White else WfhGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isListening) "Listening for Inspector Dictation..." else "Voice-to-Text Reporting",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isListening) WfhRedVibrant else Color.White
                                        )
                                        Text(
                                            text = if (isListening) "Speak your observation now" else "Dictate findings hands-free via mic",
                                            fontSize = 10.sp,
                                            color = WfhTextMuted
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onTriggerVoiceInput() },
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("voice_to_text_mic_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isListening) WfhRedVibrant else WfhRedPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Dictate",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isListening) "Recording..." else "Dictate",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Last Voice Input feedback pill
                        if (lastSpokenText != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = ComplianceGreenContainer,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ComplianceGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = ComplianceGreenLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Voice transcribed: \"$lastSpokenText\"",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { lastSpokenText = null },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = WfhTextMuted,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Field Observations & Remedial Recommendations") },
                            placeholder = { Text("e.g. Wash basins cleaned, ventilation operational, soap dispensers filled, or tap Dictate above...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_inspection_notes"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WfhRedPrimary,
                                unfocusedBorderColor = WfhBorderSubtle,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = WfhSurfaceDark,
                                unfocusedContainerColor = WfhSurfaceDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            minLines = 3,
                            maxLines = 6
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick observation tags for rapid field reporting
                        Text(
                            text = "Quick Observation Presets (Tap to Append):",
                            fontSize = 10.sp,
                            color = WfhTextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val quickNotes = listOf(
                                "✓ Compliant ventilation airflow",
                                "✓ Hand wash soap & sanitizers restocked",
                                "✓ Hot water supply verified (42°C)",
                                "⚠ Shower drainage sub-standard",
                                "⚠ Deep chemical disinfection required",
                                "⚠ Urinal auto-flush defective"
                            )
                            items(quickNotes) { tag ->
                                Surface(
                                    color = WfhSurfaceElevated,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle),
                                    modifier = Modifier.clickable {
                                        val cleanTag = tag.substringAfter(" ").trim()
                                        notes = if (notes.isBlank()) cleanTag else "${notes.trim().trimEnd('.')}. $cleanTag."
                                    }
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 10.sp,
                                        color = if (tag.startsWith("✓")) ComplianceGreenLight else ReviewAmber,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        if (isGpsTagged) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Coordinates: $gpsCoordinates (WGS84)",
                                fontSize = 11.sp,
                                color = WfhGold
                            )
                        }
                    }
                }
            }

            // Site Photographic Evidence (Camera Hardware Access)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Camera",
                                    tint = WfhRedVibrant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Site Photographic Evidence",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            // Photo Count Chip
                            Surface(
                                color = if (attachedPhotos.isNotEmpty()) ComplianceGreenContainer else WfhSurfaceElevated,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (attachedPhotos.isNotEmpty()) ComplianceGreen else WfhBorderSubtle
                                )
                            ) {
                                Text(
                                    text = "${attachedPhotos.size} Photo${if (attachedPhotos.size == 1) "" else "s"} Attached",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (attachedPhotos.isNotEmpty()) ComplianceGreenLight else WfhTextMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Photograph physical workplace conditions, defects, or cleanliness verification via device camera hardware.",
                            fontSize = 11.sp,
                            color = WfhTextMuted
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Camera & Quick Sample Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onTriggerCameraCapture() },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(48.dp)
                                    .testTag("take_photo_camera_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Capture Photo",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Capture Site Photo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    val samplePath = CameraCaptureHelper.createSampleSitePhoto(
                                        context = context,
                                        siteLocation = siteLocation,
                                        category = "Audit Photo #${attachedPhotos.size + 1}"
                                    )
                                    attachedPhotos = attachedPhotos + samplePath
                                    Toast.makeText(context, "Site snapshot attached!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("add_sample_photo_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Sample Snapshot",
                                    modifier = Modifier.size(16.dp),
                                    tint = WfhGold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Quick Snapshot",
                                    fontSize = 11.sp,
                                    color = WfhGold,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Photo Thumbnails Strip
                        if (attachedPhotos.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Attached Photos (Tap to enlarge, trash to delete):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WfhTextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(attachedPhotos.size) { index ->
                                    val photoPath = attachedPhotos[index]
                                    Card(
                                        modifier = Modifier
                                            .size(width = 130.dp, height = 120.dp)
                                            .border(1.dp, WfhBorderSubtle, RoundedCornerShape(10.dp))
                                            .clickable { viewingPhotoPath = photoPath }
                                            .testTag("inspection_photo_thumbnail_$index"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = WfhSurfaceDark)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            AsyncImage(
                                                model = File(photoPath),
                                                contentDescription = "Inspection Photo #${index + 1}",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )

                                            // Top overlay with index badge and delete button
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0x99000000))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "#${index + 1}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                IconButton(
                                                    onClick = {
                                                        CameraCaptureHelper.deletePhotoFile(photoPath)
                                                        attachedPhotos = attachedPhotos.filterIndexed { i, _ -> i != index }
                                                        Toast.makeText(context, "Photo removed", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .testTag("delete_photo_button_$index")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete photo",
                                                        tint = CriticalRed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }

                                            // Bottom hint
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth()
                                                    .background(Color(0x88000000))
                                                    .padding(vertical = 2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Tap to enlarge",
                                                    fontSize = 9.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Full-screen Image Preview Dialog
    viewingPhotoPath?.let { path ->
        AlertDialog(
            onDismissRequest = { viewingPhotoPath = null },
            modifier = Modifier.testTag("inspection_photo_preview_dialog"),
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
                            .height(260.dp)
                            .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black)
                    ) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = "Full Site Photo",
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
                    Text(
                        text = "Site: $siteLocation",
                        fontSize = 11.sp,
                        color = WfhGold
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
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        CameraCaptureHelper.deletePhotoFile(path)
                        attachedPhotos = attachedPhotos.filter { it != path }
                        viewingPhotoPath = null
                        Toast.makeText(context, "Photo deleted", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete Photo", color = CriticalRed)
                }
            }
        )
    }
}

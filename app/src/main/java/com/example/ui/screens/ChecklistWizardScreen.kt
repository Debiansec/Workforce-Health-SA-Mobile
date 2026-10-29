package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.data.local.*
import com.example.data.model.*
import com.example.ui.components.SignaturePad
import com.example.ui.components.WfhHeader
import com.example.ui.theme.*
import kotlinx.coroutines.flow.Flow

@Composable
fun ChecklistWizardScreen(
    assessmentId: String,
    assessmentFlow: Flow<List<AssessmentEntity>>,
    answersFlow: Flow<List<AnswerEntity>>,
    deepChemicalsFlow: Flow<List<ChemicalRecordEntity>>,
    routineChemicalsFlow: Flow<List<ChemicalRecordEntity>>,
    environmentalReadingsFlow: Flow<List<EnvironmentalReadingEntity>>,
    currentStep: Int,
    onStepChange: (Int) -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onUpdateAnswer: (AnswerEntity) -> Unit,
    onAddCustomItem: (String, ComplianceResponse, String) -> Unit,
    onAddChemical: (String, String, Boolean, String, String) -> Unit,
    onDeleteChemical: (String) -> Unit,
    onUpdateEnvironmentalReading: (EnvironmentalReadingEntity) -> Unit,
    onCreateRemedialAction: (finding: String, relatedQuestion: String, action: String, responsible: String, targetDate: String, priority: RemedialPriority) -> Unit,
    onSubmitAssessment: (String) -> Unit,
    onBack: () -> Unit
) {
    val assessments by assessmentFlow.collectAsState(initial = emptyList())
    val assessment = assessments.find { it.id == assessmentId }

    val answers by answersFlow.collectAsState(initial = emptyList())
    val deepChemicals by deepChemicalsFlow.collectAsState(initial = emptyList())
    val routineChemicals by routineChemicalsFlow.collectAsState(initial = emptyList())
    val environmentalReadings by environmentalReadingsFlow.collectAsState(initial = emptyList())

    // Dialog state for Remedial Action creation
    var remedialActionDialogQuestion by remember { mutableStateOf<AnswerEntity?>(null) }
    var showAddCustomDialog by remember { mutableStateOf(false) }
    var showAddChemicalDialogCategory by remember { mutableStateOf<String?>(null) }

    val stepTitles = listOf(
        "Overview",
        "A: Basins",
        "B: Showers",
        "C: Toilets",
        "D: Urinals",
        "E: Lockers",
        "F: General",
        "G: Other",
        "Environment",
        "Signatures"
    )

    Scaffold(
        containerColor = WfhDarkBackground,
        topBar = {
            WfhHeader(
                title = assessment?.templateTitle ?: "Checklist Wizard",
                subtitle = "${assessment?.customerName ?: ""} • ${assessment?.facilityName ?: ""}",
                showBackButton = true,
                onBackClick = onBack
            )
        },
        bottomBar = {
            // Navigation Bar with Back & Next
            Surface(
                color = WfhSurfaceDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = onPrevStep,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("wizard_prev_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Previous")
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    if (currentStep < 9) {
                        Button(
                            onClick = onNextStep,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("wizard_next_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Next Section", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                onSubmitAssessment(assessment?.assessorSignature ?: "Attested by Assessor")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("submit_assessment_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ComplianceGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Submit", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Submit for Review", fontWeight = FontWeight.Bold)
                        }
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
            // Step Progress Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WfhSurfaceCard)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(stepTitles.size) { index ->
                    val isCurrent = currentStep == index
                    val isPast = currentStep > index

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when {
                                    isCurrent -> WfhRedPrimary
                                    isPast -> ComplianceGreenContainer
                                    else -> WfhSurfaceElevated
                                }
                            )
                            .clickable { onStepChange(index) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isPast) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = ComplianceGreenLight,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = stepTitles[index],
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) Color.White else if (isPast) ComplianceGreenLight else WfhTextSecondary
                            )
                        }
                    }
                }
            }

            // Step Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when (currentStep) {
                    0 -> {
                        item {
                            WizardOverviewStep(assessment = assessment)
                        }
                    }
                    1 -> {
                        val sectionItems = answers.filter { it.sectionCode == "A" }
                        item {
                            SectionHeaderCard("SECTION A — BASINS", "5 Questions • Water taps, soap dispensers, signs & moulds")
                        }
                        items(sectionItems) { answer ->
                            QuestionCard(
                                answer = answer,
                                onUpdate = onUpdateAnswer,
                                onOpenRemedialAction = { remedialActionDialogQuestion = answer }
                            )
                        }
                    }
                    2 -> {
                        val sectionItems = answers.filter { it.sectionCode == "B" }
                        item {
                            SectionHeaderCard("SECTION B — SHOWERS", "5 Questions • Taps, showerheads, curtains & mildew")
                        }
                        items(sectionItems) { answer ->
                            QuestionCard(
                                answer = answer,
                                onUpdate = onUpdateAnswer,
                                onOpenRemedialAction = { remedialActionDialogQuestion = answer }
                            )
                        }
                    }
                    3 -> {
                        val sectionItems = answers.filter { it.sectionCode == "C" }
                        item {
                            SectionHeaderCard("SECTION C — TOILETS", "7 Questions • Cleanliness, lids, sitting seats, flushing & paper")
                        }
                        items(sectionItems) { answer ->
                            QuestionCard(
                                answer = answer,
                                onUpdate = onUpdateAnswer,
                                onOpenRemedialAction = { remedialActionDialogQuestion = answer }
                            )
                        }
                    }
                    4 -> {
                        val sectionItems = answers.filter { it.sectionCode == "D" }
                        item {
                            SectionHeaderCard("SECTION D — URINALS", "5 Questions • Cleanliness, flushing, floors & bad odours")
                        }
                        items(sectionItems) { answer ->
                            QuestionCard(
                                answer = answer,
                                onUpdate = onUpdateAnswer,
                                onOpenRemedialAction = { remedialActionDialogQuestion = answer }
                            )
                        }
                    }
                    5 -> {
                        val sectionItems = answers.filter { it.sectionCode == "E" }
                        item {
                            SectionHeaderCard("SECTION E — LOCKERS", "5 Questions • Assignment, tags, locks & cleanliness")
                        }
                        items(sectionItems) { answer ->
                            QuestionCard(
                                answer = answer,
                                onUpdate = onUpdateAnswer,
                                onOpenRemedialAction = { remedialActionDialogQuestion = answer }
                            )
                        }
                    }
                    6 -> {
                        val sectionItems = answers.filter { it.sectionCode == "F" }
                        item {
                            SectionHeaderCard("SECTION F — GENERAL", "23 Questions • Deep cleaning, routine registers, SDS & training")
                        }
                        items(sectionItems) { answer ->
                            QuestionCard(
                                answer = answer,
                                onUpdate = onUpdateAnswer,
                                onOpenRemedialAction = { remedialActionDialogQuestion = answer }
                            )

                            // Render Chemical Table for Question 20 (Deep Cleaning)
                            if (answer.questionNumber == 20) {
                                ChemicalRegisterSection(
                                    title = "Deep-Cleaning Chemical Register (Question 20)",
                                    chemicals = deepChemicals,
                                    onAddClick = { showAddChemicalDialogCategory = "DEEP_CLEANING" },
                                    onDeleteClick = onDeleteChemical
                                )
                            }

                            // Render Chemical Table for Question 22 (Routine Cleaning)
                            if (answer.questionNumber == 22) {
                                ChemicalRegisterSection(
                                    title = "Routine-Cleaning Chemical Register (Question 22)",
                                    chemicals = routineChemicals,
                                    onAddClick = { showAddChemicalDialogCategory = "ROUTINE_CLEANING" },
                                    onDeleteClick = onDeleteChemical
                                )
                            }
                        }
                    }
                    7 -> {
                        val customItems = answers.filter { it.sectionCode == "G" }
                        item {
                            SectionHeaderCard(
                                title = "SECTION G — OTHER, PLEASE LIST BELOW",
                                subtitle = "Add custom repeatable inspection items for unique site features"
                            )
                        }
                        items(customItems) { answer ->
                            QuestionCard(
                                answer = answer,
                                onUpdate = onUpdateAnswer,
                                onOpenRemedialAction = { remedialActionDialogQuestion = answer }
                            )
                        }
                        item {
                            OutlinedButton(
                                onClick = { showAddCustomDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("add_custom_item_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = WfhRedVibrant)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Custom Inspection Question")
                            }
                        }
                    }
                    8 -> {
                        item {
                            SectionHeaderCard(
                                title = "ENVIRONMENTAL CONDITIONS",
                                subtitle = "Shift temperature, WBGT, humidity, wind speed & instrument records"
                            )
                        }
                        items(environmentalReadings) { reading ->
                            EnvironmentalReadingCard(
                                reading = reading,
                                onUpdate = onUpdateEnvironmentalReading
                            )
                        }
                    }
                    9 -> {
                        item {
                            SectionHeaderCard(
                                title = "ATTESTATION & SIGNATURES",
                                subtitle = "Field Assessor & Appointed Reviewer Digital Attestation"
                            )
                        }
                        item {
                            SignaturePad(
                                title = "Field Assessor Attestation",
                                signerRole = "Occupational Hygiene Assessor",
                                signerName = assessment?.assessorName ?: "Assessor",
                                onSaveSignature = { sigText ->
                                    onSubmitAssessment(sigText)
                                }
                            )
                        }
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
                                        text = "Submission Pre-Flight Check",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val answered = answers.count { it.response != ComplianceResponse.NOT_ASSESSED.name }
                                    val total = answers.size
                                    Text(
                                        text = "• Checklist completion: $answered of $total questions answered",
                                        color = if (answered == total) ComplianceGreenLight else ReviewAmber,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "• Deep-cleaning chemicals recorded: ${deepChemicals.size}",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "• Routine-cleaning chemicals recorded: ${routineChemicals.size}",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "• Environmental readings: ${environmentalReadings.size} periods recorded",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Dialog for creating a Remedial Action for a question
    if (remedialActionDialogQuestion != null) {
        val q = remedialActionDialogQuestion!!
        var finding by remember { mutableStateOf("Non-compliance observed in ${q.sectionTitle} - Question ${q.questionNumber}") }
        var actionText by remember { mutableStateOf("") }
        var responsible by remember { mutableStateOf("Facilities / Cleaning Team") }
        var targetDate by remember { mutableStateOf("2026-10-31") }
        var priority by remember { mutableStateOf(RemedialPriority.MEDIUM) }

        AlertDialog(
            onDismissRequest = { remedialActionDialogQuestion = null },
            containerColor = WfhSurfaceElevated,
            title = {
                Text(
                    text = "Raise Remedial Action",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Question: ${q.questionText}",
                        color = WfhTextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = finding,
                        onValueChange = { finding = it },
                        label = { Text("Finding Description") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = actionText,
                        onValueChange = { actionText = it },
                        label = { Text("Recommended Corrective Action") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = responsible,
                        onValueChange = { responsible = it },
                        label = { Text("Responsible Person / Dept") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = targetDate,
                        onValueChange = { targetDate = it },
                        label = { Text("Target Completion Date (YYYY-MM-DD)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreateRemedialAction(
                            finding,
                            "${q.sectionTitle} - Q${q.questionNumber}",
                            actionText.ifEmpty { "Immediate rectification and sanitisation" },
                            responsible,
                            targetDate,
                            priority
                        )
                        remedialActionDialogQuestion = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary)
                ) {
                    Text("Create Action")
                }
            },
            dismissButton = {
                TextButton(onClick = { remedialActionDialogQuestion = null }) {
                    Text("Cancel", color = WfhTextSecondary)
                }
            }
        )
    }

    // Dialog for adding custom question in Section G
    if (showAddCustomDialog) {
        var customQ by remember { mutableStateOf("") }
        var customRem by remember { mutableStateOf("") }
        var customResp by remember { mutableStateOf(ComplianceResponse.YES) }

        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            containerColor = WfhSurfaceElevated,
            title = { Text("Add Custom Inspection Item", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customQ,
                        onValueChange = { customQ = it },
                        label = { Text("Inspection Question") }
                    )
                    OutlinedTextField(
                        value = customRem,
                        onValueChange = { customRem = it },
                        label = { Text("Remarks / Observations") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customQ.isNotEmpty()) {
                            onAddCustomItem(customQ, customResp, customRem)
                            showAddCustomDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary)
                ) {
                    Text("Add Item")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) {
                    Text("Cancel", color = WfhTextSecondary)
                }
            }
        )
    }

    // Dialog for adding Chemical
    if (showAddChemicalDialogCategory != null) {
        val cat = showAddChemicalDialogCategory!!
        var chemName by remember { mutableStateOf("") }
        var manufacturer by remember { mutableStateOf("") }
        var remarks by remember { mutableStateOf("") }
        var sdsReturned by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddChemicalDialogCategory = null },
            containerColor = WfhSurfaceElevated,
            title = {
                Text(
                    text = if (cat == "DEEP_CLEANING") "Add Deep-Cleaning Chemical" else "Add Routine Chemical",
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = chemName,
                        onValueChange = { chemName = it },
                        label = { Text("Chemical Product Name") }
                    )
                    OutlinedTextField(
                        value = manufacturer,
                        onValueChange = { manufacturer = it },
                        label = { Text("Manufacturer / Supplier") }
                    )
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Usage / Dilution Ratio") }
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = sdsReturned,
                            onCheckedChange = { sdsReturned = it },
                            colors = CheckboxDefaults.colors(checkedColor = WfhRedPrimary)
                        )
                        Text("Safety Data Sheet (SDS) Returned", color = Color.White, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (chemName.isNotEmpty()) {
                            onAddChemical(cat, chemName, sdsReturned, manufacturer, remarks)
                            showAddChemicalDialogCategory = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WfhRedPrimary)
                ) {
                    Text("Add Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChemicalDialogCategory = null }) {
                    Text("Cancel", color = WfhTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SectionHeaderCard(title: String, subtitle: String) {
    Surface(
        color = WfhSurfaceCard,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = WfhTextSecondary
            )
        }
    }
}

@Composable
private fun QuestionCard(
    answer: AnswerEntity,
    onUpdate: (AnswerEntity) -> Unit,
    onOpenRemedialAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp))
            .testTag("question_card_${answer.questionId}"),
        colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = WfhSurfaceElevated,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Q${answer.questionNumber}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WfhGold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = answer.questionText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Yes / No Selection Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isYes = answer.response == ComplianceResponse.YES.name
                val isNo = answer.response == ComplianceResponse.NO.name

                Button(
                    onClick = {
                        onUpdate(answer.copy(response = ComplianceResponse.YES.name))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("btn_yes_${answer.questionId}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isYes) ComplianceGreen else WfhSurfaceElevated,
                        contentColor = if (isYes) Color.White else WfhTextSecondary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = if (isYes) null else androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Yes", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("YES", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onUpdate(answer.copy(response = ComplianceResponse.NO.name))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("btn_no_${answer.questionId}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNo) CriticalRed else WfhSurfaceElevated,
                        contentColor = if (isNo) Color.White else WfhTextSecondary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = if (isNo) null else androidx.compose.foundation.BorderStroke(1.dp, WfhBorderSubtle)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "No", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("NO", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Remarks field
            OutlinedTextField(
                value = answer.remarks,
                onValueChange = { onUpdate(answer.copy(remarks = it)) },
                label = { Text("Remarks / Observations", fontSize = 11.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("remarks_${answer.questionId}"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WfhRedPrimary,
                    unfocusedBorderColor = WfhBorderSubtle,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = WfhSurfaceDark,
                    unfocusedContainerColor = WfhSurfaceDark
                ),
                shape = RoundedCornerShape(8.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action row: Photo, GPS Tag, Remedial Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Photo toggle
                    IconButton(
                        onClick = {
                            val newPhoto = if (answer.photoUri == null) "evidence_${answer.questionId}.jpg" else null
                            onUpdate(answer.copy(photoUri = newPhoto))
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (answer.photoUri != null) ComplianceGreenContainer else WfhSurfaceElevated, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Photo",
                            tint = if (answer.photoUri != null) ComplianceGreenLight else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // GPS Tag
                    IconButton(
                        onClick = {
                            onUpdate(answer.copy(geoCoordinates = "-23.8315° S, 30.1628° E"))
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(WfhSurfaceElevated, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "GPS",
                            tint = WfhGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (answer.response == ComplianceResponse.NO.name) {
                    TextButton(
                        onClick = onOpenRemedialAction,
                        colors = ButtonDefaults.textButtonColors(contentColor = CriticalRed)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Action", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Raise Action", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (answer.photoUri != null) {
                Surface(
                    color = ComplianceGreenContainer,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = ComplianceGreenLight, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Photo Attached: ${answer.photoUri}", fontSize = 10.sp, color = ComplianceGreenLight)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChemicalRegisterSection(
    title: String,
    chemicals: List<ChemicalRecordEntity>,
    onAddClick: () -> Unit,
    onDeleteClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = WfhSurfaceElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                IconButton(onClick = onAddClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = WfhRedVibrant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (chemicals.isEmpty()) {
                Text(
                    text = "No chemical products recorded. Tap '+' to add.",
                    fontSize = 11.sp,
                    color = WfhTextMuted
                )
            } else {
                chemicals.forEach { chem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(WfhSurfaceDark, RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(chem.chemicalName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                            Text("${chem.manufacturer} • ${chem.remarks}", color = WfhTextSecondary, fontSize = 10.sp)
                        }
                        Surface(
                            color = if (chem.sdsReturned) ComplianceGreenContainer else CriticalRedContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (chem.sdsReturned) "SDS Sighted" else "No SDS",
                                color = if (chem.sdsReturned) ComplianceGreenLight else CriticalRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        IconButton(onClick = { onDeleteClick(chem.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = WfhTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnvironmentalReadingCard(
    reading: EnvironmentalReadingEntity,
    onUpdate: (EnvironmentalReadingEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = reading.periodName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = WfhGold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = reading.measurementTime,
                    onValueChange = { onUpdate(reading.copy(measurementTime = it)) },
                    label = { Text("Time (HH:mm)", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = reading.wetBulbTemp,
                    onValueChange = { onUpdate(reading.copy(wetBulbTemp = it)) },
                    label = { Text("Wet-Bulb (°C)", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = reading.dryBulbTemp,
                    onValueChange = { onUpdate(reading.copy(dryBulbTemp = it)) },
                    label = { Text("Dry-Bulb (°C)", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = reading.globeTemp,
                    onValueChange = { onUpdate(reading.copy(globeTemp = it)) },
                    label = { Text("Globe (°C)", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = reading.wbgtIndex,
                    onValueChange = { onUpdate(reading.copy(wbgtIndex = it)) },
                    label = { Text("WBGT (°C)", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = reading.relativeHumidity,
                    onValueChange = { onUpdate(reading.copy(relativeHumidity = it)) },
                    label = { Text("RH (%)", fontSize = 10.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = reading.generalConditions,
                onValueChange = { onUpdate(reading.copy(generalConditions = it)) },
                label = { Text("General Environmental Conditions", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Equipment: ${reading.equipmentUsed}",
                fontSize = 10.sp,
                color = WfhTextMuted
            )
        }
    }
}

@Composable
private fun WizardOverviewStep(assessment: AssessmentEntity?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, WfhBorderSubtle, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = WfhSurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Site & Inspection Parameters",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            InfoRow("Company:", assessment?.customerName ?: "")
            InfoRow("Mine / Site:", assessment?.siteName ?: "")
            InfoRow("Facility:", assessment?.facilityName ?: "")
            InfoRow("DME Mine Code:", assessment?.dmeMineCode ?: "")
            InfoRow("Document Ref:", assessment?.documentReference ?: "")
            InfoRow("Field Visit Date:", assessment?.fieldVisitDate ?: "")
            InfoRow("Previous Visit Date:", assessment?.previousVisitDate ?: "")
            InfoRow("Mine Manager:", assessment?.mineManager ?: "")
            InfoRow("Appointed Hygienist:", assessment?.siteOccupationalHygienist ?: "")

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = WfhBorderSubtle)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Special Hygienist Instructions:",
                fontWeight = FontWeight.Bold,
                color = WfhGold,
                fontSize = 12.sp
            )
            Text(
                text = assessment?.hygienistSpecialInstructions ?: "None specified.",
                color = WfhTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = WfhTextSecondary, fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
